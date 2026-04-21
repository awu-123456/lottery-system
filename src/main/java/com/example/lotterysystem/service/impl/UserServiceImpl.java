package com.example.lotterysystem.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.JWTUtil;
import com.example.lotterysystem.common.utils.RegexUtil;
import com.example.lotterysystem.controller.param.ShortMessageLoginParam;
import com.example.lotterysystem.controller.param.UserLoginParam;
import com.example.lotterysystem.controller.param.UserPasswordLoginParam;
import com.example.lotterysystem.controller.param.UserRegisterParam;
import com.example.lotterysystem.dao.dataobject.Encrypt;
import com.example.lotterysystem.dao.dataobject.UserDO;
import com.example.lotterysystem.dao.mapper.UserMapper;
import com.example.lotterysystem.service.UserService;
import com.example.lotterysystem.service.dto.UserDTO;
import com.example.lotterysystem.service.dto.UserLoginDTO;
import com.example.lotterysystem.service.dto.UserRegisterDTO;
import com.example.lotterysystem.service.enums.UserIdentityEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private VerificationCodeServiceImpl verificationCodeService;

    @Override
    public List<UserDTO> findUserInfo(UserIdentityEnum identity) {
        String identityString = identity.name();
        List<UserDO> userDOList = userMapper.selectUserListByIdentity(identityString);
        List<UserDTO> userDTOList = userDOList.stream()
                .map(userDO -> {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setUserId(userDO.getId());
                    userDTO.setUserName(userDO.getUserName());
                    userDTO.setEmail(userDO.getEmail());
                    userDTO.setPhoneNumber(String.valueOf(userDO.getPhoneNumber()));
                    userDTO.setIdentity(UserIdentityEnum.forName(userDO.getIdentity()));
                    return userDTO;
                }).collect(Collectors.toList());
        return userDTOList;
    }

    @Override
    public UserLoginDTO login(UserLoginParam param) {
        UserLoginDTO userLoginDTO;
        if(param instanceof UserPasswordLoginParam loginParam) {
            userLoginDTO = loginByUserPassword(loginParam);
        } else if(param instanceof ShortMessageLoginParam loginParam) {
            userLoginDTO = loginByShortMessage(loginParam);
        } else {
            throw new ServiceException(ServiceErrorCodeConstants.LOGIN_INFO_NOT_EXIST);
        }
        return userLoginDTO;
    }

    private UserLoginDTO loginByShortMessage(ShortMessageLoginParam loginParam) {
        if(!RegexUtil.checkMobile(loginParam.getLoginMobile())) {
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_ERROR);
        }

        UserDO userDO = userMapper.selectByPhoneNumber(new Encrypt(loginParam.getLoginMobile()));
        if(userDO == null) {
            throw new ServiceException(ServiceErrorCodeConstants.USER_INFO_IS_EMPTY);
        } else if(StringUtils.hasText(loginParam.getMandatoryIdentity()) &&
                    !loginParam.getMandatoryIdentity().equals(userDO.getIdentity())) {
            throw new ServiceException(ServiceErrorCodeConstants.IDENTITY_ERROR);
        }

        String code = verificationCodeService.getVerificationCode(
                loginParam.getMandatoryIdentity());
        if(!loginParam.getVerificationCode().equals(code)) {
            throw new ServiceException(ServiceErrorCodeConstants.VERIFICATION_CODE_ERROR);
        }

        Map<String,Object> claim  = new HashMap<>();
        claim.put("id",userDO.getId());
        claim.put("identity",userDO.getIdentity());
        String token = JWTUtil.genJwt(claim);

        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setToken(token);
        userLoginDTO.setIdentity(UserIdentityEnum.forName(userDO.getIdentity()));
        return userLoginDTO;
    }

    private UserLoginDTO loginByUserPassword(UserPasswordLoginParam loginParam) {
        UserDO userDO = null;
        if(RegexUtil.checkMail(loginParam.getLoginName())) {
            userDO = userMapper.selectByMail(loginParam.getLoginName());
        } else if(RegexUtil.checkMobile(loginParam.getLoginName())) {
            userDO = userMapper.selectByPhoneNumber(new Encrypt(loginParam.getLoginName()));
        } else {
            throw new ServiceException((ServiceErrorCodeConstants.LOGIN_NOT_EXIST));
        }

        if(userDO == null) {
            throw new ServiceException(ServiceErrorCodeConstants.USER_INFO_IS_EMPTY);
        } else if(StringUtils.hasText(loginParam.getMandatoryIdentity()) &&
                    !loginParam.getMandatoryIdentity().equalsIgnoreCase(userDO.getIdentity())) {
            throw new ServiceException(ServiceErrorCodeConstants.IDENTITY_ERROR);
        } else if(!DigestUtil.sha256Hex(loginParam.getPassword())
                    .equals(userDO.getPassword())) {
            throw new ServiceException(ServiceErrorCodeConstants.PASSWORD_ERROR);
        }

        Map<String,Object> claim  = new HashMap<>();
        claim.put("id",userDO.getId());
        claim.put("identity",userDO.getIdentity());
        String token = JWTUtil.genJwt(claim);

        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setToken(token);
        userLoginDTO.setIdentity(UserIdentityEnum.forName(userDO.getIdentity()));
        return userLoginDTO;
    }

    @Override
    public UserRegisterDTO register(UserRegisterParam param) {
        checkRegisterInfo(param);

        UserDO userDO = new UserDO();
        userDO.setUserName(param.getName());
        userDO.setEmail(param.getMail());
        userDO.setPhoneNumber(new Encrypt(param.getPhoneNumber()));
        if(StringUtils.hasText(param.getPassword())) {
            userDO.setPassword(param.getPassword());
        }
        userDO.setIdentity(param.getIdentity());
        userMapper.insert(userDO);

        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setUserId(userDO.getId());
        return userRegisterDTO;
    }

    private void checkRegisterInfo(UserRegisterParam param) {
        if(param == null) {
            throw new ServiceException(ServiceErrorCodeConstants.REGISTER_INFO_IS_EMPTY);
        }
        if(!RegexUtil.checkMail(param.getMail())) {
            throw new ServiceException(ServiceErrorCodeConstants.MAIL_ERROR);
        }
        if(!RegexUtil.checkMobile(param.getPhoneNumber())) {
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_ERROR);
        }
        if(UserIdentityEnum.forName(param.getIdentity()) == null) {
            throw new ServiceException(ServiceErrorCodeConstants.IDENTITY_ERROR);
        }
        if(param.getIdentity().equalsIgnoreCase(UserIdentityEnum.ADMIN.name())
            && !StringUtils.hasText(param.getPassword())) {
            throw new ServiceException(ServiceErrorCodeConstants.PASSWORD_IS_EMPTY);
        }
        if (StringUtils.hasText(param.getPassword())
                && !RegexUtil.checkPassword(param.getPassword())) {
            throw new ServiceException(ServiceErrorCodeConstants.PASSWORD_ERROR);
        }
        if (checkMailUsed(param.getMail())) {
            throw new ServiceException(ServiceErrorCodeConstants.MAIL_USED);
        }
        if(checkPhoneNumberUsed(param.getPhoneNumber())) {
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_USED);
        }
    }

    private boolean checkPhoneNumberUsed(String phoneNumber) {
        int count = userMapper.countByPhone(new Encrypt(phoneNumber));
        return count > 0;
    }

    private boolean checkMailUsed(String mail) {
        int count = userMapper.countByMail(mail);
        return count > 0;
    }
}
