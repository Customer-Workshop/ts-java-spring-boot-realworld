package io.spring.tags.core;

import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@EqualsAndHashCode(of = "name")
public class Tag {
  private String id;
  private String name;

  public Tag(String name) {
    this.id = UUID.randomUUID().toString();
    this.name = name;
  }

  public Tag(String id, String name) {
    this.id = id;
    this.name = name;
  }
}
