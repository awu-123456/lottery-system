package com.example.lotterysystem.common.errorcode;

import lombok.Data;

@Data
public class ErrorCode {
    private final Integer code;
    private final String msg;

    public ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
