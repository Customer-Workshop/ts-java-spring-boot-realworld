package io.spring.shopping.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class Cart {
  private List<CartItem> items = new ArrayList<>();

  public double getTotal() {
    return items.stream()
        .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
        .sum();
  }

  public int getItemCount() {
    return items.stream().mapToInt(CartItem::getQuantity).sum();
  }
}
