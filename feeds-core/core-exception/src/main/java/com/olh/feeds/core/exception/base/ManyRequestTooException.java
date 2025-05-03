package com.olh.feeds.core.exception.base;

import static com.olh.feeds.core.exception.base.StatusConstants.MANY_REQUEST_TOO;

public class ManyRequestTooException extends BaseException{
  private static final String DEFAULT_CODE = "com.olh.news.core.exception.base.ManyRequestTooException";
  public ManyRequestTooException() {
    super(DEFAULT_CODE, "Rate limit exceeded", MANY_REQUEST_TOO, null);
  }
}
