package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class PageTest {

  @Test
  public void should_create_page_with_default_values() {
    Page page = new Page(0, 20);

    assertThat(page.getOffset(), is(0));
    assertThat(page.getLimit(), is(20));
  }

  @Test
  public void should_create_page_with_custom_values() {
    Page page = new Page(10, 50);

    assertThat(page.getOffset(), is(10));
    assertThat(page.getLimit(), is(50));
  }

  @Test
  public void should_cap_limit_at_max() {
    Page page = new Page(0, 200);

    assertThat(page.getLimit(), is(100));
  }

  @Test
  public void should_use_default_for_negative_offset() {
    Page page = new Page(-5, 20);

    assertThat(page.getOffset(), is(0));
  }

  @Test
  public void should_use_default_for_negative_limit() {
    Page page = new Page(0, -5);

    assertThat(page.getLimit(), is(20));
  }

  @Test
  public void should_use_default_for_zero_limit() {
    Page page = new Page(0, 0);

    assertThat(page.getLimit(), is(20));
  }

  @Test
  public void should_be_equal_with_same_values() {
    Page page1 = new Page(10, 20);
    Page page2 = new Page(10, 20);

    assertThat(page1.equals(page2), is(true));
  }

  @Test
  public void should_have_consistent_hashcode() {
    Page page = new Page(10, 20);
    int hashCode1 = page.hashCode();
    int hashCode2 = page.hashCode();

    assertThat(hashCode1, is(hashCode2));
  }
}
