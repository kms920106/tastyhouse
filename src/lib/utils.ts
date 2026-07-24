import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/** 현재 시각을 epoch milliseconds 로 반환 (요청 소요 시간 측정 등에 사용). */
export function getEpochMs(): number {
  return Date.now();
}

/**
 * 쿼리스트링 등에서 받은 값을 0 이상의 정수로 파싱한다.
 * 정수가 아니거나 음수면 fallback 을 반환한다. (page/size 같은 페이지네이션 파라미터용)
 * searchParams 는 같은 키가 배열로 올 수 있어(`?page=1&page=2`) 배열이면 첫 값을 사용한다.
 */
export function parseNonNegativeInt(value: string | string[] | undefined, fallback: number): number {
  const raw = Array.isArray(value) ? value[0] : value;
  const parsed = Number(raw);
  return Number.isInteger(parsed) && parsed >= 0 ? parsed : fallback;
}

/** searchParams 문자열을 trim 후 비어있지 않으면 반환, 아니면 undefined. (검색어용) */
export function parseSearchString(value: string | string[] | undefined): string | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  const trimmed = raw?.trim();
  return trimmed ? trimmed : undefined;
}

/** "true"/"false" 만 boolean 으로, 그 외(생략 포함)는 undefined. (선택 필터용) */
export function parseOptionalBoolean(value: string | string[] | undefined): boolean | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  if (raw === "true") return true;
  if (raw === "false") return false;
  return undefined;
}

export const getInitials = (str: string): string => {
  if (typeof str !== "string" || !str.trim()) return "?";

  return (
    str
      .trim()
      .split(/\s+/)
      .filter(Boolean)
      .map((word) => word[0])
      .join("")
      .toUpperCase() || "?"
  );
};

export function formatCurrency(
  amount: number,
  opts?: {
    currency?: string;
    locale?: string;
    minimumFractionDigits?: number;
    maximumFractionDigits?: number;
    noDecimals?: boolean;
  },
) {
  const { currency = "USD", locale = "en-US", minimumFractionDigits, maximumFractionDigits, noDecimals } = opts ?? {};

  const formatOptions: Intl.NumberFormatOptions = {
    style: "currency",
    currency,
    minimumFractionDigits: noDecimals ? 0 : minimumFractionDigits,
    maximumFractionDigits: noDecimals ? 0 : maximumFractionDigits,
  };

  return new Intl.NumberFormat(locale, formatOptions).format(amount);
}
