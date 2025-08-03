package org.ozea.common.type;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.nio.ByteBuffer;
import java.sql.*;
import java.util.UUID;

@MappedJdbcTypes(JdbcType.BINARY)
@MappedTypes(UUID.class)
public class UUIDTypeHandler implements TypeHandler<UUID> {

    @Override
    public void setParameter(PreparedStatement ps, int i, UUID uuid, JdbcType jdbcType) throws SQLException {
        if (uuid == null) {
            ps.setNull(i, Types.BINARY);
        } else {
            ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
            bb.putLong(uuid.getMostSignificantBits());
            bb.putLong(uuid.getLeastSignificantBits());
            ps.setBytes(i, bb.array());
        }
    }

    @Override
    public UUID getResult(ResultSet rs, String columnName) throws SQLException {
        byte[] bytes = rs.getBytes(columnName);
        return toUUID(bytes);
    }

    @Override
    public UUID getResult(ResultSet rs, int columnIndex) throws SQLException {
        byte[] bytes = rs.getBytes(columnIndex);
        return toUUID(bytes);
    }

    @Override
    public UUID getResult(CallableStatement cs, int columnIndex) throws SQLException {
        byte[] bytes = cs.getBytes(columnIndex);
        return toUUID(bytes);
    }

    private UUID toUUID(byte[] bytes) {
        if (bytes == null || bytes.length != 16) return null;
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long high = bb.getLong();
        long low = bb.getLong();
        return new UUID(high, low);
    }
} 