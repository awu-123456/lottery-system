package com.example.lotterysystem.service.activitystatus.operater;

import com.example.lotterysystem.dao.dataobject.ActivityDO;
import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.dao.mapper.ActivityMapper;
import com.example.lotterysystem.dao.mapper.ActivityPrizeMapper;
import com.example.lotterysystem.service.activitystatus.impl.ActivityStatusManagerImpl;
import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActivityOperator extends AbstractActivityOperator {

    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;

    @Override
    public Integer sequence() {
        return 2;
    }

    @Override
    public Boolean needConvert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        Long activityId = convertActivityStatusDTO.getActivityId();
        ActivityStatusEnum targetActivityStatusEnum = convertActivityStatusDTO.getTargetActivityStatus();
        if(activityId == null || targetActivityStatusEnum == null) {
            return false;
        }
        ActivityDO activityDO = activityMapper.selectById(activityId);
        if(activityDO == null) {
            return false;
        }
        if(targetActivityStatusEnum.name().equalsIgnoreCase(activityDO.getStatus())) {
            return false;
        }
        int count = activityPrizeMapper.countPrize(activityId, ActivityPrizeStatusEnum.INIT.name());
        if(count > 0) {
            return false;
        }
        return true;
    }

    @Override
    public Boolean convert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        try {
            activityMapper.updateStatus(convertActivityStatusDTO.getActivityId(),convertActivityStatusDTO.getTargetActivityStatus().name());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
