package io.spring.application;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class NodeTest {

  @Test
  public void should_implement_node_interface() {
    TestNode node = new TestNode();

    assertThat(node.getCursor(), notNullValue());
  }

  private static class TestNode implements Node {
    @Override
    public PageCursor getCursor() {
      return new DateTimeCursor(java.time.LocalDateTime.now());
    }
  }
}
