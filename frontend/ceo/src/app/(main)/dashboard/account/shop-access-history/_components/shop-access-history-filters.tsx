"use client";

import type { DateRange } from "react-day-picker";

import { DateRangePicker } from "@/components/date-range-picker";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { CEO_SHOP_ACCESS_ACTION_TYPE_OPTIONS } from "@/feature/ceo/constants";
import { CEO_SHOP_ACCESS_HISTORY_COPY } from "@/feature/ceo/message";
import type { ShopSummary } from "@/feature/shop/domain";
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

interface ShopAccessHistoryFiltersProps {
  /** 가게 목록 조회가 실패하면 undefined — 가게 필터만 비활성화한다 */
  shops?: ShopSummary[];
  actionType?: string;
  shopId?: number;
  startDate?: string;
  endDate?: string;
  /** 보관 기간(오늘 - 5년 ~ 오늘). 브라우저 1차 방어이고 진짜 방어선은 서버다 */
  minDate: string;
  maxDate: string;
  disabled?: boolean;
  onActionTypeChange: (actionType: string | null) => void;
  onShopChange: (shopId: string | null) => void;
  onPeriodChange: (period: { startDate: string | null; endDate: string | null }) => void;
  onSearch: () => void;
  onReset: () => void;
}

export function ShopAccessHistoryFilters({
  shops,
  actionType,
  shopId,
  startDate,
  endDate,
  minDate,
  maxDate,
  disabled,
  onActionTypeChange,
  onShopChange,
  onPeriodChange,
  onSearch,
  onReset,
}: ShopAccessHistoryFiltersProps) {
  // URL 이 단일 진실원이므로 피커를 완전 제어(controlled)로 둔다 — 값을 넘기지 않으면
  // 피커가 자체 기본값(최근 30일)을 표시해 URL 에 없는 필터가 걸린 것처럼 보인다.
  const dateRange: DateRange | undefined =
    startDate || endDate ? { from: parseLocalDate(startDate), to: parseLocalDate(endDate) } : undefined;

  // 역순 기간은 애초에 만들지 못하게 하고, 그래도 URL 로 들어왔으면 조회 버튼을 잠근다.
  const isRangeInverted = Boolean(startDate && endDate && startDate > endDate);
  const isShopFilterAvailable = shops !== undefined;
  const isSearchDisabled = disabled === true || isRangeInverted;
  // 가게 목록 조회가 실패하면 가게 필터만 잠근다 — 목록 조회는 그대로 살린다.
  const isShopSelectDisabled = disabled === true || !isShopFilterAvailable;

  return (
    <div className="flex flex-col gap-3">
      <div className="flex flex-col gap-3 md:flex-row md:items-end">
        <div className="flex flex-1 flex-col gap-2">
          <Label htmlFor="shop-access-history-action-type">
            {CEO_SHOP_ACCESS_HISTORY_COPY.FILTER_ACTION_TYPE_LABEL}
          </Label>
          <Select
            value={actionType ?? ALL_VALUE}
            onValueChange={(value) => onActionTypeChange(value === ALL_VALUE ? null : value)}
            disabled={disabled}
          >
            <SelectTrigger id="shop-access-history-action-type" className="w-full">
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value={ALL_VALUE}>{CEO_SHOP_ACCESS_HISTORY_COPY.FILTER_ALL}</SelectItem>
                {CEO_SHOP_ACCESS_ACTION_TYPE_OPTIONS.map((option) => (
                  <SelectItem key={option.code} value={option.code}>
                    {option.name}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
        </div>

        <div className="flex flex-1 flex-col gap-2">
          <Label htmlFor="shop-access-history-shop">{CEO_SHOP_ACCESS_HISTORY_COPY.FILTER_SHOP_LABEL}</Label>
          {/* 현재 배정된 가게만 담긴다 — 이미 해제된 가게는 목록에 없으므로 아래 도움말로 안내한다. */}
          <Select
            value={shopId !== undefined ? String(shopId) : ALL_VALUE}
            onValueChange={(value) => onShopChange(value === ALL_VALUE ? null : value)}
            disabled={isShopSelectDisabled}
          >
            <SelectTrigger id="shop-access-history-shop" className="w-full">
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value={ALL_VALUE}>{CEO_SHOP_ACCESS_HISTORY_COPY.FILTER_ALL}</SelectItem>
                {(shops ?? []).map((shop) => (
                  <SelectItem key={shop.id} value={String(shop.id)}>
                    {shop.name}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
        </div>

        <div className="flex flex-col gap-2">
          {/* DateRangePicker 트리거의 id 는 "date" 로 고정돼 있다 */}
          <Label htmlFor="date">{CEO_SHOP_ACCESS_HISTORY_COPY.FILTER_PERIOD_LABEL}</Label>
          <DateRangePicker
            value={dateRange}
            disabled={disabled}
            placeholder={CEO_SHOP_ACCESS_HISTORY_COPY.FILTER_ALL}
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
          {CEO_SHOP_ACCESS_HISTORY_COPY.SEARCH}
        </Button>
        <Button type="button" variant="outline" onClick={onReset} disabled={disabled}>
          {CEO_SHOP_ACCESS_HISTORY_COPY.RESET}
        </Button>
      </div>

      <p className="text-muted-foreground text-xs">
        {isShopFilterAvailable
          ? CEO_SHOP_ACCESS_HISTORY_COPY.SHOP_FILTER_HINT
          : CEO_SHOP_ACCESS_HISTORY_COPY.SHOP_FILTER_UNAVAILABLE}
      </p>
    </div>
  );
}
