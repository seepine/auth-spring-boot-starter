package com.seepine.auth.service.impl;

import com.seepine.auth.entity.AuthProperties;
import com.seepine.auth.entity.asymmetric.RSA;
import com.seepine.auth.enums.AuthExceptionType;
import com.seepine.auth.exception.AuthException;
import com.seepine.auth.exception.RSAException;
import com.seepine.auth.service.AuthSecretService;
import com.seepine.auth.util.CurrentTimeMillisClock;
import com.seepine.auth.util.HttpServletUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class AuthSecretServiceImpl implements AuthSecretService {
  AuthProperties authProperties;
  RSA rsa;
  RSA oldRsa;

  public AuthSecretServiceImpl(AuthProperties authProperties) throws RSAException {
    this.authProperties = authProperties;
    if (authProperties.isEnableSecret()) {
      if (authProperties.getRsaPrivateKey() == null) {
        throw new RSAException("开启secret且未实现SecretService时，必须设置rsaPrivateKey");
      }
      rsa = new RSA(null, authProperties.getRsaPrivateKey());
      if (authProperties.getRsaOldPrivateKey() != null) {
        oldRsa = new RSA(null, authProperties.getRsaOldPrivateKey());
      }
    }
  }

  @Override
  public boolean verify(String secretValue) throws AuthException {
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
    if (Math.abs(nowTime - originTime) > authProperties.getSecretTimeout()) {
      throw new AuthException(AuthExceptionType.EXPIRED_SECRET);
    }
    return true;
  }
}
