package com.olh.feeds.core.email.exception;

import com.olh.feeds.core.exception.base.InternalServerError;

public class SetHelperFailedException extends InternalServerError {
  public SetHelperFailedException() {
    super("com.olh.feeds.core.email.exception.SetHelperFailedException");
  }
}
