package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import lombok.Data;

import java.util.List;

@Data
public class ConvertActivityStatusDTO {

    private Long activityId;

    private ActivityStatusEnum targetActivityStatus;

    private Long prizeId;

    private ActivityPrizeStatusEnum targetPrizeStatus;

    private List<Long> userIds;

    private ActivityUserStatusEnum targetUserStatus;
}
