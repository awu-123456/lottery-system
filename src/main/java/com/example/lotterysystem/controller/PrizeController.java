package com.example.lotterysystem.controller;

import com.example.lotterysystem.common.polo.CommonResult;
import com.example.lotterysystem.controller.param.CreatePrizeParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class PrizeController {

    public CommonResult<Long> createPrize(@Validated @RequestPart() CreatePrizeParam param,
                                          @RequestPart() MultipartFile picFile) {

    }
}
