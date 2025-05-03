package com.olh.feeds.core.exception.response;

import com.olh.feeds.core.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class ResponseGeneral<T> {
    private int status;
    private String message;
    private T data;
    private String timestamp;

    public static <T> ResponseGeneral<T> of(int status, String message, T data) {
        return of(status, message, data, com.olh.feeds.core.utils.DateUtils.getCurrentDateString());
    }

    public static <T> ResponseGeneral<T> of(int status, String message) {
        return of(status, message, null, DateUtils.getCurrentDateString());
    }

    public static <T> ResponseGeneral<T> ofSuccess(String message, T data) {
        return of(HttpStatus.OK.value(), message, data, DateUtils.getCurrentDateString());
    }

    public static <T> ResponseGeneral<T> ofSuccess(String message) {
        return of(HttpStatus.OK.value(), message, null, DateUtils.getCurrentDateString());
    }

    public static <T> ResponseGeneral<T> ofCreated(String message, T data) {
        return of(HttpStatus.CREATED.value(), message, data, DateUtils.getCurrentDateString());
    }

}
