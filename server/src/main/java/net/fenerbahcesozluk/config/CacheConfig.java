package net.fenerbahcesozluk.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.addMixIn(PageImpl.class, PageImplMixin.class);
    objectMapper.activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance,
        ObjectMapper.DefaultTyping.NON_FINAL,
        JsonTypeInfo.As.PROPERTY);

    GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(5))
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(serializer));

    // Stats cache with longer TTL (15 minutes)
    RedisCacheConfiguration statsConfig = config.entryTtl(Duration.ofMinutes(15));

    // Categories cache with 1 hour TTL
    RedisCacheConfiguration categoriesConfig = config.entryTtl(Duration.ofHours(24));

    // Transfermarkt cache with 24 hour TTL
    RedisCacheConfiguration transfermarktConfig = config.entryTtl(Duration.ofHours(24));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(config)
        .withCacheConfiguration("stats", statsConfig)
        .withCacheConfiguration("categories", categoriesConfig)
        .withCacheConfiguration("playerProfiles", transfermarktConfig)
        .withCacheConfiguration("clubProfiles", transfermarktConfig)
        .build();
  }
}
