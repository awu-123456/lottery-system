package com.example.lotterysystem.controller.handler;

import com.example.lotterysystem.common.errorcode.GlobalErrorCodeConstants;
import com.example.lotterysystem.common.exception.ControllerException;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.polo.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = ServiceException.class)
    public CommonResult<?> ServiceExceptionHandler(ServiceException e) {
        logger.error("ServiceException:",e);
        return CommonResult.error(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),e.getMessage());
    }

    @ExceptionHandler(value = ControllerException.class)
    public CommonResult<?> ControllerExceptionHandler(ControllerException e) {
        logger.error("ControllerException:",e);
        return CommonResult.error(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),e.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public CommonResult<?> exceptionHandler(Exception e) {
        logger.error("服务异常:",e);
        return CommonResult.error(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),e.getMessage());
    }
}
