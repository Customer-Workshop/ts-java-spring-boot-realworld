package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import io.spring.infrastructure.mybatis.readservice.TagReadService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TagsQueryServiceTest {

  @Mock
  private TagReadService tagReadService;

  private TagsQueryService tagsQueryService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    tagsQueryService = new TagsQueryService(tagReadService);
  }

  @Test
  public void should_return_all_tags() {
    when(tagReadService.all()).thenReturn(Arrays.asList("java", "spring", "reactive"));

    List<String> tags = tagsQueryService.allTags();

    assertThat(tags.size(), is(3));
    assertThat(tags.get(0), is("java"));
  }

  @Test
  public void should_return_empty_list_when_no_tags() {
    when(tagReadService.all()).thenReturn(Collections.emptyList());

    List<String> tags = tagsQueryService.allTags();

    assertThat(tags.size(), is(0));
  }
}
