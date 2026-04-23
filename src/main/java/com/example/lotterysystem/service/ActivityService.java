package com.example.lotterysystem.service;

import com.example.lotterysystem.controller.param.CreateActivityParam;
import com.example.lotterysystem.service.dto.CreateActivityDTO;
import org.springframework.stereotype.Service;

@Service
public interface ActivityService {
    CreateActivityDTO createActivity(CreateActivityParam param);
}
