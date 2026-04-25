package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.ActivityDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityMapper {
    void insert(ActivityDO activityDO);

    int count();

    List<ActivityDO> selectActivityList(@Param("offset") int offset, @Param("pageSize") Integer pageSize);
}
