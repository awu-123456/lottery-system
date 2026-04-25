package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class findActivityListResult implements Serializable {

    private Integer total;

    private List<ActivityInfo> records;

    @Data
    public static class ActivityInfo implements Serializable {

        private Long activityId;

        private String activityName;

        private String description;

        private Boolean valid;
    }
}
