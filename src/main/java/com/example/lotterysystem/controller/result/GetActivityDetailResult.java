package com.example.lotterysystem.controller.result;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GetActivityDetailResult implements Serializable {

    private Long activityId;

    private String activityName;

    private String description;

    private Boolean valid;

    private List<Prize> prizes;

    private List<User>  users;

    @Data
    public static class Prize {

        private Long prizeId;

        private String name;

        private String imageUrl;

        private BigDecimal price;

        private String description;

        private String prizeTierName;

        private Long prizeAmount;

        private Boolean valid;
    }

    @Data
    public static class User {

        private Long userId;

        private String userName;

        private Boolean valid;
    }
}
