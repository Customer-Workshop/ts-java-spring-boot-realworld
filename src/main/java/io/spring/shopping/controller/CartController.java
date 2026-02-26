package io.spring.shopping.controller;

import io.spring.shopping.model.Cart;
import io.spring.shopping.model.CartItem;
import io.spring.shopping.service.CartService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> getCart() {
    Cart cart = cartService.getCart();
    Map<String, Object> response = new HashMap<>();
    response.put("items", cart.getItems());
    response.put("total", cart.getTotal());
    response.put("itemCount", cart.getItemCount());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/items")
  public ResponseEntity<CartItem> addToCart(@RequestBody AddToCartRequest request) {
    CartItem item = cartService.addToCart(request.getProductId(), request.getQuantity());
    return ResponseEntity.ok(item);
  }

  @PutMapping("/items/{id}")
  public ResponseEntity<Map<String, Object>> updateCartItem(
      @PathVariable Long id, @RequestBody UpdateQuantityRequest request) {
    cartService.updateQuantity(id, request.getQuantity());
    Cart cart = cartService.getCart();
    Map<String, Object> response = new HashMap<>();
    response.put("items", cart.getItems());
    response.put("total", cart.getTotal());
    response.put("itemCount", cart.getItemCount());
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/items/{id}")
  public ResponseEntity<Map<String, Object>> removeFromCart(@PathVariable Long id) {
    cartService.removeFromCart(id);
    Cart cart = cartService.getCart();
    Map<String, Object> response = new HashMap<>();
    response.put("items", cart.getItems());
    response.put("total", cart.getTotal());
    response.put("itemCount", cart.getItemCount());
    return ResponseEntity.ok(response);
  }

  @DeleteMapping
  public ResponseEntity<Void> clearCart() {
    cartService.clearCart();
    return ResponseEntity.noContent().build();
  }
}
