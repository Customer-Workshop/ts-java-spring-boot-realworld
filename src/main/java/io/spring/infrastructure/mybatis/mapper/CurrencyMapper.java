package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.currency.Currency;
import io.spring.core.currency.CurrencyRate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** MyBatis mapper interface for currency and exchange rate database operations. */
@Mapper
public interface CurrencyMapper {

  List<Currency> findAllCurrencies();

  CurrencyRate findExchangeRate(
      @Param("from") String currencyCodeFrom, @Param("to") String currencyCodeTo);
}
