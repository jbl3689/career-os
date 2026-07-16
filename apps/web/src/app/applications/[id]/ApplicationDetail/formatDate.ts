export function formatDate(date: string) {
  return new Intl.DateTimeFormat("en-GB", { dateStyle: "long" }).format(
    new Date(`${date}T00:00:00`),
  );
}
