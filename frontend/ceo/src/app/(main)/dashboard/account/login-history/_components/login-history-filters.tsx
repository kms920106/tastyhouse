"use client";

import type { DateRange } from "react-day-picker";

import { DateRangePicker } from "@/components/date-range-picker";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { CEO_LOGIN_RESULT_OPTIONS } from "@/feature/ceo/constants";
import { CEO_LOGIN_HISTORY_COPY } from "@/feature/ceo/message";
import { formatDate } from "@/lib/date";

/**
 * Radix `Select` 의 `value` 는 항상 string 이어야 하므로(`frontend/ceo/CLAUDE.md`)
 * "전체"를 `undefined` 가 아니라 이 sentinel 로 다루고, 상위로 올릴 때만 `null` 로 바꿔
 * `params.delete` 를 태운다.
 */
const ALL_VALUE = "ALL";

/**
 * `yyyy-MM-dd` 문자열을 로컬 자정 Date 로 되돌린다.
 *
 * `new Date("2026-08-12")` 는 UTC 자정으로 파싱돼 KST 에서 하루 앞의 날짜가 되므로
 * 쓰지 않고 연·월·일을 직접 넘긴다(`formatDate` 가 `toISOString` 을 피하는 것과 같은 이유).
 */
function parseLocalDate(value: string | undefined): Date | undefined {
  if (!value) return undefined;
  const [year, month, day] = value.split("-").map(Number);
  return new Date(year, month - 1, day);
}

interface LoginHistoryFiltersProps {
  result?: string;
  startDate?: string;
  endDate?: string;
  /** 보관 기간(오늘 - 90일 ~ 오늘). 브라우저 1차 방어이고 진짜 방어선은 서버다 */
  minDate: string;
  maxDate: string;
  disabled?: boolean;
  onResultChange: (result: string | null) => void;
  onPeriodChange: (period: { startDate: string | null; endDate: string | null }) => void;
  onSearch: () => void;
  onReset: () => void;
}

export function LoginHistoryFilters({
  result,
  startDate,
  endDate,
  minDate,
  maxDate,
  disabled,
  onResultChange,
  onPeriodChange,
  onSearch,
  onReset,
}: LoginHistoryFiltersProps) {
  // URL 이 단일 진실원이므로 피커를 완전 제어(controlled)로 둔다 — 값을 넘기지 않으면
  // 피커가 자체 기본값(최근 30일)을 표시해 URL 에 없는 필터가 걸린 것처럼 보인다.
  const dateRange: DateRange | undefined =
    startDate || endDate ? { from: parseLocalDate(startDate), to: parseLocalDate(endDate) } : undefined;

  // 역순 기간은 애초에 만들지 못하게 하고, 그래도 URL 로 들어왔으면 조회 버튼을 잠근다.
  const isRangeInverted = Boolean(startDate && endDate && startDate > endDate);
  const isSearchDisabled = disabled === true || isRangeInverted;

  return (
    <div className="flex flex-col gap-3 md:flex-row md:items-end">
      <div className="flex flex-1 flex-col gap-2">
        <Label htmlFor="login-history-result">{CEO_LOGIN_HISTORY_COPY.FILTER_RESULT_LABEL}</Label>
        <Select
          value={result ?? ALL_VALUE}
          onValueChange={(value) => onResultChange(value === ALL_VALUE ? null : value)}
          disabled={disabled}
        >
          <SelectTrigger id="login-history-result" className="w-full">
            <SelectValue />
          </SelectTrigger>
          <SelectContent position="popper" align="start">
            <SelectGroup>
              <SelectItem value={ALL_VALUE}>{CEO_LOGIN_HISTORY_COPY.FILTER_ALL}</SelectItem>
              {CEO_LOGIN_RESULT_OPTIONS.map((option) => (
                <SelectItem key={option.code} value={option.code}>
                  {option.name}
                </SelectItem>
              ))}
            </SelectGroup>
          </SelectContent>
        </Select>
      </div>

      <div className="flex flex-col gap-2">
        {/* DateRangePicker 트리거의 id 는 "date" 로 고정돼 있다 */}
        <Label htmlFor="date">{CEO_LOGIN_HISTORY_COPY.FILTER_PERIOD_LABEL}</Label>
        <DateRangePicker
          value={dateRange}
          disabled={disabled}
          placeholder={CEO_LOGIN_HISTORY_COPY.FILTER_ALL}
          minDate={minDate}
          maxDate={maxDate}
          onChange={(nextRange) =>
            onPeriodChange({
              startDate: nextRange?.from ? formatDate(nextRange.from) : null,
              endDate: nextRange?.to ? formatDate(nextRange.to) : null,
            })
          }
        />
      </div>

      {/* 필터는 변경 즉시 반영되므로 이 버튼은 시각적 확인용 재조회다. */}
      <Button type="button" variant="outline" onClick={onSearch} disabled={isSearchDisabled}>
        {CEO_LOGIN_HISTORY_COPY.SEARCH}
      </Button>
      <Button type="button" variant="outline" onClick={onReset} disabled={disabled}>
        {CEO_LOGIN_HISTORY_COPY.RESET}
      </Button>
    </div>
  );
}
