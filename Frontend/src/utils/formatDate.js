const fullFormatter = new Intl.DateTimeFormat("es-AR", {
  day: "2-digit",
  month: "2-digit",
  year: "numeric",
  hour: "2-digit",
  minute: "2-digit",
});

const shortFormatter = new Intl.DateTimeFormat("es", {
  day: "2-digit",
  month: "short",
});

export function formatDateTime(iso) {
  if (!iso) return "—";
  try {
    return fullFormatter.format(new Date(iso));
  } catch {
    return iso;
  }
}

export function formatDateShort(iso) {
  if (!iso) return "—";
  try {
    return shortFormatter.format(new Date(iso + "T00:00:00"));
  } catch {
    return iso;
  }
}
