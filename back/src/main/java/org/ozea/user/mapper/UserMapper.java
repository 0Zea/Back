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

    void updateUser(User user);

    void updateKakaoAccessToken(@Param("email") String email,
                                @Param("kakaoAccessToken") String kakaoAccessToken);

    boolean checkEmail(@Param("email") String email);

    void deleteUser(UUID userId);
    void deleteUserPoints(UUID userId);
    void deleteUserGoals(UUID userId);
    void deleteUserInquiries(UUID userId);
    void deleteUserQuiz(UUID userId);
}
