package io.spring.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import io.spring.core.currency.Currency;
import io.spring.core.currency.CurrencyRate;
import io.spring.infrastructure.mybatis.mapper.CurrencyMapper;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CurrencyServiceTest {

  @Mock private CurrencyMapper currencyMapper;

  @InjectMocks private CurrencyService currencyService;

  @Test
  public void should_return_all_currencies_as_map() {
    Currency inr = new Currency(1, "INR", "Indian Rupees", "INDIA");
    Currency usd = new Currency(2, "USD", "US Dollars", "USA");

    when(currencyMapper.findAllCurrencies()).thenReturn(Arrays.asList(inr, usd));

    Map<String, Map<String, String>> result = currencyService.getAllCurrencies();

    assertEquals(2, result.size());
    assertNotNull(result.get("INR"));
    assertEquals("INDIA", result.get("INR").get("countryName"));
    assertEquals("INR", result.get("INR").get("currencyCode"));
    assertEquals("Indian Rupees", result.get("INR").get("currencyName"));
    assertNotNull(result.get("USD"));
    assertEquals("USA", result.get("USD").get("countryName"));
  }

  @Test
  public void should_return_exchange_rate() {
    CurrencyRate rate = new CurrencyRate("USD", "INR", 80.08);

    when(currencyMapper.findExchangeRate("USD", "INR")).thenReturn(rate);

    Map<String, String> result = currencyService.getExchangeRate("USD", "INR");

    assertNotNull(result);
    assertEquals("USD", result.get("fromCurrencyCode"));
    assertEquals("INR", result.get("toCurrencyCode"));
    assertEquals("80.08", result.get("exchangeRate"));
  }

  @Test
  public void should_return_null_when_rate_not_found() {
    when(currencyMapper.findExchangeRate("XYZ", "ABC")).thenReturn(null);

    Map<String, String> result = currencyService.getExchangeRate("XYZ", "ABC");

    assertNull(result);
  }
}
