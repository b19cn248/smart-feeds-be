package com.olh.feeds.core.exception.base;

import java.util.Map;

import static com.olh.feeds.core.exception.base.StatusConstants.SERVER_ERROR;

public class InternalServerError extends BaseException {
  private static final String DEFAULT_CODE = "com.olh.news.core.exception.base.InternalServerError";

  public InternalServerError(String code, String message, Map<String, String> params) {
    super(code, message, SERVER_ERROR, params);
  }

  public InternalServerError() {
    super(DEFAULT_CODE, "", SERVER_ERROR, null);
  }

  public InternalServerError(String message) {
    super(DEFAULT_CODE, message, SERVER_ERROR, null);
  }
}
