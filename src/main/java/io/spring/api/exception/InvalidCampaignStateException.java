package io.spring.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidCampaignStateException extends RuntimeException {
  public InvalidCampaignStateException(String message) {
    super(message);
  }
}
