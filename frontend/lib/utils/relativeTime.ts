const UNITS = [
  { name: "year", seconds: 365 * 24 * 60 * 60 },
  { name: "month", seconds: 30 * 24 * 60 * 60 },
  { name: "week", seconds: 7 * 24 * 60 * 60 },
  { name: "day", seconds: 24 * 60 * 60 },
  { name: "hour", seconds: 60 * 60 },
  { name: "minute", seconds: 60 },
  { name: "second", seconds: 1 },
] as const;

const relativeTimeFormatter = new Intl.RelativeTimeFormat(undefined, {
  numeric: "always",
});

export const getRelativeTime = (
  value: string,
  now: Date = new Date()
): string => {
  if (!value) return "";

  const date = new Date(value);
  if (Number.isNaN(date.getTime()) || Number.isNaN(now.getTime())) return "";

  const differenceInSeconds = (date.getTime() - now.getTime()) / 1000;
  if (Math.abs(differenceInSeconds) < 10) return "just now";

  const unit = UNITS.find(
    ({ seconds }) => Math.abs(differenceInSeconds) >= seconds
  );
  if (!unit) return "just now";

  const amount =
    Math.floor(Math.abs(differenceInSeconds) / unit.seconds) *
    Math.sign(differenceInSeconds);
  return relativeTimeFormatter.format(amount, unit.name);
};

export const getFullDate = (value: string): string => {
  if (!value) return "";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";

  return date.toLocaleString();
};
