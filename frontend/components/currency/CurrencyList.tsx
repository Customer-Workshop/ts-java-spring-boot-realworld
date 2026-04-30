import React from "react";
import { CurrencyInfo } from "../../lib/api/currency";

interface CurrencyListProps {
  currencies: Record<string, CurrencyInfo>;
}

/**
 * Displays all available currencies in a table format.
 */
const CurrencyList: React.FC<CurrencyListProps> = ({ currencies }) => {
  const entries = Object.entries(currencies);

  if (entries.length === 0) {
    return <p>No currencies available.</p>;
  }

  return (
    <div className="currency-list">
      <h2>Available Currencies</h2>
      <table className="table table-striped" style={{ width: "100%" }}>
        <thead>
          <tr>
            <th>Currency Code</th>
            <th>Currency Name</th>
            <th>Country</th>
          </tr>
        </thead>
        <tbody>
          {entries.map(([code, info]) => (
            <tr key={code} data-testid={`currency-row-${code}`}>
              <td>{info.currencyCode}</td>
              <td>{info.currencyName}</td>
              <td>{info.countryName}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default CurrencyList;
