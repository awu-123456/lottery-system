package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityPrizeMapper {
    void batchInsert(@Param("items") List<ActivityPrizeDO> activityPrizeDOList);
}
