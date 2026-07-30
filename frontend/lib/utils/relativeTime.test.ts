import { getFullDate, getRelativeTime } from "./relativeTime";

const now = new Date("2026-07-30T20:57:31.000Z");
const isoOffset = (seconds: number): string =>
  new Date(now.getTime() - seconds * 1000).toISOString();

describe("getRelativeTime", () => {
  it.each([
    [10, "10 seconds ago"],
    [59, "59 seconds ago"],
    [60, "1 minute ago"],
    [3599, "59 minutes ago"],
    [3600, "1 hour ago"],
    [86399, "23 hours ago"],
    [86400, "1 day ago"],
    [604799, "6 days ago"],
    [604800, "1 week ago"],
    [2591999, "4 weeks ago"],
    [2592000, "1 month ago"],
    [31535999, "12 months ago"],
    [31536000, "1 year ago"],
  ])("formats %s seconds as %s", (seconds, expected) => {
    expect(getRelativeTime(isoOffset(seconds), now)).toBe(expected);
  });

  it("formats the API's millisecond ISO-8601 timestamp", () => {
    expect(
      getRelativeTime("2026-07-29T20:57:31.000Z", now)
    ).toBe("1 day ago");
  });

  it("handles very recent, future, invalid, and empty values safely", () => {
    expect(getRelativeTime(isoOffset(9), now)).toBe("just now");
    expect(
      getRelativeTime(new Date(now.getTime() + 60 * 1000).toISOString(), now)
    ).toBe("in 1 minute");
    expect(getRelativeTime("not a date", now)).toBe("");
    expect(getRelativeTime("", now)).toBe("");
  });
});

describe("getFullDate", () => {
  it("returns a human-readable tooltip date", () => {
    expect(getFullDate("2026-07-29T20:57:31.000Z")).toBe(
      new Date("2026-07-29T20:57:31.000Z").toLocaleString()
    );
  });

  it("handles invalid and empty values safely", () => {
    expect(getFullDate("not a date")).toBe("");
    expect(getFullDate("")).toBe("");
  });
});
