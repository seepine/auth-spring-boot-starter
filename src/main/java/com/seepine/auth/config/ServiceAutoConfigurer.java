package com.seepine.auth.config;

import com.seepine.auth.AutoConfiguration;
import com.seepine.auth.exception.RSAException;
import com.seepine.auth.properties.SecretProperties;
import com.seepine.auth.service.AuthSecretService;
import com.seepine.auth.service.impl.AuthSecretServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author seepine
 */
@Slf4j
@Configuration
@AutoConfigureAfter(AutoConfiguration.class)
public class ServiceAutoConfigurer {

  @Resource private SecretProperties secretProperties;

  @Bean
  @ConditionalOnMissingBean(AuthSecretService.class)
  public AuthSecretService authSecretService() throws RSAException {
    return new AuthSecretServiceImpl(secretProperties);
  }
}
