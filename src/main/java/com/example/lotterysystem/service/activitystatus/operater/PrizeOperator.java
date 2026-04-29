package com.example.lotterysystem.service.activitystatus.operater;

import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.dao.mapper.ActivityPrizeMapper;
import com.example.lotterysystem.service.ActivityService;
import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrizeOperator extends AbstractActivityOperator {

    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;

    @Override
    public Integer sequence() {
        return 1;
    }

    @Override
    public Boolean needConvert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        Long activityId = convertActivityStatusDTO.getActivityId();
        Long prizeId = convertActivityStatusDTO.getPrizeId();
        ActivityStatusEnum targetActivityStatusEnum = convertActivityStatusDTO.getTargetActivityStatus();
        if(activityId == null || prizeId == null || targetActivityStatusEnum == null) {
            return false;
        }
        ActivityPrizeDO activityPrizeDO = activityPrizeMapper.selectByAPId(activityId,prizeId);
        if(activityPrizeDO == null) {
            return false;
        }
        if(targetActivityStatusEnum.name().equalsIgnoreCase(activityPrizeDO.getStatus())) {
            return false;
        }
        return true;
    }

    @Override
    public Boolean convert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        Long activityId = convertActivityStatusDTO.getActivityId();
        Long prizeId = convertActivityStatusDTO.getPrizeId();
        ActivityStatusEnum targetActivityStatusEnum = convertActivityStatusDTO.getTargetActivityStatus();
        try {
            activityPrizeMapper.updateStatus(activityId,prizeId,targetActivityStatusEnum);
            return  true;
        } catch (Exception e) {
            return false;
        }
    }
}
