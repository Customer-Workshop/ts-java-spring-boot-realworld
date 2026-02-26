package io.spring.bdd.steps;

import io.cucumber.java.Before;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** Cucumber hooks that clean up the database before each scenario to ensure test isolation. */
public class DatabaseCleanupHooks {

  @Autowired private DataSource dataSource;

  @Before(order = 0)
  public void cleanDatabase() {
    JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    // Delete in order that respects foreign key-like constraints
    jdbc.execute("DELETE FROM article_favorites");
    jdbc.execute("DELETE FROM article_tags");
    jdbc.execute("DELETE FROM comments");
    jdbc.execute("DELETE FROM follows");
    jdbc.execute("DELETE FROM tags");
    jdbc.execute("DELETE FROM articles");
    jdbc.execute("DELETE FROM users");
  }
}
