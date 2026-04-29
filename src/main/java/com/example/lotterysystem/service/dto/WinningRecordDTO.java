package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import lombok.Data;

import java.util.Date;

@Data
public class WinningRecordDTO {

    private Long winnerId;

    private String winnerName;

    private String prizeName;

    private ActivityPrizeTiersEnum prizeTier;

    private Date winningTime;
}
