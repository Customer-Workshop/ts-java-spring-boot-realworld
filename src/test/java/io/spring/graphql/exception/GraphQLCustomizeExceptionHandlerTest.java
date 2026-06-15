package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"unchecked", "rawtypes"})
public class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    InvalidAuthenticationException ex = new InvalidAuthenticationException();
    when(params.getException()).thenReturn(ex);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_constraint_violation_exception() {
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);

    ConstraintViolationException cve =
        buildConstraintViolationException("createUser.param.email", "can't be empty");
    when(params.getException()).thenReturn(cve);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_delegate_to_default_handler_for_other_exceptions() {
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    RuntimeException ex = new RuntimeException("something else");
    when(params.getException()).thenReturn(ex);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @Test
  void should_get_errors_as_data() {
    ConstraintViolationException cve =
        buildConstraintViolationException("createUser.param.email", "can't be empty");

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
    assertEquals("email", error.getErrors().get(0).getKey());
    assertTrue(error.getErrors().get(0).getValue().contains("can't be empty"));
  }

  @Test
  void should_handle_simple_path() {
    ConstraintViolationException cve = buildConstraintViolationException("simplefield", "invalid");

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("simplefield", error.getErrors().get(0).getKey());
  }

  private ConstraintViolationException buildConstraintViolationException(
      String pathStr, String message) {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn(pathStr);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn(message);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation =
        new javax.validation.constraints.NotBlank() {
          @Override
          public String message() {
            return "";
          }

          @Override
          public Class[] groups() {
            return new Class[0];
          }

          @Override
          public Class[] payload() {
            return new Class[0];
          }

          @Override
          public Class<? extends Annotation> annotationType() {
            return javax.validation.constraints.NotBlank.class;
          }
        };
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    violations.add(violation);

    return new ConstraintViolationException(violations);
  }
}
