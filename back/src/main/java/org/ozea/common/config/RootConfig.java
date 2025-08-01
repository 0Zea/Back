package org.ozea.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Spring의 Root Application Context를 설정하는 클래스.
 * 데이터베이스 연결, 트랜잭션 관리 등 백엔드 관련 설정을 담당합니다.
 */
@Configuration
@PropertySource({"classpath:/application.properties"}) // application.properties 파일의 설정을 불러옵니다.
@Slf4j
@EnableTransactionManagement // 어노테이션 기반의 트랜잭션 관리를 활성화합니다.
@ComponentScan(basePackages = {"org.ozea"}) // org.ozea 패키지 내의 컴포넌트들을 스캔하여 빈으로 등록합니다.
@MapperScan(basePackages = {"org.ozea.user.mapper", "org.ozea.goal.mapper", "org.ozea.inquiry.mapper", "org.ozea.asset.mapper","org.ozea.notice.mapper", "org.ozea.point.mapper", "org.ozea.bank.mapper", "org.ozea.product.mapper", "org.ozea.term.mapper"}) // 도메인별 매퍼 패키지들을 스캔합니다.
@EnableAspectJAutoProxy
public class RootConfig {
    @Value("${jdbc.driver}") String driver;
    @Value("${jdbc.url}") String url;
    @Value("${jdbc.username}") String username;
    @Value("${jdbc.password}") String password;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        // MySQL 연결 정리 스레드 타이밍 이슈 해결을 위한 HikariCP 설정
        config.setLeakDetectionThreshold(60000); // 60초
        config.setConnectionTimeout(30000); // 30초
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setIdleTimeout(300000); // 5분
        config.setMaxLifetime(1200000); // 20분
        config.setValidationTimeout(5000); // 5초

        HikariDataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }

    @Autowired
    ApplicationContext applicationContext;

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setConfigLocation(
                applicationContext.getResource("classpath:/mybatis-config.xml")); // mybatis-config.xml 파일 위치를 설정합니다.
        sqlSessionFactory.setDataSource(dataSource()); // 데이터 소스를 설정합니다.
        return (SqlSessionFactory) sqlSessionFactory.getObject();
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // JavaTimeModule 등록
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // LocalDateTime 직렬화 형식 설정 (ISO 문자열 형식)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));

        // LocalDateTime 역직렬화 설정도 추가
        javaTimeModule.addDeserializer(LocalDateTime.class,
            new com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer(formatter));

        // LocalDate는 JavaTimeModule에서 기본적으로 ISO 형식으로 처리됨

        objectMapper.registerModule(javaTimeModule);

        // 날짜/시간 관련 설정
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return objectMapper;
    }

    @PreDestroy
    public void cleanup() {
        log.info("애플리케이션 종료 시 MySQL 연결 정리 스레드 종료 시작");
        try {
            // MySQL AbandonedConnectionCleanupThread 강제 종료
            Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
            java.lang.reflect.Method method = Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread")
                    .getMethod("checkedShutdown");
            method.invoke(null);
            log.info("MySQL 연결 정리 스레드가 성공적으로 종료되었습니다.");
        } catch (Exception e) {
            log.warn("MySQL 연결 정리 스레드 종료 중 예외 발생: {}", e.getMessage());
        }
    }
}