package io.spring.application;

import io.spring.core.currency.Currency;
import io.spring.core.currency.CurrencyRate;
import io.spring.infrastructure.mybatis.mapper.CurrencyMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service layer for currency and exchange rate operations. Provides business logic for retrieving
 * currency information and calculating exchange rates.
 */
@Service
@AllArgsConstructor
public class CurrencyService {

  private final CurrencyMapper currencyMapper;

  /**
   * Retrieves all currencies as a map keyed by currency code.
   *
   * @return a map where each key is a currency code and the value contains country name, currency
   *     code, and currency name
   */
  public Map<String, Map<String, String>> getAllCurrencies() {
    List<Currency> currencies = currencyMapper.findAllCurrencies();
    Map<String, Map<String, String>> result = new LinkedHashMap<>();
    for (Currency currency : currencies) {
      Map<String, String> entry = new LinkedHashMap<>();
      entry.put("countryName", currency.getCountryName());
      entry.put("currencyCode", currency.getCurrencyCode());
      entry.put("currencyName", currency.getCurrencyName());
      result.put(currency.getCurrencyCode(), entry);
    }
    return result;
  }

  /**
   * Retrieves the exchange rate between two currencies.
   *
   * @param fromCurrencyCode the source currency code
   * @param toCurrencyCode the target currency code
   * @return a map containing the from/to codes and exchange rate, or null if not found
   */
  public Map<String, String> getExchangeRate(String fromCurrencyCode, String toCurrencyCode) {
    CurrencyRate rate = currencyMapper.findExchangeRate(fromCurrencyCode, toCurrencyCode);
    if (rate == null) {
      return null;
    }
    Map<String, String> result = new LinkedHashMap<>();
    result.put("fromCurrencyCode", rate.getCurrencyCodeFrom());
    result.put("toCurrencyCode", rate.getCurrencyCodeTo());
    result.put("exchangeRate", String.valueOf(rate.getExchangeRate()));
    return result;
  }
}
