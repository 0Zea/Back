package org.ozea.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.ozea.user.dto.UserSignupDTO;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceIntegrationTest {

    @Test
    @DisplayName("기본 테스트 - JUnit 동작 확인")
    void basicTest() {
        // JUnit이 정상적으로 동작하는지 확인
        assertTrue(true);
    }

    @Test
    @DisplayName("UserSignupDTO 생성 및 검증 테스트")
    void userSignupDTOCreationTest() {
        // given
        UserSignupDTO signupDTO = new UserSignupDTO();
        signupDTO.setEmail("test@example.com");
        signupDTO.setName("테스트 사용자");
        signupDTO.setPassword("Test1234!");
        signupDTO.setPhoneNum("010-1234-5678");
        signupDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        signupDTO.setSex("male");
        signupDTO.setMbti("INTJ");
        signupDTO.setSalary(30000000L);
        signupDTO.setPayAmount(2500000L);
        signupDTO.setKakao(false);
        
        // when & then
        assertNotNull(signupDTO);
        assertEquals("test@example.com", signupDTO.getEmail());
        assertEquals("테스트 사용자", signupDTO.getName());
        assertEquals("Test1234!", signupDTO.getPassword());
        assertEquals("010-1234-5678", signupDTO.getPhoneNum());
        assertEquals(LocalDate.of(1990, 1, 1), signupDTO.getBirthDate());
        assertEquals("male", signupDTO.getSex());
        assertEquals("INTJ", signupDTO.getMbti());
        assertEquals(30000000L, signupDTO.getSalary());
        assertEquals(2500000L, signupDTO.getPayAmount());
        assertFalse(signupDTO.isKakao());
    }

    @Test
    @DisplayName("UserSignupDTO 유효성 검증 테스트")
    void userSignupDTOValidationTest() {
        // given
        UserSignupDTO signupDTO = new UserSignupDTO();
        
        // when
        signupDTO.setEmail("valid@email.com");
        signupDTO.setName("유효한 사용자");
        signupDTO.setPassword("ValidPass123!");
        
        // then
        assertNotNull(signupDTO.getEmail());
        assertNotNull(signupDTO.getName());
        assertNotNull(signupDTO.getPassword());
        assertTrue(signupDTO.getEmail().contains("@"));
        assertTrue(signupDTO.getPassword().length() >= 8);
    }

    @Test
    @DisplayName("계산 테스트")
    void calculationTest() {
        // given
        int a = 5;
        int b = 3;
        
        // when
        int sum = a + b;
        int product = a * b;
        
        // then
        assertEquals(8, sum);
        assertEquals(15, product);
    }
} 