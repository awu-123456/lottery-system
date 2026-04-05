package com.example.lotterysystem.common.errorcode;

import lombok.Data;

@Data
public class ErrorCode {
    private Integer code;
    private String message;

    public ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
