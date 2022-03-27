package com.seepine.auth.config;

import com.seepine.auth.entity.AuthProperties;
import com.seepine.auth.exception.RSAException;
import com.seepine.auth.service.AuthSecretService;
import com.seepine.auth.service.impl.AuthSecretServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.Resource;

/**
 * @author seepine
 */
@Slf4j
@Configuration
@DependsOn("authProperties")
@AutoConfigureAfter(AuthAutoConfigurer.class)
public class ServiceAutoConfigurer {

  @Resource private AuthProperties authProperties;

  @Bean
  @ConditionalOnMissingBean(AuthSecretService.class)
  public AuthSecretService authSecretService() throws RSAException {
    return new AuthSecretServiceImpl(authProperties);
  }
}
