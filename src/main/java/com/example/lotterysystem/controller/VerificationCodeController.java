package com.example.lotterysystem.controller;

import com.example.lotterysystem.common.polo.CommonResult;
import com.example.lotterysystem.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VerificationCodeController {

    @Autowired
    private VerificationCodeService verificationCodeService;

    @GetMapping("/verification-code/send")
    public CommonResult<String> sendVerificationCode(@RequestParam String phoneNumber) {
        verificationCodeService.sendVerificationCode(phoneNumber);
        return CommonResult.success("验证码发送成功");
    }
}