package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.PrizeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrizeMapper {
    int insert(PrizeDO prizeDO);

    int count();

    List<PrizeDO> selectPrizeList(@Param("offset") int offset, @Param("pageSize") Integer pageSize);

    List<Long> selectExistById(@Param("items") List<Long> prizeIds);
}
