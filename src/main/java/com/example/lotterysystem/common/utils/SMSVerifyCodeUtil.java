package com.example.lotterysystem.common.utils;

import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import darabonba.core.client.ClientOverrideConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class SMSVerifyCodeUtil {
    private static final Logger logger = LoggerFactory.getLogger(SMSVerifyCodeUtil.class);

    @Value("${sms.access-key-id}")
    private String accessKeyId;
    @Value("${sms.access-key-secret}")
    private String accessKeySecret;
    @Value("${sms.sign-name}")
    private String signName;

    public boolean sendVerifyCode(String phoneNumber, String code, String templateCode) {
        StaticCredentialProvider provider = StaticCredentialProvider.create(
                Credential.builder()
                        .accessKeyId(accessKeyId)
                        .accessKeySecret(accessKeySecret)
                        .build()
        );

        try (AsyncClient client = AsyncClient.builder()
                .credentialsProvider(provider)
                .overrideConfiguration(ClientOverrideConfiguration.create())
                .build()) {

            SendSmsVerifyCodeRequest request = SendSmsVerifyCodeRequest.builder()
                    .phoneNumber(phoneNumber)
                    .signName(signName)
                    .templateCode(templateCode)
                    .templateParam(String.format("{\"code\":\"%s\",\"min\":\"5\"}", code))
                    .build();

            CompletableFuture<SendSmsVerifyCodeResponse> futureResponse =
                    client.sendSmsVerifyCode(request);
            SendSmsVerifyCodeResponse response = futureResponse.get();

            if ("OK".equals(response.getBody().getCode())) {
                logger.info("验证码发送成功，手机号：{}", phoneNumber);
                return true;
            } else {
                logger.error("验证码发送失败，手机号：{}，错误：{}", phoneNumber,
                        response.getBody().getMessage());
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("发送验证码异常，手机号：{}", phoneNumber, e);
            return false;
        }
    }
}