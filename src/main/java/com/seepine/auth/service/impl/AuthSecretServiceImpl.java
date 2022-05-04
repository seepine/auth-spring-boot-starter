package com.seepine.auth.service.impl;

import com.seepine.auth.entity.asymmetric.RSA;
import com.seepine.auth.enums.AuthExceptionType;
import com.seepine.auth.exception.AuthException;
import com.seepine.auth.exception.RSAException;
import com.seepine.auth.properties.SecretProperties;
import com.seepine.auth.service.AuthSecretService;
import com.seepine.auth.util.CurrentTimeMillisClock;
import com.seepine.auth.util.HttpServletUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class AuthSecretServiceImpl implements AuthSecretService {
  SecretProperties secretProperties;
  RSA rsa;
  RSA oldRsa;

  public AuthSecretServiceImpl(SecretProperties secretProperties) throws RSAException {
    this.secretProperties = secretProperties;
    if (secretProperties.getRsaPrivateKey() == null) {
      return;
    }
    try {
      rsa = new RSA(null, secretProperties.getRsaPrivateKey());
      if (secretProperties.getOldRsaPrivateKey() != null) {
        oldRsa = new RSA(null, secretProperties.getOldRsaPrivateKey());
      }
    } catch (Exception e) {
      throw new RSAException("初始化RSA失败，请检查rsa私钥是否正确");
    }
  }

  @Override
  public void verify(String secretValue) throws AuthException {
    String origin;
    try {
      origin = rsa.privateDecrypt(secretValue);
    } catch (RSAException e) {
      if (oldRsa == null) {
        throw new AuthException(AuthExceptionType.INVALID_SECRET);
      }
      try {
        origin = rsa.privateDecrypt(secretValue);
      } catch (RSAException e2) {
        throw new AuthException(AuthExceptionType.INVALID_SECRET);
      }
    }
    long originTime;
    try {
      originTime = Long.parseLong(origin);
    } catch (Exception e) {
      throw new AuthException(AuthExceptionType.INVALID_SECRET);
    }
    long nowTime = CurrentTimeMillisClock.now();
    HttpServletRequest httpServletRequest = HttpServletUtil.getHttpRequest();
    if (httpServletRequest != null) {
      log.debug(
          "auth secret verify [{}] originTime:{},nowTime：{}",
          httpServletRequest.getRequestURI(),
          origin,
          nowTime);
    }
    if (Math.abs(nowTime - originTime) > secretProperties.getTimeout() * 1000L) {
      throw new AuthException(AuthExceptionType.EXPIRED_SECRET);
    }
  }
}
