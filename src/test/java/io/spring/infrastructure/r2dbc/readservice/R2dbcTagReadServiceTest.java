package io.spring.infrastructure.r2dbc.readservice;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class R2dbcTagReadServiceTest {

  @Test
  public void should_create_service() {
    assertThat(R2dbcTagReadService.class, notNullValue());
  }
}
