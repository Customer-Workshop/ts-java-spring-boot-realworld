import React, { useState } from "react";
import {
  CurrencyInfo,
  ExchangeRateResponse,
  fetchExchangeRate,
} from "../../lib/api/currency";

interface ExchangeRateCalculatorProps {
  currencies: Record<string, CurrencyInfo>;
}

/**
 * Provides a form to select two currencies and calculate the exchange rate.
 */
const ExchangeRateCalculator: React.FC<ExchangeRateCalculatorProps> = ({
  currencies,
}) => {
  const [fromCurrency, setFromCurrency] = useState("");
  const [toCurrency, setToCurrency] = useState("");
  const [result, setResult] = useState<ExchangeRateResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const currencyCodes = Object.keys(currencies);

  const handleCalculate = async () => {
    if (!fromCurrency || !toCurrency) {
      setError("Please select both currencies.");
      setResult(null);
      return;
    }
    if (fromCurrency === toCurrency) {
      setError("Please select different currencies.");
      setResult(null);
      return;
    }

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const data = await fetchExchangeRate(fromCurrency, toCurrency);
      setResult(data);
    } catch {
      setError("Exchange rate not found for the selected pair.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="exchange-rate-calculator" style={{ marginTop: "2rem" }}>
      <h2>Exchange Rate Calculator</h2>
      <div className="row" style={{ marginBottom: "1rem" }}>
        <div className="col-md-4">
          <label htmlFor="fromCurrency">From Currency</label>
          <select
            id="fromCurrency"
            className="form-control"
            value={fromCurrency}
            onChange={(e) => setFromCurrency(e.target.value)}
            data-testid="from-currency-select"
          >
            <option value="">-- Select --</option>
            {currencyCodes.map((code) => (
              <option key={code} value={code}>
                {code} - {currencies[code].currencyName}
              </option>
            ))}
          </select>
        </div>
        <div className="col-md-4">
          <label htmlFor="toCurrency">To Currency</label>
          <select
            id="toCurrency"
            className="form-control"
            value={toCurrency}
            onChange={(e) => setToCurrency(e.target.value)}
            data-testid="to-currency-select"
          >
            <option value="">-- Select --</option>
            {currencyCodes.map((code) => (
              <option key={code} value={code}>
                {code} - {currencies[code].currencyName}
              </option>
            ))}
          </select>
        </div>
        <div className="col-md-4" style={{ display: "flex", alignItems: "flex-end" }}>
          <button
            className="btn btn-primary"
            onClick={handleCalculate}
            disabled={loading}
            data-testid="calculate-btn"
          >
            {loading ? "Calculating..." : "Get Exchange Rate"}
          </button>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger" role="alert" data-testid="error-message">
          {error}
        </div>
      )}

      {result && (
        <div className="alert alert-success" role="alert" data-testid="exchange-result">
          <strong>
            1 {result.fromCurrencyCode} = {result.exchangeRate}{" "}
            {result.toCurrencyCode}
          </strong>
        </div>
      )}
    </div>
  );
};

export default ExchangeRateCalculator;
