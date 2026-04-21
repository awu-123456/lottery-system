package com.example.lotterysystem.service;

public interface VerificationCodeService {

    void sendVerificationCode(String phoneNumber);

    String getVerificationCode(String phoneNumber);
}
