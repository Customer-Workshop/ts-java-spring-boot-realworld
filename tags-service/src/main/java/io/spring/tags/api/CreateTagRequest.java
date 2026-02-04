package io.spring.tags.api;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateTagRequest {
  private String name;
}
