package com.seepine.auth.entity.asymmetric;

import com.seepine.auth.exception.RSAException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA加解密封装
 *
 * @author seepine
 */
public class RSA {
  private static final Base64.Encoder base64Encoder = Base64.getEncoder();
  private static final Base64.Decoder base64Decoder = Base64.getDecoder();

  private static final String RSA = "RSA";
  RSAPublicKey publicKey;
  String publicKeyBase64;

  RSAPrivateKey privateKey;
  String privateKeyBase64;

  int PRIVATE_DECODE_BLOCK_SIZE;
  int PRIVATE_ENCODE_BLOCK_SIZE;
  int PUBLIC_DECODE_BLOCK_SIZE;
  int PUBLIC_ENCODE_BLOCK_SIZE;

  public RSA() {
    genKeyPair();
  }

  public RSA(String publicKey, String privateKey) {
    this.publicKeyBase64 = publicKey;
    this.privateKeyBase64 = privateKey;
    initKey();
  }

  /**
   * 获取公钥base64
   *
   * @return 返回公钥base64
   */
  public String getPublicKey() {
    return this.publicKeyBase64;
  }

  /**
   * 获取私钥base64
   *
   * @return 返回私钥base64
   */
  public String getPrivateKey() {
    return this.privateKeyBase64;
  }

  /** 生成公私密钥 */
  private void genKeyPair() {
    try {
      KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(RSA);
      keyPairGen.initialize(2048, new SecureRandom());
      KeyPair keyPair = keyPairGen.generateKeyPair();
      publicKey = (RSAPublicKey) keyPair.getPublic();
      privateKey = (RSAPrivateKey) keyPair.getPrivate();
      publicKeyBase64 = base64Encoder.encodeToString(publicKey.getEncoded());
      privateKeyBase64 = base64Encoder.encodeToString((privateKey.getEncoded()));
      initCipher();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }

  private void initKey() {
    try {
      if (publicKeyBase64 != null) {
        publicKey =
            (RSAPublicKey)
                KeyFactory.getInstance(RSA)
                    .generatePublic(new X509EncodedKeySpec(base64Decoder.decode(publicKeyBase64)));
      }
      if (privateKeyBase64 != null) {
        privateKey =
            (RSAPrivateKey)
                KeyFactory.getInstance("RSA")
                    .generatePrivate(
                        new PKCS8EncodedKeySpec(base64Decoder.decode(privateKeyBase64)));
      }
      initCipher();
    } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }
  /** 初始化公私钥Cipher */
  private void initCipher() {
    if (privateKey != null) {
      PRIVATE_DECODE_BLOCK_SIZE = privateKey.getModulus().bitLength() / 8;
      PRIVATE_ENCODE_BLOCK_SIZE = PRIVATE_DECODE_BLOCK_SIZE - 11;
    }
    if (publicKey != null) {
      PUBLIC_DECODE_BLOCK_SIZE = publicKey.getModulus().bitLength() / 8;
      PUBLIC_ENCODE_BLOCK_SIZE = PUBLIC_DECODE_BLOCK_SIZE - 11;
    }
  }

  public Cipher getCipher(int var1, Key key) throws RSAException {
    try {
      Cipher cipher = Cipher.getInstance(RSA);
      cipher.init(var1, key);
      return cipher;
    } catch (Exception e) {
      throw new RSAException(e.getMessage());
    }
  }
  /**
   * 私钥加密
   *
   * @param origin 明文
   * @return 密文
   * @throws RSAException 异常信息
   */
  public String privateEncrypt(String origin) throws RSAException {
    return encodeDoFinal(
        origin, getCipher(Cipher.ENCRYPT_MODE, privateKey), PRIVATE_ENCODE_BLOCK_SIZE);
  }
  /**
   * 私钥解密
   *
   * @param secret 密文
   * @return 明文
   * @throws RSAException 异常信息
   */
  public String privateDecrypt(String secret) throws RSAException {
    return decodeDoFinal(
        secret, getCipher(Cipher.DECRYPT_MODE, privateKey), PRIVATE_DECODE_BLOCK_SIZE);
  }

  /**
   * 公钥加密
   *
   * @param origin 明文
   * @return 密文
   * @throws RSAException 异常信息
   */
  public String publicEncrypt(String origin) throws RSAException {
    return encodeDoFinal(
        origin, getCipher(Cipher.ENCRYPT_MODE, publicKey), PUBLIC_ENCODE_BLOCK_SIZE);
  }
  /**
   * 公钥解密
   *
   * @param secret 密文
   * @return 明文
   * @throws RSAException 异常信息
   */
  public String publicDecrypt(String secret) throws RSAException {
    return decodeDoFinal(
        secret, getCipher(Cipher.DECRYPT_MODE, publicKey), PUBLIC_DECODE_BLOCK_SIZE);
  }

  private static String encodeDoFinal(String str, Cipher cipher, int maxBlock) throws RSAException {
    if (str == null) {
      throw new RSAException("加密对象不能为空");
    }
    return base64Encoder.encodeToString(
        divisionDoFinal(str.getBytes(StandardCharsets.UTF_8), cipher, maxBlock));
  }

  private static String decodeDoFinal(String str, Cipher cipher, int maxBlock) throws RSAException {
    if (str == null) {
      throw new RSAException("解密对象不能为空");
    }
    return new String(
        divisionDoFinal(
            base64Decoder.decode(str.getBytes(StandardCharsets.UTF_8)), cipher, maxBlock),
        StandardCharsets.UTF_8);
  }

  private static byte[] divisionDoFinal(byte[] inputArray, Cipher cipher, int maxBlock)
      throws RSAException {
    try {
      if (cipher == null) {
        throw new RSAException("公钥或私钥为空时，不能使用其进行加解密");
      }
      int inputLength = inputArray.length;
      int offSet = 0;
      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      while (inputLength - offSet > 0) {
        if (inputLength - offSet > maxBlock) {
          out.write(cipher.doFinal(inputArray, offSet, maxBlock));
          offSet += maxBlock;
        } else {
          out.write(cipher.doFinal(inputArray, offSet, inputLength - offSet));
          offSet = inputLength;
        }
      }
      return out.toByteArray();
    } catch (IllegalBlockSizeException | BadPaddingException | IOException e) {
      e.printStackTrace();
      throw new RSAException(e.getMessage());
    }
  }
}
