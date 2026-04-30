import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import ExchangeRateCalculator from "../../../components/currency/ExchangeRateCalculator";
import { CurrencyInfo } from "../../../lib/api/currency";
import * as currencyApi from "../../../lib/api/currency";

const mockCurrencies: Record<string, CurrencyInfo> = {
  INR: { countryName: "INDIA", currencyCode: "INR", currencyName: "Indian Rupees" },
  USD: { countryName: "USA", currencyCode: "USD", currencyName: "US Dollars" },
  CAD: { countryName: "CANADA", currencyCode: "CAD", currencyName: "Canadian Dollars" },
};

jest.mock("../../../lib/api/currency", () => ({
  ...jest.requireActual("../../../lib/api/currency"),
  fetchExchangeRate: jest.fn(),
}));

describe("ExchangeRateCalculator", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders form with currency dropdowns", () => {
    render(<ExchangeRateCalculator currencies={mockCurrencies} />);

    expect(screen.getByText("Exchange Rate Calculator")).toBeInTheDocument();
    expect(screen.getByTestId("from-currency-select")).toBeInTheDocument();
    expect(screen.getByTestId("to-currency-select")).toBeInTheDocument();
    expect(screen.getByTestId("calculate-btn")).toBeInTheDocument();
  });

  it("shows error when currencies not selected", async () => {
    render(<ExchangeRateCalculator currencies={mockCurrencies} />);

    fireEvent.click(screen.getByTestId("calculate-btn"));

    expect(screen.getByTestId("error-message")).toHaveTextContent(
      "Please select both currencies."
    );
  });

  it("shows error when same currency selected", async () => {
    render(<ExchangeRateCalculator currencies={mockCurrencies} />);

    fireEvent.change(screen.getByTestId("from-currency-select"), {
      target: { value: "USD" },
    });
    fireEvent.change(screen.getByTestId("to-currency-select"), {
      target: { value: "USD" },
    });
    fireEvent.click(screen.getByTestId("calculate-btn"));

    expect(screen.getByTestId("error-message")).toHaveTextContent(
      "Please select different currencies."
    );
  });

  it("displays exchange rate result on success", async () => {
    (currencyApi.fetchExchangeRate as jest.Mock).mockResolvedValue({
      fromCurrencyCode: "USD",
      toCurrencyCode: "INR",
      exchangeRate: "80.08",
    });

    render(<ExchangeRateCalculator currencies={mockCurrencies} />);

    fireEvent.change(screen.getByTestId("from-currency-select"), {
      target: { value: "USD" },
    });
    fireEvent.change(screen.getByTestId("to-currency-select"), {
      target: { value: "INR" },
    });
    fireEvent.click(screen.getByTestId("calculate-btn"));

    await waitFor(() => {
      expect(screen.getByTestId("exchange-result")).toHaveTextContent(
        "1 USD = 80.08 INR"
      );
    });
  });

  it("displays error when exchange rate not found", async () => {
    (currencyApi.fetchExchangeRate as jest.Mock).mockRejectedValue(
      new Error("Not found")
    );

    render(<ExchangeRateCalculator currencies={mockCurrencies} />);

    fireEvent.change(screen.getByTestId("from-currency-select"), {
      target: { value: "USD" },
    });
    fireEvent.change(screen.getByTestId("to-currency-select"), {
      target: { value: "CAD" },
    });
    fireEvent.click(screen.getByTestId("calculate-btn"));

    await waitFor(() => {
      expect(screen.getByTestId("error-message")).toHaveTextContent(
        "Exchange rate not found for the selected pair."
      );
    });
  });
});
