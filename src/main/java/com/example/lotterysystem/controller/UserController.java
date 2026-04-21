package com.example.lotterysystem.controller;

import com.example.lotterysystem.common.errorcode.ControllerErrorCodeConstants;
import com.example.lotterysystem.common.exception.ControllerException;
import com.example.lotterysystem.common.polo.CommonResult;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.controller.param.ShortMessageLoginParam;
import com.example.lotterysystem.controller.param.UserPasswordLoginParam;
import com.example.lotterysystem.controller.param.UserRegisterParam;
import com.example.lotterysystem.controller.result.BaseUserInfoResult;
import com.example.lotterysystem.controller.result.UserLoginResult;
import com.example.lotterysystem.controller.result.UserRegisterResult;
import com.example.lotterysystem.service.UserService;
import com.example.lotterysystem.service.dto.UserDTO;
import com.example.lotterysystem.service.dto.UserLoginDTO;
import com.example.lotterysystem.service.dto.UserRegisterDTO;
import com.example.lotterysystem.service.enums.UserIdentityEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public CommonResult<UserRegisterResult> userRegister(@Validated @RequestBody UserRegisterParam param) {
        logger.info("userRegister UserRegisterParam:{}", JacksonUtil.writeValueAsString(param));
        UserRegisterDTO userRegisterDTO = userService.register(param);
        return CommonResult.success(convertToUserRegisterResult(userRegisterDTO));
    }

    @PostMapping("/password/login")
    public CommonResult<UserLoginResult> userPasswordLogin(@Validated @RequestBody UserPasswordLoginParam param) {
        logger.info("userPasswordLogin UserPasswordLoginParam:{}",
                JacksonUtil.writeValueAsString(param));
        UserLoginDTO userLoginDTO = userService.login(param);
        return CommonResult.success(convertToUserLoginResult(userLoginDTO));
    }



    @PostMapping("/message/login")
    public CommonResult<UserLoginResult> shortMessageLogin(@Validated @RequestBody ShortMessageLoginParam param) {
        logger.info("shortMessageLogin shortMessageLoginParam:{}",
                JacksonUtil.writeValueAsString(param));
        UserLoginDTO userLoginDTO = userService.login(param);
        return CommonResult.success(convertToUserLoginResult(userLoginDTO));
    }

    @RequestMapping("/base-user/find-list")
    public CommonResult<List<BaseUserInfoResult>> findBaseUserInfo(String identity) {
        logger.info("findBaseUserInfo BaseUserInfoResult:{}", JacksonUtil.writeValueAsString(identity));
        List<UserDTO> userDTOList = userService.findUserInfo(UserIdentityEnum.forName(identity));
        return CommonResult.success(convertToBaseUserInfoResult(userDTOList));
    }

    private List<BaseUserInfoResult> convertToBaseUserInfoResult(List<UserDTO> userDTOList) {

    }

    private UserLoginResult convertToUserLoginResult(UserLoginDTO userLoginDTO) {
        if(userLoginDTO == null) {
            throw new ControllerException(ControllerErrorCodeConstants.LOGIN_ERROR);
        }
        UserLoginResult userLoginResult = new UserLoginResult();
        userLoginResult.setToken(userLoginDTO.getToken());
        userLoginResult.setIdentity(userLoginDTO.getIdentity().name());
        return userLoginResult;
    }

    private UserRegisterResult convertToUserRegisterResult(UserRegisterDTO userRegisterDTO) {
        UserRegisterResult result = new UserRegisterResult();
        if(userRegisterDTO == null) {
            throw new ControllerException(ControllerErrorCodeConstants.REGISTER_ERROR);
        }
        result.setUserID(userRegisterDTO.getUserId());
        return result;
    }
}
