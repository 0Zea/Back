package org.ozea.common.mybatis.type;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.time.LocalTime;

public class LocalTimeCompatTypeHandler extends BaseTypeHandler<LocalTime> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalTime parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setTime(i, Time.valueOf(parameter));
    }

    @Override
    public LocalTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Time t = rs.getTime(columnName);
        return t != null ? t.toLocalTime() : null;
    }

    @Override
    public LocalTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Time t = rs.getTime(columnIndex);
        return t != null ? t.toLocalTime() : null;
    }

    @Override
    public LocalTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Time t = cs.getTime(columnIndex);
        return t != null ? t.toLocalTime() : null;
    }
}