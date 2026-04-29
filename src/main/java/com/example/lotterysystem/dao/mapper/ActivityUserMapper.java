package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.ActivityUserDO;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper
public interface ActivityUserMapper {
    void batchInsert(@Param("items") List<ActivityUserDO> activityUserDOList);

    List<ActivityUserDO> selectByActivityId(@Param("activityId") Long activityId);

    List<ActivityUserDO> batchSelectByAUId(@Param("activityId") Long activityId, @Param("userIds") List<Long> userIds);

    void batchUpdateStatus(@Param("activityId") Long activityId, @Param("userIds") List<Long> userIds,
                           @Param("status") ActivityUserStatusEnum status);
}
