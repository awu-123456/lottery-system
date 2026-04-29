package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ActivityDetailDTO {

    private Long activityId;

    private String activityName;

    private String desc;

    private ActivityStatusEnum status;

    public Boolean valid() {
        return status.equals(ActivityStatusEnum.RUNNING);
    }

    private List<PrizeDTO> prizeDTOList;

    private List<UserDTO> userDTOList;

    @Data
    public static class PrizeDTO {

        private Long prizeId;

        private String name;

        private String imageUrl;

        private BigDecimal price;

        private String description;

        private ActivityPrizeTiersEnum tiers;

        private Long prizeAmount;

        private ActivityPrizeStatusEnum status;

        public Boolean valid() {
            return status.equals(ActivityPrizeStatusEnum.INIT);
        }
    }

    @Data
    public static class UserDTO {

        private Long userId;

        private String userName;

        private ActivityUserStatusEnum status;

        public  Boolean valid() {
            return status.equals(ActivityUserStatusEnum.INIT);
        }
    }
}
