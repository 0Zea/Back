package org.ozea.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ozea.user.domain.User;

import java.util.UUID;

@Mapper
public interface UserMapper {

    User findById(UUID userId);
    User getUserByEmail(String email);

    void insertUser(User user);
    void insertUserWithEmail(User user);

    /** 프로필 등 전체 업데이트 (명시적으로 쓸 때만) */
    void updateUser(User user);

    /** ✅ 카카오 액세스 토큰만 갱신하는 전용 메서드 */
    void updateKakaoAccessToken(@Param("email") String email,
                                @Param("kakaoAccessToken") String kakaoAccessToken);

    /** ✅ 이메일 존재 여부 */
    boolean checkEmail(@Param("email") String email);

    void deleteUser(UUID userId);
    void deleteUserPoints(UUID userId);
    void deleteUserGoals(UUID userId);
    void deleteUserInquiries(UUID userId);
    void deleteUserQuiz(UUID userId);
}