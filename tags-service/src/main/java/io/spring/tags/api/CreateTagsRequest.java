package io.spring.tags.api;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateTagsRequest {
  private List<String> names;
}
