package com.olh.feeds.core.exception.base;


import static com.olh.feeds.core.exception.base.StatusConstants.BAD_REQUEST;

public class BadRequestException extends BaseException {
    private static final String DEFAULT_CODE = "com.olh.news.core.exception.base.BadRequestException";

    public BadRequestException() {
        super(DEFAULT_CODE, "Bad request", BAD_REQUEST, null);
    }

    public BadRequestException(String code) {
        super(code, "", BAD_REQUEST, null);
    }
}
