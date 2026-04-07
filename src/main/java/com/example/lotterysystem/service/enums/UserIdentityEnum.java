package com.example.lotterysystem.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserIdentityEnum {

    ADMIN("管理员"),
    NORMAL("普通用户")
    ;

    private final String message;

    public static UserIdentityEnum forName(String name) {
        for (UserIdentityEnum e : UserIdentityEnum.values()) {
            if (e.name().equals(name)) {
                return e;
            }
        }
        return null;
    }
}
