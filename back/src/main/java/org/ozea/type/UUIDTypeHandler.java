package org.ozea.type;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.nio.ByteBuffer;
import java.sql.*;
import java.util.UUID;

/**
 * MyBatis에서 UUID 타입을 데이터베이스의 BINARY(16) 또는 VARBINARY 타입으로 매핑하기 위한 TypeHandler입니다.
 * 이 핸들러는 UUID 객체를 바이트 배열로 변환하여 데이터베이스에 저장하고,
 * 데이터베이스에서 읽어온 바이트 배열을 UUID 객체로 변환합니다.
 */
public class UUIDTypeHandler implements TypeHandler<UUID> {

    /**
     * PreparedStatement에 파라미터를 설정할 때 호출됩니다.
     * UUID를 16바이트 배열로 변환하여 설정합니다.
     * @param ps PreparedStatement 객체
     * @param i 파라미터 인덱스
     * @param uuid 설정할 UUID 값
     * @param jdbcType JDBC 타입
     * @throws SQLException SQL 예외 발생 시
     */
    @Override
    public void setParameter(PreparedStatement ps, int i, UUID uuid, JdbcType jdbcType) throws SQLException {
        if (uuid == null) {
            ps.setNull(i, Types.BINARY);
        } else {
            ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
            bb.putLong(uuid.getMostSignificantBits()); // UUID의 상위 8바이트
            bb.putLong(uuid.getLeastSignificantBits()); // UUID의 하위 8바이트
            ps.setBytes(i, bb.array()); // 16바이트 배열로 변환하여 설정
        }
    }

    /**
     * ResultSet에서 컬럼 이름을 사용하여 결과를 읽어올 때 호출됩니다.
     * 읽어온 바이트 배열을 UUID 객체로 변환합니다.
     * @param rs ResultSet 객체
     * @param columnName 컬럼 이름
     * @return 읽어온 UUID 값
     * @throws SQLException SQL 예외 발생 시
     */
    @Override
    public UUID getResult(ResultSet rs, String columnName) throws SQLException {
        byte[] bytes = rs.getBytes(columnName);
        return toUUID(bytes);
    }

    /**
     * ResultSet에서 컬럼 인덱스를 사용하여 결과를 읽어올 때 호출됩니다.
     * 읽어온 바이트 배열을 UUID 객체로 변환합니다.
     * @param rs ResultSet 객체
     * @param columnIndex 컬럼 인덱스
     * @return 읽어온 UUID 값
     * @throws SQLException SQL 예외 발생 시
     */
    @Override
    public UUID getResult(ResultSet rs, int columnIndex) throws SQLException {
        byte[] bytes = rs.getBytes(columnIndex);
        return toUUID(bytes);
    }

    /**
     * CallableStatement에서 결과를 읽어올 때 호출됩니다.
     * 읽어온 바이트 배열을 UUID 객체로 변환합니다.
     * @param cs CallableStatement 객체
     * @param columnIndex 컬럼 인덱스
     * @return 읽어온 UUID 값
     * @throws SQLException SQL 예외 발생 시
     */
    @Override
    public UUID getResult(CallableStatement cs, int columnIndex) throws SQLException {
        byte[] bytes = cs.getBytes(columnIndex);
        return toUUID(bytes);
    }

    /**
     * 바이트 배열을 UUID 객체로 변환합니다.
     * @param bytes 변환할 바이트 배열
     * @return 변환된 UUID 객체, 또는 null (바이트 배열이 null이거나 길이가 16이 아닌 경우)
     */
    private UUID toUUID(byte[] bytes) {
        if (bytes == null || bytes.length != 16) return null;
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long high = bb.getLong(); // 상위 8바이트
        long low = bb.getLong(); // 하위 8바이트
        return new UUID(high, low);
    }

}
