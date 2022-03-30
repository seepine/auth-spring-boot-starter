package com.seepine.auth.config;

import com.seepine.auth.entity.AuthProperties;
import com.seepine.auth.interceptor.AuthenticationInterceptor;
import com.seepine.auth.interceptor.PermissionInterceptor;
import com.seepine.auth.interceptor.SecretInterceptor;
import com.seepine.auth.service.AuthSecretService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author seepine
 */
@Configuration
@DependsOn("authProperties")
@AutoConfigureAfter(ServiceAutoConfigurer.class)
public class InterceptorConfig implements WebMvcConfigurer {
  @Resource private AuthProperties authProperties;
  @Resource private AuthSecretService secretService;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 拦截@Secret注解
    if (authProperties.isEnableSecret()) {
      registry
          .addInterceptor(new SecretInterceptor(authProperties, secretService))
          .addPathPatterns("/**")
          .excludePathPatterns("/error")
          .excludePathPatterns(authProperties.getExcludeSecretPath())
          .order(authProperties.getInterceptorOrder());
    }
    // 拦截非@Expose注解
    if (authProperties.isEnableAuth()) {
      registry
          .addInterceptor(new AuthenticationInterceptor(authProperties))
          .addPathPatterns("/**")
          .excludePathPatterns("/error")
          .excludePathPatterns(authProperties.getExcludePath())
          .order(authProperties.getInterceptorOrder() + 100);
    }
    // 拦截@Permission注解
    registry
        .addInterceptor(new PermissionInterceptor())
        .addPathPatterns("/**")
        .order(authProperties.getInterceptorOrder() + 200);
  }
}
