import { SERVER_BASE_URL } from "../utils/constant";

export interface CurrencyInfo {
  countryName: string;
  currencyCode: string;
  currencyName: string;
}

export interface ExchangeRateResponse {
  fromCurrencyCode: string;
  toCurrencyCode: string;
  exchangeRate: string;
}

/**
 * Fetches all available currencies from the API.
 */
export const fetchCurrencies = async (): Promise<
  Record<string, CurrencyInfo>
> => {
  const response = await fetch(`${SERVER_BASE_URL}/currency`);
  if (!response.ok) {
    throw new Error("Failed to fetch currencies");
  }
  return response.json();
};

/**
 * Fetches the exchange rate between two currencies.
 */
export const fetchExchangeRate = async (
  from: string,
  to: string
): Promise<ExchangeRateResponse> => {
  const response = await fetch(
    `${SERVER_BASE_URL}/exchange-rate/${from}/${to}`
  );
  if (!response.ok) {
    throw new Error("Exchange rate not found");
  }
  return response.json();
};
