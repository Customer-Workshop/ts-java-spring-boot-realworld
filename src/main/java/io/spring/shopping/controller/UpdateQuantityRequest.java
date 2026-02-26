package io.spring.shopping.controller;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

@Data
@JsonRootName("item")
public class UpdateQuantityRequest {
  private int quantity;
}
