package com.example.lotterysystem.common.polo;

import com.example.lotterysystem.common.errorcode.ErrorCode;
import com.example.lotterysystem.common.errorcode.GlobalErrorCodeConstants;
import lombok.Data;
import org.springframework.util.Assert;

import java.io.Serializable;

@Data
public class CommonResult<T> implements Serializable {
    private Integer code;
    private T data;
    private String msg;

    public static <T> CommonResult<T> success(T data) {
        CommonResult<T> commonResult = new CommonResult<T>();
        commonResult.setCode(GlobalErrorCodeConstants.SUCCESS.getCode());
        commonResult.setData(data);
        commonResult.setMsg("");
        return commonResult;
    }

    public static <T> CommonResult<T> error(Integer code, String msg) {
        Assert.isTrue(!GlobalErrorCodeConstants.SUCCESS.getCode().equals(code),
                "code 不是错误的异常");
        CommonResult<T> commonResult = new CommonResult<T>();
        commonResult.setCode(code);
        commonResult.setMsg(msg);
        return commonResult;
    }

    public static <T> CommonResult<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMsg());
    }
}
