package org.ozea.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@Profile("test")
@ActiveProfiles("test")
public class TestConfig {
    // 테스트용 설정
    // 실제 데이터베이스 연결 없이 테스트할 수 있도록 설정
} 