/** 版本号解析结果：前缀 + 数字段 + 后缀 */
export type ParsedVersion = {
  prefix: string;
  segments: string[];
  suffix: string;
};

/** 解析版本号，保留前缀（如 v）与后缀（如 -beta），数字段拆分为数组；无法解析时返回 null */
export function parseVersion(raw: string): ParsedVersion | null {
  const trimmed = raw.trim();
  if (!trimmed) return null;
  const m = trimmed.match(/^([^\d]*?)((?:\d+)(?:\.\d+)*)([\s\S]*)$/);
  if (!m) return null;
  return {
    prefix: m[1],
    segments: m[2].split('.').map(seg => String(Number.parseInt(seg, 10))),
    suffix: m[3]
  };
}

/** 将解析结果重新拼回版本号字符串 */
export function buildVersion(p: ParsedVersion): string {
  return p.prefix + p.segments.join('.') + p.suffix;
}

/** 根据上一版本号自动生成下一版本（末尾数字段 +1）；无上一版本或无法解析时返回默认 v1.0.0 */
export function nextVersionTag(tag?: string): string {
  if (!tag) return 'v1.0.0';
  const p = parseVersion(tag);
  if (!p || p.segments.length === 0) return 'v1.0.0';
  const last = p.segments.length - 1;
  p.segments[last] = String((Number.parseInt(p.segments[last], 10) || 0) + 1);
  return buildVersion(p);
}
