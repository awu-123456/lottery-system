package com.example.lotterysystem.controller;

import com.example.lotterysystem.common.errorcode.ControllerErrorCodeConstants;
import com.example.lotterysystem.common.exception.ControllerException;
import com.example.lotterysystem.common.polo.CommonResult;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.controller.param.CreateActivityParam;
import com.example.lotterysystem.controller.param.PageParam;
import com.example.lotterysystem.controller.result.CreateActivityResult;
import com.example.lotterysystem.controller.result.FindPrizeListResult;
import com.example.lotterysystem.controller.result.findActivityListResult;
import com.example.lotterysystem.service.ActivityService;
import com.example.lotterysystem.service.dto.ActivityDTO;
import com.example.lotterysystem.service.dto.CreateActivityDTO;
import com.example.lotterysystem.service.dto.PageListDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
public class ActivityController {

    private Logger logger = LoggerFactory.getLogger(ActivityController.class);
    @Autowired
    private ActivityService activityService;

    @RequestMapping("/activity/create")
    public CommonResult<CreateActivityResult> createActivity(@Validated @RequestBody CreateActivityParam param) {
        logger.info("createActivity CreateActivityParam:{}", JacksonUtil.writeValueAsString(param));
        return CommonResult.success(convertToCreateActivityResult(activityService.createActivity(param)));
    }

    public CommonResult<findActivityListResult> findActivityList(PageParam pageParam) {
        logger.info("findActivityList PageParam:{}", JacksonUtil.writeValueAsString(pageParam));
        return CommonResult.success(convertTofindActivityListResult(activityService.findActivityList(pageParam)));
    }

    private findActivityListResult convertTofindActivityListResult(PageListDTO<ActivityDTO> activityList) {
        if(activityList == null) {
            throw new ControllerException(ControllerErrorCodeConstants.FIND_ACTIVITY_LIST_ERROR);
        }
        findActivityListResult result = new findActivityListResult();
        result.setTotal(activityList.getTotal());
        result.setRecords(activityList.getRecords().stream()
                .map(activityDTO -> {
                    findActivityListResult.ActivityInfo activityInfo = new findActivityListResult.ActivityInfo();
                    activityInfo.setActivityId(activityDTO.getActivityId());
                    activityInfo.setActivityName(activityDTO.getActivityName());
                    activityInfo.setDescription(activityDTO.getDescription());
                    activityInfo.setValid(activityDTO.valid());
                    return  activityInfo;
                }).collect(Collectors.toList()));
        return result;
    }

    public CreateActivityResult convertToCreateActivityResult(CreateActivityDTO createActivityDTO) {
        if(createActivityDTO == null) {
            throw new ControllerException(ControllerErrorCodeConstants.CREATE_ACTIVITY_ERROR);
        }
        CreateActivityResult result = new CreateActivityResult();
        result.setActivityId(createActivityDTO.getActivityId());
        return result;
    }
}
