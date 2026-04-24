package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.ActivityDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActivityMapper {
    void insert(ActivityDO activityDO);
}
