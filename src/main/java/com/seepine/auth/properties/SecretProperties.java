package com.seepine.auth.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

@Data
@ConfigurationProperties(prefix = "auth.secret")
public class SecretProperties {
  /** 是否开启接口加密 */
  Boolean enabled = Boolean.FALSE;
  /** 请求头参数 */
  String header = "secret";
  /** rsa私钥 */
  String rsaPrivateKey;
  /** rsa旧私钥,当rsaPrivateKey解密失败时，将会使用oldRsaPrivateKey进行解密 */
  String oldRsaPrivateKey;
  /** 允许超时毫秒数，默认3分钟 */
  Integer timeout = 3 * 60;
  /** 是否默认拦截所有接口 */
  Boolean defaultAll = Boolean.FALSE;
  /** 拦截器排除的pathPatterns */
  String[] excludePathPatterns = new String[] {};
  /** 拦截器的order */
  Integer interceptorOrder = Ordered.HIGHEST_PRECEDENCE;
}
