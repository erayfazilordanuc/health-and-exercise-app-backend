package exercise.Configuration;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class CacheConfig {
  @Bean
  public Caffeine<Object, Object> caffeineConf() {
    return Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(5));
  }

  @Bean
  public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
    CaffeineCacheManager mgr = new CaffeineCacheManager("codes");
    mgr.setCaffeine(caffeine);
    return mgr;
  }
}