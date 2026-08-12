"use client";

import type { DateRange } from "react-day-picker";

import { DateRangePicker } from "@/components/date-range-picker";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import type { ShopRequestStatusOption, ShopRequestTypeOption } from "@/feature/shop/domain";
import { SHOP_REQUEST_COPY } from "@/feature/shop/message";
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

interface ShopRequestFiltersProps {
  requestTypes: ShopRequestTypeOption[];
  statuses: ShopRequestStatusOption[];
  requestType?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
  disabled?: boolean;
  onRequestTypeChange: (requestType: string | null) => void;
  onStatusChange: (status: string | null) => void;
  onPeriodChange: (period: { startDate: string | null; endDate: string | null }) => void;
  onReset: () => void;
}

export function ShopRequestFilters({
  requestTypes,
  statuses,
  requestType,
  status,
  startDate,
  endDate,
  disabled,
  onRequestTypeChange,
  onStatusChange,
  onPeriodChange,
  onReset,
}: ShopRequestFiltersProps) {
  // URL 이 단일 진실원이므로 피커를 완전 제어(controlled)로 둔다 — 값을 넘기지 않으면
  // 피커가 자체 기본값(최근 30일)을 표시해 URL 에 없는 필터가 걸린 것처럼 보인다.
  const dateRange: DateRange | undefined =
    startDate || endDate ? { from: parseLocalDate(startDate), to: parseLocalDate(endDate) } : undefined;

  return (
    <div className="flex flex-col gap-3 md:flex-row md:items-end">
      <div className="flex flex-1 flex-col gap-2">
        <Label htmlFor="shop-request-type">{SHOP_REQUEST_COPY.FILTER_TYPE_LABEL}</Label>
        <Select
          value={requestType ?? ALL_VALUE}
          onValueChange={(value) => onRequestTypeChange(value === ALL_VALUE ? null : value)}
          disabled={disabled}
        >
          <SelectTrigger id="shop-request-type" className="w-full">
            <SelectValue />
          </SelectTrigger>
          <SelectContent position="popper" align="start">
            <SelectGroup>
              <SelectItem value={ALL_VALUE}>{SHOP_REQUEST_COPY.FILTER_ALL}</SelectItem>
              {requestTypes.map((item) => (
                <SelectItem key={item.code} value={item.code}>
                  {item.description}
                </SelectItem>
              ))}
            </SelectGroup>
          </SelectContent>
        </Select>
      </div>

      <div className="flex flex-1 flex-col gap-2">
        <Label htmlFor="shop-request-status">{SHOP_REQUEST_COPY.FILTER_STATUS_LABEL}</Label>
        <Select
          value={status ?? ALL_VALUE}
          onValueChange={(value) => onStatusChange(value === ALL_VALUE ? null : value)}
          disabled={disabled}
        >
          <SelectTrigger id="shop-request-status" className="w-full">
            <SelectValue />
          </SelectTrigger>
          <SelectContent position="popper" align="start">
            <SelectGroup>
              <SelectItem value={ALL_VALUE}>{SHOP_REQUEST_COPY.FILTER_ALL}</SelectItem>
              {statuses.map((item) => (
                <SelectItem key={item.code} value={item.code}>
                  {item.description}
                </SelectItem>
              ))}
            </SelectGroup>
          </SelectContent>
        </Select>
      </div>

      <div className="flex flex-col gap-2">
        {/* DateRangePicker 트리거의 id 는 "date" 로 고정돼 있다 */}
        <Label htmlFor="date">{SHOP_REQUEST_COPY.FILTER_PERIOD_LABEL}</Label>
        {/* 시작·종료 두 값을 다루므로 변경이력의 단일 `<input type="date">` 와 다르다. */}
        <DateRangePicker
          value={dateRange}
          disabled={disabled}
          placeholder={SHOP_REQUEST_COPY.FILTER_ALL}
          onChange={(nextRange) =>
            onPeriodChange({
              startDate: nextRange?.from ? formatDate(nextRange.from) : null,
              endDate: nextRange?.to ? formatDate(nextRange.to) : null,
            })
          }
        />
      </div>

      <Button type="button" variant="outline" onClick={onReset} disabled={disabled}>
        {SHOP_REQUEST_COPY.FILTER_RESET}
      </Button>
    </div>
  );
}
