package io.spring.core.currency;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain entity representing an exchange rate between two currencies. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyRate {
  private String currencyCodeFrom;
  private String currencyCodeTo;
  private double exchangeRate;
}
