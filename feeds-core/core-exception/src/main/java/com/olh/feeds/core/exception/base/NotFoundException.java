package com.olh.feeds.core.exception.base;


import java.util.HashMap;
import java.util.Map;

import static com.olh.feeds.core.exception.base.StatusConstants.NOT_FOUND;

public class NotFoundException extends BaseException {
  private static final String DEFAULT_CODE = "com.olh.news.core.exception.base.NotFoundException";

  public NotFoundException(String id, String objectName) {
    super(DEFAULT_CODE, "Not found", NOT_FOUND, createParams(id, objectName));
  }

  public NotFoundException() {
    super(DEFAULT_CODE, "Not found", NOT_FOUND, null);
  }

  public NotFoundException(String code) {
    super(code, "", NOT_FOUND, null);
  }

  private static Map<String, String> createParams(String id, String objectName) {
    Map<String, String> params = new HashMap<>();
    params.put("id", id);
    params.put("objectName", objectName);
    return params;
  }
}
