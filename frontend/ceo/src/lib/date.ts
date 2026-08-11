/**
 * ISO-8601 LocalDateTime 문자열(예: "2026-01-01T00:00:00")을
 * 화면 표시용 문자열로 변환한다.
 */
export function formatDateTime(value: string | null | undefined): string {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;

  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const dd = String(date.getDate()).padStart(2, "0");
  const hh = String(date.getHours()).padStart(2, "0");
  const min = String(date.getMinutes()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd} ${hh}:${min}`;
}

/**
 * Date 를 `<input type="date">` 가 요구하는 `"YYYY-MM-DD"` 로 변환한다.
 *
 * `toISOString()` 은 UTC 로 변환해 KST 자정 전후에 하루가 밀리므로 쓰지 않고
 * 로컬 시각 기준으로 직접 조립한다.
 */
export function formatDate(value: Date): string {
  const yyyy = value.getFullYear();
  const mm = String(value.getMonth() + 1).padStart(2, "0");
  const dd = String(value.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
}
