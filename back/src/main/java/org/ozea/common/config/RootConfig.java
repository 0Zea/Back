package org.ozea.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Configuration
@PropertySource({"classpath:/application.properties"})
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
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
    private PoolingHttpClientConnectionManager cm;
    private CloseableHttpClient httpClient;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setPoolName("HikariPool-ozea");
        config.setMaximumPoolSize(15);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(10_000);
        config.setIdleTimeout(300_000);
        config.setMaxLifetime(1_800_000);
        config.setValidationTimeout(3_000);
        config.setLeakDetectionThreshold(10_000);

        Properties props = new Properties();
        props.setProperty("cachePrepStmts", "true");
        props.setProperty("useServerPrepStmts", "true");
        props.setProperty("prepStmtCacheSize", "250");
        props.setProperty("prepStmtCacheSqlLimit", "2048");
        props.setProperty("useUnicode", "true");
        props.setProperty("characterEncoding", "utf8");
        props.setProperty("tcpKeepAlive", "true");
        props.setProperty("rewriteBatchedStatements", "true");
        config.setDataSourceProperties(props);

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

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        javaTime.addSerializer(java.time.LocalDate.class, new com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer(df));
        javaTime.addDeserializer(java.time.LocalDate.class, new com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer(df));

        mapper.registerModule(javaTime);
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public RestTemplate restTemplate() {
        cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        cm.setDefaultMaxPerRoute(20);

        httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .evictIdleConnections(30, TimeUnit.SECONDS)
                .disableCookieManagement()
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(10_000);
        factory.setConnectionRequestTimeout(3_000);

        return new RestTemplate(factory);
    }
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer cfg = new PropertySourcesPlaceholderConfigurer();
        cfg.setIgnoreUnresolvablePlaceholders(true);
        return cfg;
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (httpClient != null) httpClient.close();
        } catch (Exception ignore) {}
        try {
            if (cm != null) cm.close();
        } catch (Exception ignore) {}
        try {
            Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
            java.lang.reflect.Method method = Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread")
                    .getMethod("checkedShutdown");
            method.invoke(null);
        } catch (Exception e) {
        }
    }
}