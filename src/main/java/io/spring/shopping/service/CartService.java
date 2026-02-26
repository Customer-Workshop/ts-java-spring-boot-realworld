package io.spring.shopping.service;

import io.spring.shopping.model.Cart;
import io.spring.shopping.model.CartItem;
import io.spring.shopping.model.Product;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class CartService {

  private final Cart cart = new Cart();
  private final AtomicLong cartItemIdGenerator = new AtomicLong(1);
  private final ProductService productService;

  public CartService(ProductService productService) {
    this.productService = productService;
  }

  public Cart getCart() {
    return cart;
  }

  public CartItem addToCart(Long productId, int quantity) {
    Optional<Product> productOpt = productService.getProductById(productId);
    if (productOpt.isEmpty()) {
      throw new IllegalArgumentException("Product not found with id: " + productId);
    }

    Product product = productOpt.get();

    // Check if product already in cart, then update quantity
    Optional<CartItem> existingItem =
        cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(productId))
            .findFirst();

    if (existingItem.isPresent()) {
      CartItem item = existingItem.get();
      item.setQuantity(item.getQuantity() + quantity);
      return item;
    }

    // Add new item to cart
    CartItem newItem = new CartItem(cartItemIdGenerator.getAndIncrement(), product, quantity);
    cart.getItems().add(newItem);
    return newItem;
  }

  public void removeFromCart(Long cartItemId) {
    cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
  }

  public void updateQuantity(Long cartItemId, int quantity) {
    cart.getItems().stream()
        .filter(item -> item.getId().equals(cartItemId))
        .findFirst()
        .ifPresent(
            item -> {
              if (quantity <= 0) {
                cart.getItems().remove(item);
              } else {
                item.setQuantity(quantity);
              }
            });
  }

  public void clearCart() {
    cart.getItems().clear();
  }
}
