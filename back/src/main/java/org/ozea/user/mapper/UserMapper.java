package org.ozea.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.ozea.user.domain.User;

import java.util.UUID;

/**
 * 사용자(User) 데이터베이스 연동을 위한 MyBatis 매퍼 인터페이스입니다.
 * SQL 쿼리는 src/main/resources/mapper/UserMapper.xml 파일에 정의되어 있습니다.
 */
@Mapper // MyBatis 매퍼 인터페이스임을 나타냅니다.
public interface UserMapper {
    /**
     * 이메일을 기준으로 사용자 정보를 조회합니다.
     * @param email 조회할 사용자의 이메일
     * @return 조회된 User 객체, 없으면 null
     */
    User getUserByEmail(String email);

    /**
     * 새로운 사용자 정보를 데이터베이스에 삽입합니다.
     * @param user 삽입할 User 객체
     */
    void insertUser(User user);

    /**
     * 이메일만 저장하는 사용자 삽입 메서드 (카카오 최초 로그인 시 사용)
     * @param user 삽입할 User 객체 (userId, email만 필수)
     */
    void insertUserWithEmail(User user);

    /**
     * 사용자 정보를 업데이트하는 메서드 (추가 정보 입력 후 사용)
     * @param user 업데이트할 User 객체
     */
    void updateUser(User user);


    User findById(UUID userId);

    // 이메일 중복 확인
    boolean checkEmail(String email);

    default User findByEmail(String email) {
        return null;
    }
} 