import React from "react";
import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import CurrencyList from "../../../components/currency/CurrencyList";
import { CurrencyInfo } from "../../../lib/api/currency";

const mockCurrencies: Record<string, CurrencyInfo> = {
  INR: { countryName: "INDIA", currencyCode: "INR", currencyName: "Indian Rupees" },
  USD: { countryName: "USA", currencyCode: "USD", currencyName: "US Dollars" },
};

describe("CurrencyList", () => {
  it("renders currency table with all entries", () => {
    render(<CurrencyList currencies={mockCurrencies} />);

    expect(screen.getByText("Available Currencies")).toBeInTheDocument();
    expect(screen.getByText("INR")).toBeInTheDocument();
    expect(screen.getByText("Indian Rupees")).toBeInTheDocument();
    expect(screen.getByText("INDIA")).toBeInTheDocument();
    expect(screen.getByText("USD")).toBeInTheDocument();
    expect(screen.getByText("US Dollars")).toBeInTheDocument();
    expect(screen.getByText("USA")).toBeInTheDocument();
  });

  it("renders table headers", () => {
    render(<CurrencyList currencies={mockCurrencies} />);

    expect(screen.getByText("Currency Code")).toBeInTheDocument();
    expect(screen.getByText("Currency Name")).toBeInTheDocument();
    expect(screen.getByText("Country")).toBeInTheDocument();
  });

  it("shows message when no currencies available", () => {
    render(<CurrencyList currencies={{}} />);

    expect(screen.getByText("No currencies available.")).toBeInTheDocument();
  });

  it("renders correct number of rows", () => {
    render(<CurrencyList currencies={mockCurrencies} />);

    const rows = screen.getAllByTestId(/currency-row-/);
    expect(rows).toHaveLength(2);
  });
});
