package com.example.lotterysystem.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    int countByMail(@Param("email") String email);

    int countByPhone(@Param("phoneNumber") String phoneNumber);
}
