package io.spring.shopping.controller;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

@Data
@JsonRootName("item")
public class AddToCartRequest {
  private Long productId;
  private int quantity = 1;
}
