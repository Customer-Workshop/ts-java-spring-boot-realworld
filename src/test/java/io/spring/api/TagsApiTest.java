package io.spring.api;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import io.spring.application.TagsQueryService;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class TagsApiTest {

  @Mock private TagsQueryService tagsQueryService;

  private TagsApi tagsApi;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    tagsApi = new TagsApi(tagsQueryService);
  }

  @Test
  public void should_return_all_tags() {
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList("java", "spring", "reactive"));

    ResponseEntity response = tagsApi.getTags();

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), notNullValue());
  }

  @Test
  public void should_return_empty_tags() {
    when(tagsQueryService.allTags()).thenReturn(Collections.emptyList());

    ResponseEntity response = tagsApi.getTags();

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), notNullValue());
  }
}
