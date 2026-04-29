package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.WinningRecordDO;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WinnerRecordMapper {
    void batchInsert(@Param("items") List<WinningRecordDO> winningRecordDOList);

    List<WinningRecordDO> selectByActivityId(@Param("activityId") Long activityId);

    int countByAPId(@Param("activityId") Long activityId, @Param("prizeId") Long prizeId);

    void deleteRecords(@Param("activityId") Long activityId, @Param("prizeId") Long prizeId);

    List<WinningRecordDO> selectByActivityIdOrPrizeId(@Param("activityId") Long activityId, @Param("prizeId") Long prizeId);
}
