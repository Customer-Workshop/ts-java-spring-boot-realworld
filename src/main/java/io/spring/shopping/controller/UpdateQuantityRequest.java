package io.spring.shopping.controller;

import javax.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateQuantityRequest {
  @Min(0)
  private int quantity;
}
