package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.CurrencyService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({CurrencyApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class CurrencyApiTest extends TestWithCurrentUser {

  @Autowired private MockMvc mvc;

  @MockBean private CurrencyService currencyService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_all_currencies() {
    Map<String, Map<String, String>> currencies = new LinkedHashMap<>();

    Map<String, String> inr = new LinkedHashMap<>();
    inr.put("countryName", "INDIA");
    inr.put("currencyCode", "INR");
    inr.put("currencyName", "Indian Rupees");
    currencies.put("INR", inr);

    Map<String, String> usd = new LinkedHashMap<>();
    usd.put("countryName", "USA");
    usd.put("currencyCode", "USD");
    usd.put("currencyName", "US Dollars");
    currencies.put("USD", usd);

    when(currencyService.getAllCurrencies()).thenReturn(currencies);

    given()
        .contentType("application/json")
        .when()
        .get("/currency")
        .then()
        .statusCode(200)
        .body("INR.countryName", equalTo("INDIA"))
        .body("INR.currencyCode", equalTo("INR"))
        .body("INR.currencyName", equalTo("Indian Rupees"))
        .body("USD.countryName", equalTo("USA"))
        .body("USD.currencyCode", equalTo("USD"))
        .body("USD.currencyName", equalTo("US Dollars"));
  }

  @Test
  public void should_get_exchange_rate() {
    Map<String, String> rate = new LinkedHashMap<>();
    rate.put("fromCurrencyCode", "USD");
    rate.put("toCurrencyCode", "INR");
    rate.put("exchangeRate", "80.08");

    when(currencyService.getExchangeRate("USD", "INR")).thenReturn(rate);

    given()
        .contentType("application/json")
        .when()
        .get("/exchange-rate/{fromCur}/{toCur}", "USD", "INR")
        .then()
        .statusCode(200)
        .body("fromCurrencyCode", equalTo("USD"))
        .body("toCurrencyCode", equalTo("INR"))
        .body("exchangeRate", equalTo("80.08"));
  }

  @Test
  public void should_return_404_when_exchange_rate_not_found() {
    when(currencyService.getExchangeRate("XYZ", "ABC")).thenReturn(null);

    given()
        .contentType("application/json")
        .when()
        .get("/exchange-rate/{fromCur}/{toCur}", "XYZ", "ABC")
        .then()
        .statusCode(404);
  }
}
