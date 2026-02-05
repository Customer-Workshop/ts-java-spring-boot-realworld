package io.spring.infrastructure.r2dbc.readservice;

import io.spring.infrastructure.mybatis.readservice.TagReadService;
import java.util.List;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;

@Service
public class R2dbcTagReadService implements TagReadService {

  private final DatabaseClient db;

  public R2dbcTagReadService(DatabaseClient db) {
    this.db = db;
  }

  @Override
  public List<String> all() {
    return db.sql("SELECT DISTINCT name FROM tags ORDER BY name")
        .map((row, metadata) -> row.get("name", String.class))
        .all()
        .collectList()
        .block();
  }
}
