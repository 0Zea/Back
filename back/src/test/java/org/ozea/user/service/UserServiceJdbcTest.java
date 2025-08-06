package org.ozea.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.ozea.user.dto.UserSignupDTO;
import org.ozea.user.domain.User;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceJdbcTest {

    @Test
    @DisplayName("User 도메인 객체 생성 테스트")
    void userDomainCreationTest() {
        // given
        User user = new User();
        UUID userId = UUID.randomUUID();
        
        // when
        user.setUserId(userId);
        user.setName("테스트 사용자");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setPhoneNum("010-1234-5678");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setSex("male");
        user.setMbti("INTJ");
        user.setSalary(30000000L);
        user.setPayAmount(2500000L);
        user.setRole("USER");
        
        // then
        assertNotNull(user);
        assertEquals(userId, user.getUserId());
        assertEquals("테스트 사용자", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("encodedPassword", user.getPassword());
        assertEquals("010-1234-5678", user.getPhoneNum());
        assertEquals(LocalDate.of(1990, 1, 1), user.getBirthDate());
        assertEquals("male", user.getSex());
        assertEquals("INTJ", user.getMbti());
        assertEquals(30000000L, user.getSalary());
        assertEquals(2500000L, user.getPayAmount());
        assertEquals("USER", user.getRole());
    }

    @Test
    @DisplayName("UserSignupDTO to User 변환 테스트")
    void userSignupDTOToUserConversionTest() {
        // given
        UserSignupDTO signupDTO = new UserSignupDTO();
        signupDTO.setEmail("conversion@test.com");
        signupDTO.setName("변환 테스트");
        signupDTO.setPassword("TestPass123!");
        signupDTO.setPhoneNum("010-9876-5432");
        signupDTO.setBirthDate(LocalDate.of(1995, 5, 15));
        signupDTO.setSex("female");
        signupDTO.setMbti("ENFP");
        signupDTO.setSalary(35000000L);
        signupDTO.setPayAmount(3000000L);
        signupDTO.setKakao(false);
        
        // when
        User user = signupDTO.toVO();
        
        // then
        assertNotNull(user);
        assertEquals(signupDTO.getEmail(), user.getEmail());
        assertEquals(signupDTO.getName(), user.getName());
        assertEquals(signupDTO.getPhoneNum(), user.getPhoneNum());
        assertEquals(signupDTO.getBirthDate(), user.getBirthDate());
        assertEquals(signupDTO.getSex(), user.getSex());
        assertEquals(signupDTO.getMbti(), user.getMbti());
        assertEquals(signupDTO.getSalary(), user.getSalary());
        assertEquals(signupDTO.getPayAmount(), user.getPayAmount());
    }

    @Test
    @DisplayName("UUID 생성 및 검증 테스트")
    void uuidGenerationTest() {
        // given
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        
        // when & then
        assertNotNull(uuid1);
        assertNotNull(uuid2);
        assertNotEquals(uuid1, uuid2);
        assertEquals(36, uuid1.toString().length());
        assertEquals(36, uuid2.toString().length());
    }

    @Test
    @DisplayName("LocalDate 처리 테스트")
    void localDateHandlingTest() {
        // given
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        LocalDate currentDate = LocalDate.now();
        
        // when
        int age = currentDate.getYear() - birthDate.getYear();
        
        // then
        assertNotNull(birthDate);
        assertNotNull(currentDate);
        assertTrue(age > 0);
        assertTrue(currentDate.isAfter(birthDate));
    }
} 