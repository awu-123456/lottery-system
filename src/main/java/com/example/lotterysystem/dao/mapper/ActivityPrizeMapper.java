package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityPrizeMapper {
    void batchInsert(@Param("items") List<ActivityPrizeDO> activityPrizeDOList);

    List<ActivityPrizeDO> selectByActivityId(@Param("activityId") Long activityId);

    ActivityPrizeDO selectByAPId(@Param("activityId") Long activityId, @Param("prizeId") Long prizeId);

    int countPrize(@Param("activityId") Long activityId, @Param("status") String status);

    void updateStatus(@Param("activityId") Long activityId, @Param("prizeId") Long prizeId, @Param("status") ActivityStatusEnum status);
}
