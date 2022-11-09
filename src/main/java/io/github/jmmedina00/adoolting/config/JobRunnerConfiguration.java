package io.github.jmmedina00.adoolting.config;

import org.jobrunr.jobs.mappers.JobMapper;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.storage.nosql.redis.JedisRedisStorageProvider;
import org.jobrunr.utils.mapper.jackson.JacksonJsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

@Configuration
public class JobRunnerConfiguration {
  @Value("${spring.redis.host}")
  private String redisHost;

  @Value("${spring.redis.port}")
  private String redisPort;

  @Bean
  public StorageProvider storageProvider(JobMapper jobMapper) {
    JedisRedisStorageProvider provider = new JedisRedisStorageProvider(
      getJedisPool()
    );
    provider.setJobMapper(new JobMapper(new JacksonJsonMapper()));
    return provider;
  }

  private JedisPool getJedisPool() {
    return new JedisPool(redisHost, Integer.parseInt(redisPort));
  }
}
