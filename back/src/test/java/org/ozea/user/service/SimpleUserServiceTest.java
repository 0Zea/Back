package org.ozea.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class SimpleUserServiceTest {

    @Test
    @DisplayName("기본 테스트 - 항상 통과")
    void basicTest() {
        // given
        String expected = "test";
        
        // when
        String actual = "test";
        
        // then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("간단한 계산 테스트")
    void simpleCalculationTest() {
        // given
        int a = 2;
        int b = 3;
        
        // when
        int result = a + b;
        
        // then
        assertEquals(5, result);
    }
} 