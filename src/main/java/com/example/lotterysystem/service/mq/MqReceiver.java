package com.example.lotterysystem.service.mq;

import cn.hutool.core.date.DateUtil;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.common.utils.MailUtil;
import com.example.lotterysystem.controller.param.DrawPrizeParam;
import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.dao.dataobject.WinningRecordDO;
import com.example.lotterysystem.dao.mapper.ActivityPrizeMapper;
import com.example.lotterysystem.dao.mapper.WinnerRecordMapper;
import com.example.lotterysystem.service.DrawPrizeService;
import com.example.lotterysystem.service.activitystatus.ActivityStatusManager;
import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static com.example.lotterysystem.common.config.DirectRabbitConfig.QUEUE_NAME;

@Component
@RabbitListener(queues = QUEUE_NAME)
public class MqReceiver {

    private static final Logger logger = LoggerFactory.getLogger(MqReceiver.class);

    @Autowired
    private DrawPrizeService drawPrizeService;
    @Autowired
    private ActivityStatusManager activityStatusManager;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolExecutor;
    @Autowired
    private MailUtil mailUtil;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;
    @Autowired
    private WinnerRecordMapper winnerRecordMapper;

    @RabbitHandler
    public void process(Map<String, String> message) {
        logger.info("MQ成功接收到消息，message:{}", JacksonUtil.writeValueAsString(message));
        String paramString = message.get("messageData");
        DrawPrizeParam param = JacksonUtil.readValue(paramString, DrawPrizeParam.class);
        try {
            if(!drawPrizeService.checkDrawPrizeParam(param)) {
                return;
            }
            statusConvert(param);
            List<WinningRecordDO> winningRecordDOList =  drawPrizeService.saveWinnerRecords(param);
            syncExecute(winningRecordDOList);
        } catch (ServiceException e) {
            logger.error("处理 MQ 消息异常！{}:{}",e.getCode(), e.getMessage(), e);
            rollback(param);
            throw e;
        } catch (Exception e) {
            logger.error("处理 MQ 消息异常！");
            rollback(param);
            throw e;
        }
    }

    private void rollback(DrawPrizeParam param) {
        if(!statusNeedRollback(param)) {
            return;
        }
        rollbackStatus(param);
        if(!winnerNeedRollback(param)) {
            return;
        }
        rollbackWinner(param);
    }

    private void rollbackWinner(DrawPrizeParam param) {
        drawPrizeService.deleteRecords(param.getActivityId(), param.getPrizeId());
    }

    private void rollbackStatus(DrawPrizeParam param) {
        ConvertActivityStatusDTO convertActivityStatusDTO = new ConvertActivityStatusDTO();
        convertActivityStatusDTO.setActivityId(param.getActivityId());
        convertActivityStatusDTO.setTargetActivityStatus(ActivityStatusEnum.RUNNING);
        convertActivityStatusDTO.setPrizeId(param.getPrizeId());
        convertActivityStatusDTO.setTargetPrizeStatus(ActivityPrizeStatusEnum.INIT);
        convertActivityStatusDTO.setUserIds(param.getWinnerList().stream()
                .map(DrawPrizeParam.Winner::getUserId)
                .collect(Collectors.toList()));
        convertActivityStatusDTO.setTargetUserStatus(ActivityUserStatusEnum.INIT);
        activityStatusManager.rollbackHandlerEvent(convertActivityStatusDTO);
    }

    private boolean winnerNeedRollback(DrawPrizeParam param) {
        int count = winnerRecordMapper.countByAPId(param.getActivityId(), param.getPrizeId());
        return count > 0;
    }

    private boolean statusNeedRollback(DrawPrizeParam param) {
        ActivityPrizeDO activityPrizeDO = activityPrizeMapper.selectByAPId(param.getActivityId(), param.getPrizeId());
        return activityPrizeDO.getStatus().equalsIgnoreCase(ActivityPrizeStatusEnum.COMPLETED.name());
    }

    private void syncExecute(List<WinningRecordDO> winningRecordDOList) {
        threadPoolExecutor.execute(() -> sendMail(winningRecordDOList));
    }

    private void sendMail(List<WinningRecordDO> winningRecordDOList) {
        if(CollectionUtils.isEmpty(winningRecordDOList)) {
            logger.info("中奖列表为空，不用发邮件！");
            return;
        }
        for (WinningRecordDO winningRecordDO : winningRecordDOList) {
            String context =  "Hi，" + winningRecordDO.getWinnerName() + "。恭喜你在"
                    + winningRecordDO.getActivityName() + "活动中获得"
                    + ActivityPrizeTiersEnum.forName(winningRecordDO.getPrizeTier()).getMessage()
                    + "：" + winningRecordDO.getPrizeName() + "。获奖时间为"
                    + DateUtil.formatTime(winningRecordDO.getWinningTime()) + "，请尽快领 取您的奖励！";
            mailUtil.sendSampleMail(winningRecordDO.getWinnerEmail(), "中奖通知!", context) ;
        }
    }

    private void statusConvert(DrawPrizeParam param) {
        ConvertActivityStatusDTO convertActivityStatusDTO = new ConvertActivityStatusDTO();
        convertActivityStatusDTO.setActivityId(param.getActivityId());
        convertActivityStatusDTO.setTargetActivityStatus(ActivityStatusEnum.COMPLETED);
        convertActivityStatusDTO.setPrizeId(param.getPrizeId());
        convertActivityStatusDTO.setTargetPrizeStatus(ActivityPrizeStatusEnum.COMPLETED);
        convertActivityStatusDTO.setUserIds(param.getWinnerList().stream()
                .map(DrawPrizeParam.Winner::getUserId)
                .collect(Collectors.toList()));
        convertActivityStatusDTO.setTargetUserStatus(ActivityUserStatusEnum.COMPLETED);
        activityStatusManager.handlerEvent(convertActivityStatusDTO);
    }
}
