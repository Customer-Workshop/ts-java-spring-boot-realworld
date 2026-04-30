import Head from "next/head";
import React, { useEffect, useState } from "react";

import CurrencyList from "../components/currency/CurrencyList";
import ExchangeRateCalculator from "../components/currency/ExchangeRateCalculator";
import { CurrencyInfo, fetchCurrencies } from "../lib/api/currency";

/**
 * Currency Exchange page. Displays all available currencies in a table
 * and provides an exchange rate calculator.
 */
const CurrencyPage = () => {
  const [currencies, setCurrencies] = useState<Record<string, CurrencyInfo>>(
    {}
  );
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchCurrencies()
      .then((data) => {
        setCurrencies(data);
        setLoading(false);
      })
      .catch(() => {
        setError("Failed to load currencies.");
        setLoading(false);
      });
  }, []);

  return (
    <>
      <Head>
        <title>Currency Exchange</title>
        <meta name="description" content="Currency exchange rate calculator" />
      </Head>
      <div className="container page" style={{ paddingTop: "2rem" }}>
        <h1>Currency Exchange</h1>
        {loading && <p>Loading currencies...</p>}
        {error && (
          <div className="alert alert-danger" role="alert">
            {error}
          </div>
        )}
        {!loading && !error && (
          <>
            <CurrencyList currencies={currencies} />
            <ExchangeRateCalculator currencies={currencies} />
          </>
        )}
      </div>
    </>
  );
};

export default CurrencyPage;
