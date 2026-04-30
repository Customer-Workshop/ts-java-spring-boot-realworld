package io.spring.core.currency;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain entity representing a currency with its associated country information. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Currency {
  private int id;
  private String currencyCode;
  private String currencyName;
  private String countryName;
}
