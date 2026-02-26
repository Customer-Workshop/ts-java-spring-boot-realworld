package io.spring.shopping.service;

import io.spring.shopping.model.Product;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

  private final List<Product> products =
      Arrays.asList(
          new Product(
              1L,
              "Wireless Bluetooth Headphones",
              "Premium noise-cancelling wireless headphones with 30-hour battery life, deep bass, and crystal-clear audio. Perfect for music lovers and commuters.",
              79.99,
              "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600"),
          new Product(
              2L,
              "Smart Fitness Watch",
              "Track your health and fitness with this sleek smartwatch. Features heart rate monitoring, GPS, sleep tracking, and 7-day battery life.",
              149.99,
              "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600"),
          new Product(
              3L,
              "Portable Bluetooth Speaker",
              "Compact waterproof speaker with 360-degree sound. Perfect for outdoor adventures, pool parties, and travel. 12-hour playtime.",
              49.99,
              "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=600"),
          new Product(
              4L,
              "Laptop Backpack",
              "Stylish and functional laptop backpack with USB charging port, anti-theft design, and water-resistant material. Fits up to 15.6-inch laptops.",
              39.99,
              "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600"),
          new Product(
              5L,
              "Mechanical Keyboard",
              "RGB backlit mechanical keyboard with Cherry MX switches. Programmable keys, aluminum frame, and detachable wrist rest for ultimate typing comfort.",
              99.99,
              "https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?w=600"),
          new Product(
              6L,
              "Wireless Charging Pad",
              "Fast wireless charger compatible with all Qi-enabled devices. Sleek design with LED indicator and over-charge protection.",
              24.99,
              "https://images.unsplash.com/photo-1586953208448-b95a79798f07?w=600"));

  public List<Product> getAllProducts() {
    return products;
  }

  public Optional<Product> getProductById(Long id) {
    return products.stream().filter(p -> p.getId().equals(id)).findFirst();
  }
}
