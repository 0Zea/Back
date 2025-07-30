package org.ozea.common.config;

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
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;

/**
 * Spring의 Root Application Context를 설정하는 클래스.
 * 데이터베이스 연결, 트랜잭션 관리 등 백엔드 관련 설정을 담당합니다.
 */
@Configuration
@PropertySource({"classpath:/application.properties"}) // application.properties 파일의 설정을 불러옵니다.
@Slf4j
@EnableTransactionManagement // 어노테이션 기반의 트랜잭션 관리를 활성화합니다.
@ComponentScan(basePackages = {"org.ozea"}) // org.ozea 패키지 내의 컴포넌트들을 스캔하여 빈으로 등록합니다.
@MapperScan(basePackages = {"org.ozea.user.mapper", "org.ozea.goal.mapper", "org.ozea.inquiry.mapper", "org.ozea.asset.mapper","org.ozea.notice.mapper"}) // 도메인별 매퍼 패키지들을 스캔합니다.
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
    public DataSourceTransactionManager transactionManager(){
        DataSourceTransactionManager manager = new DataSourceTransactionManager(dataSource());
        return manager;
    }

}