package com.example.lotterysystem.service;

import com.example.lotterysystem.controller.param.CreateActivityParam;
import com.example.lotterysystem.controller.param.PageParam;
import com.example.lotterysystem.service.dto.ActivityDTO;
import com.example.lotterysystem.service.dto.CreateActivityDTO;
import com.example.lotterysystem.service.dto.PageListDTO;
import org.springframework.stereotype.Service;

@Service
public interface ActivityService {
    CreateActivityDTO createActivity(CreateActivityParam param);

    PageListDTO<ActivityDTO> findActivityList(PageParam param);
}
