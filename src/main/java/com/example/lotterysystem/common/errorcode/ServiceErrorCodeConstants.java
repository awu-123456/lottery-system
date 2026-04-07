package com.example.lotterysystem.common.errorcode;

public interface ServiceErrorCodeConstants {
    ErrorCode REGISTER_INFO_IS_EMPTY = new ErrorCode(100,"注册信息为空");
    ErrorCode MAIL_ERROR = new ErrorCode(101,"邮箱错误");
    ErrorCode PHONE_NUMBER_ERROR = new ErrorCode(102,"手机号错误");
    ErrorCode IDENTITY_ERROR = new ErrorCode(103,"身份错误");
    ErrorCode PASSWORD_IS_EMPTY = new ErrorCode(104,"密码为空");
    ErrorCode PASSWORD_ERROR = new ErrorCode(105,"密码错误");
    ErrorCode MAIL_USED = new ErrorCode(106,"邮箱被使用");
    ErrorCode PHONE_NUMBER_USED = new ErrorCode(107,"手机号被使用");
    ErrorCode LOGIN_INFO_NOT_EXIST = new ErrorCode(108,"登录信息不存在");
    ErrorCode LOGIN_NOT_EXIST = new ErrorCode(109,"登录方式不存在");
    ErrorCode USER_INFO_IS_EMPTY = new ErrorCode(110,"用户信息为空");
    ErrorCode VERIFICATION_CODE_ERROR = new ErrorCode(111,"验证码校验失败");
}
