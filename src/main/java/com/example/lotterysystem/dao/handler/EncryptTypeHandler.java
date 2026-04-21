package com.example.lotterysystem.dao.handler;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.example.lotterysystem.dao.dataobject.Encrypt;
import org.apache.ibatis.type.*;
import org.springframework.util.StringUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(Encrypt.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class EncryptTypeHandler extends BaseTypeHandler<Encrypt> {
    private final byte[] KEY = "123456789abcdefg".getBytes();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Encrypt parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null || parameter.getValue() == null) {
            ps.setString(i, null);
            return;
        }
        System.out.println("加密的内容：" + parameter.getValue());
        AES aes = SecureUtil.aes(KEY);
        String str = aes.encryptHex(parameter.getValue());
        ps.setString(i, str);
    }

    @Override
    public Encrypt getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return decrypt(rs.getString(columnName));
    }

    @Override
    public Encrypt getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return decrypt(rs.getString(columnIndex));
    }

    @Override
    public Encrypt getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decrypt(cs.getString(columnIndex));
    }

    private Encrypt decrypt(String str) {
        if(!StringUtils.hasText(str)) {
            return null;
        }
        return new Encrypt(SecureUtil.aes(KEY).decryptStr(str));
    }
}
