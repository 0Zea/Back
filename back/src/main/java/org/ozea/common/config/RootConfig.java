package org.ozea.common.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.time.format.DateTimeFormatter;
@Configuration
@PropertySource({"classpath:/application.properties"})
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableScheduling
@MapperScan(basePackages = {
        "org.ozea.user.mapper","org.ozea.goal.mapper","org.ozea.inquiry.mapper",
        "org.ozea.asset.mapper","org.ozea.notice.mapper","org.ozea.point.mapper",
        "org.ozea.bank.mapper","org.ozea.product.mapper","org.ozea.term.mapper",
        "org.ozea.quiz.mapper","org.ozea.api.allaccount.mapper" , "org.ozea.api.taxkakaoouth.mapper",
        "org.ozea.taxinfo.mapper"
})
@ComponentScan(
        basePackages = "org.ozea",
        excludeFilters = @ComponentScan.Filter(org.springframework.stereotype.Controller.class)
)
@Import({RedisConfig.class})
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
        config.setLeakDetectionThreshold(60000);
        config.setConnectionTimeout(30000);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1200000);
        config.setValidationTimeout(5000);
        return new HikariDataSource(config);
    }
    @Autowired
    ApplicationContext applicationContext;

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setConfigLocation(
                applicationContext.getResource("classpath:/mybatis-config.xml"));
        sqlSessionFactory.setDataSource(dataSource());
        return (SqlSessionFactory) sqlSessionFactory.getObject();
    }
    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Primary
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        JavaTimeModule javaTime = new JavaTimeModule();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        javaTime.addSerializer(java.time.LocalDateTime.class, new LocalDateTimeSerializer(dtf));
        javaTime.addDeserializer(java.time.LocalDateTime.class,
                new com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer(dtf));

        java.time.format.DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        javaTime.addSerializer(java.time.LocalDate.class, new com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer(df));
        javaTime.addDeserializer(java.time.LocalDate.class, new com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer(df));

        mapper.registerModule(javaTime);
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    @PreDestroy
    public void cleanup() {
        try {
            Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
            java.lang.reflect.Method method = Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread")
                    .getMethod("checkedShutdown");
            method.invoke(null);
        } catch (Exception e) {
        }
    }
}
