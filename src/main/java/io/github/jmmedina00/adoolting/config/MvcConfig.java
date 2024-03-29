package io.github.jmmedina00.adoolting.config;

import io.github.jmmedina00.adoolting.config.handler.LocaleUpdateInterceptor;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
@EnableRedisRepositories("io.github.jmmedina00.adoolting.repository.cache")
@EnableJpaRepositories(
  basePackages = "io.github.jmmedina00.adoolting.repository",
  excludeFilters = @ComponentScan.Filter(
    type = FilterType.ASPECTJ,
    pattern = "io.github.jmmedina00.adoolting.repository.cache.*"
  )
)
public class MvcConfig implements WebMvcConfigurer {
  @Autowired
  private LocaleUpdateInterceptor localeInterceptor;

  @Bean
  public LocaleResolver localeResolver() {
    SessionLocaleResolver resolver = new SessionLocaleResolver();
    resolver.setDefaultLocale(Locale.ENGLISH);
    return resolver;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
      .addResourceHandler("/cdn/**")
      .addResourceLocations("file:./data/cdn/");
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(localeInterceptor);
  }
}
