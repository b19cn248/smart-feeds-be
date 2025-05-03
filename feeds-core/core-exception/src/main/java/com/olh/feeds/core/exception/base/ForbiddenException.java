package com.olh.feeds.core.exception.base;

import java.util.HashMap;
import java.util.Map;

import static com.olh.feeds.core.exception.base.StatusConstants.FORBIDDEN;

public class ForbiddenException extends BaseException{
  private static final String DEFAULT_CODE = "com.olh.news.core.exception.base.ForbiddenException";

  public ForbiddenException(String id, String objectName) {
    super(DEFAULT_CODE, "Forbidden", FORBIDDEN, createParams(id, objectName));
  }

  public ForbiddenException() {
    super(DEFAULT_CODE, "Forbidden", FORBIDDEN, null);
  }

  public ForbiddenException(String code) {
    super(code, "", FORBIDDEN, null);
  }

  private static Map<String, String> createParams(String id, String objectName) {
    Map<String, String> params = new HashMap<>();
    params.put("id", id);
    params.put("objectName", objectName);
    return params;
  }
}
