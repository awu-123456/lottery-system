package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.UserIdentityEnum;
import lombok.Data;

@Data
public class UserLoginDTO {
    private String token;
    private UserIdentityEnum identity;
}
