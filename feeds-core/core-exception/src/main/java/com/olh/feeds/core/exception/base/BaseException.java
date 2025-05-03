package com.olh.feeds.core.exception.base;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class BaseException extends RuntimeException {
  private final String code;
  private final int status;
  private final Map<String, String> params;

  public BaseException(String code, String message, int status, Map<String, String> params) {
    super(message);
    this.code = code;
    this.status = status;
    this.params = params != null ? new HashMap<>(params) : Collections.emptyMap();
  }

  public BaseException(String message) {
    this("", message, 0, Collections.emptyMap());
  }

  public BaseException addParam(String key, String value) {
    Map<String, String> newParams = new HashMap<>(this.params);
    newParams.put(key, value);
    return new BaseException(this.code, this.getMessage(), this.status, newParams);
  }
}
