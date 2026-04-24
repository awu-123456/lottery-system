package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.ActivityUserDo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ActivityUserMapper {
    void batchInsert(@Param("items") List<ActivityUserDo> activityUserDoList);
}
