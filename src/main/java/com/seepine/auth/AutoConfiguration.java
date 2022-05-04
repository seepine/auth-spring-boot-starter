package com.seepine.auth;

import com.seepine.auth.properties.AuthProperties;
import com.seepine.auth.properties.SecretProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AuthProperties.class, SecretProperties.class})
public class AutoConfiguration {}
