package com.seepine.auth.service;

import com.seepine.auth.exception.AuthException;

/**
 * secret 验证接口
 *
 * @author seepine
 */
public interface AuthSecretService {
  void verify(String secretValue) throws AuthException;
}
