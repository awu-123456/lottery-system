package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.*;
import com.example.lotterysystem.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {
    @Autowired
    private SMSUtil smsUtil;
    @Autowired
    private RedisUtil redisUtil;

    private static final String VERIFICATION_CODE_TEMPLATE_CODE = "SMS_465324787";
    private static final String VERIFICATION_CODE_PREFIX = "VERIFICATION_CODE_";
    private static final Long VERIFICATION_CODE_TIMEOUT = 60L;

    @Override
    public void sendVerificationCode(String phoneNumber) {
        if(!RegexUtil.checkMobile(phoneNumber)) {
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_ERROR);
        }
        String code = CaptchaUtil.getCaptcha(4);
        Map<String,String> map = new HashMap<>();
        map.put("code",code);
        smsUtil.sendMessage(VERIFICATION_CODE_TEMPLATE_CODE, phoneNumber,JacksonUtil.writeValueAsString(map));
        redisUtil.set(VERIFICATION_CODE_PREFIX+ phoneNumber, code,VERIFICATION_CODE_TIMEOUT);
    }

    @Override
    public String getVerificationCode(String phoneNumber) {
        if(!RegexUtil.checkMobile(phoneNumber)) {
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_ERROR);
        }
        return  redisUtil.get(VERIFICATION_CODE_PREFIX+ phoneNumber);
    }
}
