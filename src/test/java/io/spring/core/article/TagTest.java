package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  public void should_create_tag_with_name() {
    Tag tag = new Tag("java");

    assertThat(tag.getId(), notNullValue());
    assertThat(tag.getName(), is("java"));
  }

  @Test
  public void should_generate_unique_id() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");

    assertThat(tag1.getId(), not(tag2.getId()));
  }

  @Test
  public void should_be_equal_by_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");

    assertThat(tag1.equals(tag2), is(true));
    assertThat(tag1.equals(tag1), is(true));
  }

  @Test
  public void should_not_be_equal_with_different_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");

    assertThat(tag1.equals(tag2), is(false));
  }

  @Test
  public void should_have_consistent_hashcode() {
    Tag tag = new Tag("java");
    int hashCode1 = tag.hashCode();
    int hashCode2 = tag.hashCode();

    assertThat(hashCode1, is(hashCode2));
  }
}
