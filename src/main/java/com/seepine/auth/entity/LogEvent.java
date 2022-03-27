package com.seepine.auth.entity;

import lombok.Data;

@Data
public class LogEvent {
  /** 模块，一般用于微服务或指定类型，方便统计 */
  String module;
  /** 标题 */
  String title;
  /** 内容 */
  String content;
  /** 接口地址 */
  String requestUri;
  /** 请求方式 */
  String method;
  /** ua */
  String userAgent;
  /** 请求头 */
  String headers;
  /** 请求参数 */
  String params;
  /** contentType */
  String contentType;
  /** 客户端ip */
  String clientIp;
  /** 执行时间 */
  Long executionTime;
  /** 异常信息，null则表示正常执行 */
  String exception;
  /** 异常时堆栈信息 */
  String exceptionStackTrace;
  /** 用户信息，可能为空 */
  Object user;
}
