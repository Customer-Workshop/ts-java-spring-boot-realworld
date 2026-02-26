package io.spring.bdd;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

@TestConfiguration
public class CucumberTestConfiguration {

  @Bean
  @Lazy
  public HttpClientHelper httpClientHelper(Environment environment) {
    int port = environment.getRequiredProperty("local.server.port", Integer.class);
    return new HttpClientHelper(port);
  }
}
