package com.example.lotterysystem.service.activitystatus.operater;

import com.example.lotterysystem.dao.dataobject.ActivityUserDO;
import com.example.lotterysystem.dao.mapper.ActivityUserMapper;
import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class UserOperator extends AbstractActivityOperator {

    @Autowired
    private ActivityUserMapper activityUserMapper;

    @Override
    public Integer sequence() {
        return 1;
    }

    @Override
    public Boolean needConvert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        Long activityId = convertActivityStatusDTO.getActivityId();
        List<Long> userIds = convertActivityStatusDTO.getUserIds();
        ActivityUserStatusEnum targetActivityUserStatusEnum = convertActivityStatusDTO.getTargetUserStatus();
        if(activityId == null || CollectionUtils.isEmpty(userIds) || targetActivityUserStatusEnum == null) {
            return false;
        }
        List<ActivityUserDO> activityUserDO = activityUserMapper.batchSelectByAUId(activityId,userIds);
        if(CollectionUtils.isEmpty(activityUserDO)) {
            return false;
        }
        for (ActivityUserDO auDO : activityUserDO) {
            if (auDO.getStatus().equalsIgnoreCase(targetActivityUserStatusEnum.name())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean convert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        Long activityId = convertActivityStatusDTO.getActivityId();
        List<Long> userIds = convertActivityStatusDTO.getUserIds();
        ActivityUserStatusEnum targetActivityUserStatusEnum = convertActivityStatusDTO.getTargetUserStatus();
        try {
            activityUserMapper.batchUpdateStatus(activityId,userIds,targetActivityUserStatusEnum);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
