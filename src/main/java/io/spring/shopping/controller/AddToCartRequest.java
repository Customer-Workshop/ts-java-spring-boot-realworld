package io.spring.shopping.controller;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartRequest {
  @NotNull private Long productId;

  @Min(1)
  private int quantity = 1;
}
