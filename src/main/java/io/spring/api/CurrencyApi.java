package io.spring.api;

import io.spring.application.CurrencyService;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for currency and exchange rate endpoints. Provides APIs to list all currencies
 * and retrieve exchange rates between currency pairs.
 */
@RestController
@AllArgsConstructor
public class CurrencyApi {

  private final CurrencyService currencyService;

  /**
   * Lists all available currencies with their country and currency details.
   *
   * @return a map keyed by currency code containing currency information
   */
  @GetMapping("/currency")
  public ResponseEntity<Map<String, Map<String, String>>> getAllCurrencies() {
    return ResponseEntity.ok(currencyService.getAllCurrencies());
  }

  /**
   * Retrieves the exchange rate between two currencies.
   *
   * @param fromCur the source currency code (e.g., "USD")
   * @param toCur the target currency code (e.g., "INR")
   * @return the exchange rate details or 404 if the pair is not found
   */
  @GetMapping("/exchange-rate/{fromCur}/{toCur}")
  public ResponseEntity<Map<String, String>> getExchangeRate(
      @PathVariable String fromCur, @PathVariable String toCur) {
    Map<String, String> rate = currencyService.getExchangeRate(fromCur, toCur);
    if (rate == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(rate);
  }
}
