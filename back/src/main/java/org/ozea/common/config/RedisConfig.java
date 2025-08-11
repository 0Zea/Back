package org.ozea.common.config;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;


@Configuration
@EnableCaching
@PropertySource("classpath:redis.properties")
@Slf4j
public class RedisConfig extends CachingConfigurerSupport {

    @Value("${redis.host:127.0.0.1}")
    private String host;

    @Value("${redis.port:6379}")
    private int port;

    @Value("${redis.password:}")
    private String password;

    @Value("${redis.database:0}")
    private int database;

    @Value("${redis.timeout:2000}") // ms
    private int timeoutMs;

    @Value("${redis.pool.maxTotal:50}")
    private int maxTotal;

    @Value("${redis.pool.maxIdle:16}")
    private int maxIdle;

    @Value("${redis.pool.minIdle:4}")
    private int minIdle;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration(host, port);
        standalone.setDatabase(database);
        if (password != null && !password.isEmpty()) {
            standalone.setPassword(RedisPassword.of(password));
        }

        // Pool
        JedisPoolConfig pool = new JedisPoolConfig();
        pool.setMaxTotal(maxTotal);
        pool.setMaxIdle(maxIdle);
        pool.setMinIdle(minIdle);
        pool.setTestOnBorrow(true);

        JedisClientConfiguration.JedisClientConfigurationBuilder builder =
                JedisClientConfiguration.builder();

        builder.usePooling().poolConfig(pool);
        builder.connectTimeout(Duration.ofMillis(timeoutMs));
        builder.readTimeout(Duration.ofMillis(timeoutMs));

        JedisClientConfiguration client = builder.build();
        return new JedisConnectionFactory(standalone, client);
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory cf) {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        var json = new GenericJackson2JsonRedisSerializer(om);

        var base = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(json))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> per = new HashMap<>();
        per.put("userByEmail", base.entryTtl(Duration.ofMinutes(10)));
        per.put("userById",    base.entryTtl(Duration.ofMinutes(10)));
        per.put("product:detail", base.entryTtl(Duration.ofMinutes(30)));
        per.put("product:list",   base.entryTtl(Duration.ofMinutes(5)));
        per.put("product:filter", base.entryTtl(Duration.ofMinutes(3)));

        return RedisCacheManager.builder(cf)
                .cacheDefaults(base)
                .withInitialCacheConfigurations(per)
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer keySer = new StringRedisSerializer();

        ObjectMapper om = new ObjectMapper();
        om.findAndRegisterModules();
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        GenericJackson2JsonRedisSerializer valSer = new GenericJackson2JsonRedisSerializer(om);

        template.setKeySerializer(keySer);
        template.setHashKeySerializer(keySer);
        template.setValueSerializer(valSer);
        template.setHashValueSerializer(valSer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder sb = new StringBuilder();
                sb.append(target.getClass().getSimpleName()).append(":")
                        .append(method.getName());
                for (Object p : params) {
                    sb.append(":").append(String.valueOf(p));
                }
                String key = sb.toString();
                log.debug("KeyGenerator -> {}", key);
                return key;
            }
        };
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }
}