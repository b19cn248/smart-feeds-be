package com.olh.feeds.core.exception.base;


import java.util.HashMap;
import java.util.Map;

import static com.olh.feeds.core.exception.base.StatusConstants.CONFLICT;

public class ConflictException extends BaseException {
  private static final String DEFAULT_CODE = "com.olh.news.core.exception.base.ConflictException";

  public ConflictException(String id, String objectName) {
    super(DEFAULT_CODE, "Conflict occurred", CONFLICT, createParams(id, objectName));
  }

  public ConflictException() {
    super(DEFAULT_CODE, "Conflict occurred", CONFLICT, null);
  }

  public ConflictException(String code) {
    super(code, "", CONFLICT, null);
  }

  private static Map<String, String> createParams(String id, String objectName) {
    Map<String, String> params = new HashMap<>();
    params.put("id", id);
    params.put("objectName", objectName);
    return params;
  }
}
