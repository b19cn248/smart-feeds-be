package com.olh.feeds.core.exception.base;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StatusConstants {

  public static final Integer NOT_FOUND = 404;
  public static final Integer CONFLICT = 409;
  public static final Integer BAD_REQUEST = 400;
  public static final Integer SERVER_ERROR = 500;
  public static final Integer FORBIDDEN = 403;
  public static final Integer MANY_REQUEST_TOO = 429;
}
