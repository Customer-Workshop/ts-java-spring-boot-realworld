const units = [
  { name: "year", seconds: 31536000 },
  { name: "month", seconds: 2592000 },
  { name: "week", seconds: 604800 },
  { name: "day", seconds: 86400 },
  { name: "hour", seconds: 3600 },
  { name: "minute", seconds: 60 },
  { name: "second", seconds: 1 },
];

export default function relativeTime(value: string): string {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) return "";

  const difference = date.getTime() - Date.now();
  const seconds = Math.abs(difference) / 1000;

  if (seconds < 45) return "just now";

  const unit = units.find(({ seconds: unitSeconds }) => seconds >= unitSeconds);
  const amount = Math.round(difference / 1000 / unit.seconds);

  const formatter = new (Intl as any).RelativeTimeFormat("en", {
    numeric: "always",
  });

  return formatter.format(amount, unit.name);
}
