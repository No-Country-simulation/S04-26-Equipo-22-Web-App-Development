function stripHtml(html) {
  if (typeof window === "undefined" || !html) return html || "";
  const tmp = document.createElement("div");
  tmp.innerHTML = html;
  return tmp.textContent || tmp.innerText || "";
}

function toMarkdown(draft, channel) {
  if (!draft || !draft.channels) return "";
  const ch = draft.channels[channel];
  if (!ch) return "";
  const title = ch.title ? `# ${ch.title}\n\n` : "";
  const meta = `> Canal: ${channel} · Semana: ${draft.weekOf} · Estado: ${ch.status}\n\n`;
  const body = stripHtml(ch.body).trim();
  return `${title}${meta}${body}\n`;
}

function toJson(draft, channel) {
  if (!draft || !draft.channels) return null;
  const ch = draft.channels[channel];
  if (!ch) return null;
  return {
    draftId: draft.id,
    weekOf: draft.weekOf,
    topicTitle: draft.topicTitle,
    topicSummary: draft.topicSummary,
    channel,
    title: ch.title || null,
    body: ch.body,
    status: ch.status,
    sourceContributions: draft.sourceContributions || [],
    exportedAt: new Date().toISOString(),
  };
}

function triggerDownload(filename, content, mime) {
  const blob = new Blob([content], { type: mime });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

export async function copyMarkdown(draft, channel) {
  if (!draft || !draft.channels) return "";
  const md = toMarkdown(draft, channel);
  await navigator.clipboard.writeText(md);
  return md;
}

export function downloadJson(draft, channel) {
  if (!draft || !draft.channels) return;
  const payload = toJson(draft, channel);
  if (!payload) return;
  const filename = `${draft.id}-${channel}.json`;
  triggerDownload(filename, JSON.stringify(payload, null, 2), "application/json");
}

export function downloadMarkdown(draft, channel) {
  if (!draft || !draft.channels) return;
  const md = toMarkdown(draft, channel);
  const filename = `${draft.id}-${channel}.md`;
  triggerDownload(filename, md, "text/markdown");
}

export function downloadTxt(draft, channel) {
  if (!draft || !draft.channels) return;
  const ch = draft.channels[channel];
  if (!ch) return;
  const title = ch.title ? `${ch.title}\n${"=".repeat(ch.title.length)}\n\n` : "";
  const meta = `Canal: ${channel} | Semana: ${draft.weekOf} | Estado: ${ch.status}\n\n`;
  const body = stripHtml(ch.body).trim();
  const txt = `${title}${meta}${body}\n`;
  const filename = `${draft.id}-${channel}.txt`;
  triggerDownload(filename, txt, "text/plain");
}

export function getPlainText(draft, channel) {
  if (!draft || !draft.channels) return "";
  const ch = draft.channels[channel];
  if (!ch) return "";
  return stripHtml(ch.body).trim();
}

export async function copyPlainText(draft, channel) {
  if (!draft || !draft.channels) return "";
  const text = getPlainText(draft, channel);
  await navigator.clipboard.writeText(text);
  return text;
}
