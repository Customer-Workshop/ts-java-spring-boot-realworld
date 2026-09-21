package io.spring.graphql;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.graphql.server.webmvc.GraphQlHttpHandler;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * The REST API unwraps root values ({@code {"user": {...}}}), but GraphQL requests are plain {@code
 * {"query": ..., "variables": ...}} documents, so the GraphQL endpoint needs its own converter.
 */
@Configuration
public class GraphQLHttpConfig {

  @Bean
  public GraphQlHttpHandler graphQlHttpHandler(
      WebGraphQlHandler webGraphQlHandler, ObjectMapper objectMapper) {
    ObjectMapper graphQlMapper =
        objectMapper.copy().disable(DeserializationFeature.UNWRAP_ROOT_VALUE);
    return new GraphQlHttpHandler(
        webGraphQlHandler, new MappingJackson2HttpMessageConverter(graphQlMapper));
  }
}
