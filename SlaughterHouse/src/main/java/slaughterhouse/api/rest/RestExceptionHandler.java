package slaughterhouse.api.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import slaughterhouse.service.NotFoundException;

@RestControllerAdvice
public class RestExceptionHandler
{
  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleNotFound(NotFoundException exception)
  {
    return exception.getMessage();
  }
}
