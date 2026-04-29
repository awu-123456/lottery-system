package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.common.utils.RedisUtil;
import com.example.lotterysystem.controller.param.DrawPrizeParam;
import com.example.lotterysystem.controller.param.ShowWinningRecordsParam;
import com.example.lotterysystem.dao.dataobject.*;
import com.example.lotterysystem.dao.mapper.*;
import com.example.lotterysystem.service.DrawPrizeService;
import com.example.lotterysystem.service.dto.WinningRecordDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.lotterysystem.common.config.DirectRabbitConfig.EXCHANGE_NAME;
import static com.example.lotterysystem.common.config.DirectRabbitConfig.ROUTING;

@Service
public class DrawPrizeServiceImpl implements DrawPrizeService {

    private static final Logger logger = LoggerFactory.getLogger(DrawPrizeServiceImpl.class);
    private final String WINNING_RECORDS_PREFIX = "WINNING_RECORDS_";
    private final Long WINNING_RECORDS_TIMEOUT = 60 * 60 * 24 * 2L;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PrizeMapper prizeMapper;
    @Autowired
    private WinnerRecordMapper winnerRecordMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public void drawPrize(DrawPrizeParam param) {
        Map<String, String> map = new HashMap<>();
        map.put("messageId",String.valueOf(UUID.randomUUID()));
        map.put("messageData", JacksonUtil.writeValueAsString(param));
        rabbitTemplate.convertAndSend(EXCHANGE_NAME,ROUTING,map);
        logger.info("mq消息发送成功：map={}",JacksonUtil.writeValueAsString(map));
    }

    @Override
    public List<WinningRecordDO> saveWinnerRecords(DrawPrizeParam param) {
        ActivityDO activityDO = activityMapper.selectById(param.getActivityId());
        List<Long> userIds = param.getWinnerList().stream()
                .map(DrawPrizeParam.Winner::getUserId)
                .collect(Collectors.toList());
        List<UserDO> userDOList = userMapper.batchSelectByIds(userIds);
        PrizeDO prizeDO = prizeMapper.selectById(param.getPrizeId());
        ActivityPrizeDO activityPrizeDO = activityPrizeMapper.selectByAPId(param.getActivityId(), param.getPrizeId());
        List<WinningRecordDO> winningRecordDOList = userDOList.stream()
                .map(userDO -> {
                    WinningRecordDO winningRecordDO = new WinningRecordDO();
                    winningRecordDO.setActivityId(activityDO.getId());
                    winningRecordDO.setActivityName(activityDO.getActivityName());
                    winningRecordDO.setPrizeId(prizeDO.getId());
                    winningRecordDO.setPrizeName(prizeDO.getName());
                    winningRecordDO.setPrizeTier(activityPrizeDO.getPrizeTiers());
                    winningRecordDO.setWinnerId(userDO.getId());
                    winningRecordDO.setWinnerName(userDO.getUserName());
                    winningRecordDO.setWinnerEmail(userDO.getEmail());
                    winningRecordDO.setWinningTime(param.getWinningTime());
                    return winningRecordDO;
                }).collect(Collectors.toList());
        winnerRecordMapper.batchInsert(winningRecordDOList);
        cacheWinningRecords(param.getActivityId()+"_"+param.getPrizeId(),
                winningRecordDOList,
                WINNING_RECORDS_TIMEOUT);
        if(activityDO.getStatus().equalsIgnoreCase(ActivityStatusEnum.COMPLETED.name())) {
            List<WinningRecordDO> allList = winnerRecordMapper.selectByActivityId(param.getActivityId());
            cacheWinningRecords(String.valueOf(param.getActivityId()), allList, WINNING_RECORDS_TIMEOUT);
        }
        return winningRecordDOList;
    }

    @Override
    public void deleteRecords(Long activityId, Long prizeId) {
        if(activityId == null) {
            logger.warn("要删除中奖记录相关的活动id为空！");
            return;
        }
        winnerRecordMapper.deleteRecords(activityId, prizeId);
        if(prizeId != null) {
            deleteWinningRecords(activityId+"_"+prizeId);
        }
        deleteWinningRecords(String.valueOf(activityId));
    }

    private void deleteWinningRecords(String key) {
        try {
            if(redisUtil.hasKey(WINNING_RECORDS_PREFIX+key)) {
                redisUtil.del(WINNING_RECORDS_PREFIX+key);
            }
        } catch (Exception e) {
            logger.error("删除中奖记录缓存异常，key:{}",key);
        }

    }

    private void cacheWinningRecords(String key, List<WinningRecordDO> winningRecordDOList, Long time) {
        String str = "";
        try {
            if(key == null || CollectionUtils.isEmpty(winningRecordDOList)) {
                logger.warn("要缓存的内容为空！key:{}, value:{}", key, JacksonUtil.writeValueAsString(winningRecordDOList));
                return;
            }
            str = JacksonUtil.writeValueAsString(winningRecordDOList);
            redisUtil.set(WINNING_RECORDS_PREFIX+key, str, time);
        } catch (Exception e) {
            logger.error("缓存中奖记录异常！key:{}, value:{}", WINNING_RECORDS_PREFIX + key, str);
        }
    }

    @Override
    public List<WinningRecordDTO> getRecords(ShowWinningRecordsParam param) {
        String key = null == param.getPrizeId() ? String.valueOf(param.getActivityId()) : param.getActivityId()+"_"+param.getPrizeId();
        List<WinningRecordDO>  winningRecordDOList = getWinningRecords(key);
        if(!CollectionUtils.isEmpty(winningRecordDOList)) {
            return convertToWinningRecordDTOList(winningRecordDOList);
        }
        winningRecordDOList = winnerRecordMapper.selectByActivityIdOrPrizeId(param.getActivityId(), param.getPrizeId());
        if(CollectionUtils.isEmpty(winningRecordDOList)) {
            logger.info("查询的中奖记录为空！param:{}", JacksonUtil.writeValueAsString(param));
            return Arrays.asList();
        }
        cacheWinningRecords(key, winningRecordDOList, WINNING_RECORDS_TIMEOUT);
        return convertToWinningRecordDTOList(winningRecordDOList);
    }

    private List<WinningRecordDTO> convertToWinningRecordDTOList(List<WinningRecordDO> winningRecordDOList) {
        if (CollectionUtils.isEmpty(winningRecordDOList)) {
            return Arrays.asList();
        }
        return winningRecordDOList.stream()
                .map(winningRecordDO -> {
                    WinningRecordDTO winningRecordDTO = new WinningRecordDTO();
                    winningRecordDTO.setWinnerId(winningRecordDO.getWinnerId());
                    winningRecordDTO.setWinnerName(winningRecordDO.getWinnerName());
                    winningRecordDTO.setPrizeName(winningRecordDO.getPrizeName());
                    winningRecordDTO.setPrizeTier(ActivityPrizeTiersEnum.forName(winningRecordDO.getPrizeTier()));
                    winningRecordDTO.setWinningTime(winningRecordDO.getWinningTime());
                    return winningRecordDTO;
                }).collect(Collectors.toList());
    }

    private List<WinningRecordDO> getWinningRecords(String key) {
        try {
            if(StringUtils.isEmpty(key)) {
                logger.warn("要从缓存中查询中奖记录的key为空！");
                return Arrays.asList();
            }
            String str = redisUtil.get(WINNING_RECORDS_PREFIX+key);
            if(StringUtils.isEmpty(str)) {
                return Arrays.asList();
            }
            List<WinningRecordDO> winningRecordDOList = JacksonUtil.readListValue(str, WinningRecordDO.class);
            return winningRecordDOList;
        } catch (Exception e) {
            logger.error("从缓存中查询中奖记录异常！key:{}", WINNING_RECORDS_PREFIX + key);
            return Arrays.asList();
        }
    }

    @Override
    public Boolean checkDrawPrizeParam(DrawPrizeParam param) {
        ActivityDO activityDO = activityMapper.selectById(param.getActivityId());
        ActivityPrizeDO activityPrizeDO = activityPrizeMapper.selectByAPId(param.getActivityId(), param.getPrizeId());
        if(activityDO == null || activityPrizeDO == null) {
            logger.info("校验抽奖请求失败！失败原因：{}", ServiceErrorCodeConstants.ACTIVITY_OR_PRIZE_IS_EMPTY.getMsg());
            return false;
        }
        if (activityDO.getStatus().equalsIgnoreCase(ActivityStatusEnum.COMPLETED.name())) {
            logger.info("校验抽奖请求失败！失败原因：{}", ServiceErrorCodeConstants.ACTIVITY_COMPLETED.getMsg());
            return false;
        }
        if (activityPrizeDO.getStatus().equalsIgnoreCase(ActivityPrizeStatusEnum.COMPLETED.name())) {
            logger.info("校验抽奖请求失败！失败原因：{}", ServiceErrorCodeConstants.ACTIVITY_PRIZE_COMPLETED.getMsg());
            return false;
        }
        if(activityPrizeDO.getPrizeAmount() != param.getWinnerList().size()) {
            logger.info("校验抽奖请求失败！失败原因：{}", ServiceErrorCodeConstants.WINNER_PRIZE_AMOUNT_ERROR.getMsg());
            return false;
        }
        return true;
    }
}