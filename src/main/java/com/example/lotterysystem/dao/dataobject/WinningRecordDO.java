package com.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class WinningRecordDO extends BaseDO{

    private Long activityId;

    private String activityName;

    private Long prizeId;

    private String prizeName;

    private String prizeTier;

    private Long winnerId;

    private String winnerName;

    private String winnerEmail;

    private Date winningTime;
}
