package com.example.lotterysystem.service;

import com.example.lotterysystem.controller.param.UserRegisterParam;
import com.example.lotterysystem.service.dto.UserRegisterDTO;

public interface UserService {
    UserRegisterDTO register(UserRegisterParam param);
}
