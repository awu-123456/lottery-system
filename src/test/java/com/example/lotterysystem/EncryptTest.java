package com.example.lotterysystem;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.symmetric.AES;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest()
public class EncryptTest {
    @Test
    void EncryptTest(){
        AES aes = SecureUtil.aes("123456789abcdefg".getBytes());
        String encrypted = aes.encryptHex("13800000003");
        System.out.println(encrypted);
    }
}
