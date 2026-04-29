package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.Encrypt;
import com.example.lotterysystem.dao.dataobject.UserDO;
import jakarta.validation.constraints.NotBlank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    int countByMail(@Param("email") String email);

    int countByPhone(@Param("phoneNumber") Encrypt phoneNumber);

    void insert(UserDO userDO);

    UserDO selectByMail(@Param("email") String email);

    UserDO selectByPhoneNumber(@Param("phoneNumber") Encrypt phoneNumber);

    List<UserDO> selectUserListByIdentity(@Param("identity") String identity);

    List<Long> selectExistById(@Param("items") List<Long> userIds);

    List<UserDO> batchSelectByIds(@Param("items") List<Long> ids);
}
