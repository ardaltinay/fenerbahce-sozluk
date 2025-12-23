package net.fenerbahcesozluk.exception;

import net.fenerbahcesozluk.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    ErrorResponse response = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error("Validation Failed")
        .message("Giriş verileri geçersiz")
        .errors(errors)
        .build();

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
    ErrorResponse response = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(ex.getStatus().value())
        .error(ex.getStatus().getReasonPhrase())
        .message(ex.getMessage())
        .build();

    return ResponseEntity.status(ex.getStatus()).body(response);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
    ErrorResponse response = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error("Bad Request")
        .message(ex.getMessage())
        .build();

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
    ErrorResponse response = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.UNAUTHORIZED.value())
        .error("Unauthorized")
        .message("Kullanıcı adı veya şifre hatalı")
        .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler(RateLimitExceededException.class)
  public ResponseEntity<ErrorResponse> handleRateLimitExceededException(RateLimitExceededException ex) {
    ErrorResponse response = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.TOO_MANY_REQUESTS.value())
        .error("Too Many Requests")
        .message(ex.getMessage())
        .build();

    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
        .body(response);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex) {
    ErrorResponse response = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.NOT_FOUND.value())
        .error("Not Found")
        .message(ex.getMessage())
        .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    ErrorResponse response = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error("Internal Server Error")
        .message("Beklenmeyen bir hata oluştu")
        .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
