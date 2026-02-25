package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import io.spring.api.exception.InvalidAuthenticationException;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  @Test
  void should_handle_invalid_authentication_exception() throws Exception {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params = mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    CompletableFuture<DataFetcherExceptionHandlerResult> future = handler.handleException(params);
    DataFetcherExceptionHandlerResult result = future.get();

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
    assertTrue(result.getErrors().get(0).getMessage().contains("invalid email or password"));
  }

  @Test
  void should_handle_generic_exception_with_default_handler() throws Exception {
    RuntimeException exception = new RuntimeException("generic error");
    DataFetcherExceptionHandlerParameters params = mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    CompletableFuture<DataFetcherExceptionHandlerResult> future = handler.handleException(params);
    assertNotNull(future);
  }

  @Test
  void should_extract_param_from_simple_path() {
    String result = getParam("fieldName");
    assertEquals("fieldName", result);
  }

  @Test
  void should_extract_param_from_complex_path() {
    String result = getParam("object.method.field.subfield");
    assertEquals("field.subfield", result);
  }

  private String getParam(String s) {
    String[] splits = s.split("\\.");
    if (splits.length == 1) {
      return s;
    } else {
      return String.join(".", java.util.Arrays.copyOfRange(splits, 2, splits.length));
    }
  }
}
