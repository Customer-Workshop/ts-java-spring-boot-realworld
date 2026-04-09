package io.spring.api.exception;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;

public class CustomizeExceptionHandlerTest {

  private CustomizeExceptionHandler handler;
  private WebRequest webRequest;

  @BeforeEach
  public void setUp() {
    handler = new CustomizeExceptionHandler();
    webRequest = mock(WebRequest.class);
  }

  @Test
  public void should_handle_invalid_request_exception() {
    Errors errors = new BeanPropertyBindingResult(new Object(), "testObject");
    ((BeanPropertyBindingResult) errors)
        .addError(new FieldError("testObject", "fieldName", "must not be blank"));

    InvalidRequestException exception = new InvalidRequestException(errors);

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, webRequest);

    Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
  }

  @Test
  public void should_handle_invalid_request_with_multiple_field_errors() {
    Errors errors = new BeanPropertyBindingResult(new Object(), "testObject");
    ((BeanPropertyBindingResult) errors)
        .addError(new FieldError("testObject", "email", "must be valid email"));
    ((BeanPropertyBindingResult) errors)
        .addError(new FieldError("testObject", "username", "must not be blank"));

    InvalidRequestException exception = new InvalidRequestException(errors);

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, webRequest);

    Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();

    ResponseEntity<Object> response = handler.handleInvalidAuthentication(exception, webRequest);

    Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    Assertions.assertEquals("invalid email or password", body.get("message"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void should_handle_constraint_violation_exception_simple_path() {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("fieldName");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("must not be blank");
    when(violation.getRootBeanClass()).thenReturn((Class) Object.class);

    ConstraintDescriptor<?> descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    doReturn(descriptor).when(violation).getConstraintDescriptor();

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    ErrorResource result = handler.handleConstraintViolation(ex, webRequest);

    Assertions.assertNotNull(result);
    List<FieldErrorResource> fieldErrors = result.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    Assertions.assertEquals("fieldName", fieldErrors.get(0).getField());
    Assertions.assertEquals("must not be blank", fieldErrors.get(0).getMessage());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void should_handle_constraint_violation_exception_nested_path() {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createArticle.newArticleParam.title");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("can't be empty");
    when(violation.getRootBeanClass()).thenReturn((Class) Object.class);

    ConstraintDescriptor<?> descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    doReturn(descriptor).when(violation).getConstraintDescriptor();

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    ErrorResource result = handler.handleConstraintViolation(ex, webRequest);

    Assertions.assertNotNull(result);
    List<FieldErrorResource> fieldErrors = result.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    Assertions.assertEquals("title", fieldErrors.get(0).getField());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void should_handle_constraint_violation_with_multiple_violations() {
    ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
    Path path1 = mock(Path.class);
    when(path1.toString()).thenReturn("method.param.email");
    when(violation1.getPropertyPath()).thenReturn(path1);
    when(violation1.getMessage()).thenReturn("invalid email");
    when(violation1.getRootBeanClass()).thenReturn((Class) Object.class);

    ConstraintDescriptor<?> descriptor1 = mock(ConstraintDescriptor.class);
    Annotation annotation1 = mock(Annotation.class);
    when(annotation1.annotationType()).thenReturn((Class) Override.class);
    when(descriptor1.getAnnotation()).thenReturn(annotation1);
    doReturn(descriptor1).when(violation1).getConstraintDescriptor();

    ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
    Path path2 = mock(Path.class);
    when(path2.toString()).thenReturn("method.param.username");
    when(violation2.getPropertyPath()).thenReturn(path2);
    when(violation2.getMessage()).thenReturn("already taken");
    when(violation2.getRootBeanClass()).thenReturn((Class) Object.class);

    ConstraintDescriptor<?> descriptor2 = mock(ConstraintDescriptor.class);
    Annotation annotation2 = mock(Annotation.class);
    when(annotation2.annotationType()).thenReturn((Class) Override.class);
    when(descriptor2.getAnnotation()).thenReturn(annotation2);
    doReturn(descriptor2).when(violation2).getConstraintDescriptor();

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation1);
    violations.add(violation2);
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    ErrorResource result = handler.handleConstraintViolation(ex, webRequest);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(2, result.getFieldErrors().size());
  }

  @Test
  public void should_handle_invalid_request_with_no_field_errors() {
    Errors errors = new BeanPropertyBindingResult(new Object(), "testObject");

    InvalidRequestException exception = new InvalidRequestException(errors);

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, webRequest);

    Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }
}
