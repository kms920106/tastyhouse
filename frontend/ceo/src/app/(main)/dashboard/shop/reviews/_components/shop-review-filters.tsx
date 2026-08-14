"use client";

import type { DateRange } from "react-day-picker";

import { DateRangePicker } from "@/components/date-range-picker";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  ORDER_METHOD_OPTIONS,
  RATING_FILTER_OPTIONS,
  REVIEW_SORT_TYPE_OPTIONS,
  SHOP_REVIEW_TAB_OPTIONS,
} from "@/feature/shop-review/constants";
import type { ShopReviewTab } from "@/feature/shop-review/domain";
import { SHOP_REVIEW_COPY } from "@/feature/shop-review/message";
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

interface ShopReviewFiltersProps {
  tab: ShopReviewTab;
  startDate?: string;
  endDate?: string;
  rating?: number;
  orderMethod?: string;
  hasImage?: boolean;
  sortType?: string;
  disabled?: boolean;
  onTabChange: (tab: string) => void;
  onPeriodChange: (period: { startDate: string | null; endDate: string | null }) => void;
  onRatingChange: (rating: number | null) => void;
  onOrderMethodChange: (orderMethod: string | null) => void;
  /** 체크 해제는 `null` 로 올려 URL 에서 키를 지운다 — `false` 를 명시하면 서버가 필터로 받아들인다 */
  onHasImageChange: (hasImage: true | null) => void;
  onSortTypeChange: (sortType: string | null) => void;
  onReset: () => void;
}

export function ShopReviewFilters({
  tab,
  startDate,
  endDate,
  rating,
  orderMethod,
  hasImage,
  sortType,
  disabled,
  onTabChange,
  onPeriodChange,
  onRatingChange,
  onOrderMethodChange,
  onHasImageChange,
  onSortTypeChange,
  onReset,
}: ShopReviewFiltersProps) {
  // URL 이 단일 진실원이므로 피커를 완전 제어(controlled)로 둔다 — 값을 넘기지 않으면
  // 피커가 자체 기본값(최근 30일)을 표시해 URL 에 없는 필터가 걸린 것처럼 보인다.
  const dateRange: DateRange | undefined =
    startDate || endDate ? { from: parseLocalDate(startDate), to: parseLocalDate(endDate) } : undefined;

  return (
    <div className="flex flex-col gap-3">
      <Tabs value={tab} onValueChange={onTabChange}>
        <TabsList>
          {SHOP_REVIEW_TAB_OPTIONS.map((option) => (
            <TabsTrigger key={option.value} value={option.value} disabled={disabled}>
              {option.label}
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>

      <div className="flex flex-col gap-3 md:flex-row md:flex-wrap md:items-end">
        <div className="flex flex-col gap-2">
          {/* DateRangePicker 트리거의 id 는 "date" 로 고정돼 있다 */}
          <Label htmlFor="date">{SHOP_REVIEW_COPY.FILTER_PERIOD_LABEL}</Label>
          <DateRangePicker
            value={dateRange}
            disabled={disabled}
            placeholder={SHOP_REVIEW_COPY.FILTER_ALL}
            onChange={(nextRange) =>
              onPeriodChange({
                startDate: nextRange?.from ? formatDate(nextRange.from) : null,
                endDate: nextRange?.to ? formatDate(nextRange.to) : null,
              })
            }
          />
        </div>

        <div className="flex flex-1 flex-col gap-2">
          <Label htmlFor="shop-review-rating">{SHOP_REVIEW_COPY.FILTER_RATING_LABEL}</Label>
          <Select
            value={rating === undefined ? ALL_VALUE : String(rating)}
            onValueChange={(value) => onRatingChange(value === ALL_VALUE ? null : Number(value))}
            disabled={disabled}
          >
            <SelectTrigger id="shop-review-rating" className="w-full">
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value={ALL_VALUE}>{SHOP_REVIEW_COPY.FILTER_ALL}</SelectItem>
                {RATING_FILTER_OPTIONS.map((option) => (
                  <SelectItem key={option} value={String(option)}>
                    {option}
                    {SHOP_REVIEW_COPY.RATING_SUFFIX}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
        </div>

        <div className="flex flex-1 flex-col gap-2">
          <Label htmlFor="shop-review-order-method">{SHOP_REVIEW_COPY.FILTER_ORDER_METHOD_LABEL}</Label>
          <Select
            value={orderMethod ?? ALL_VALUE}
            onValueChange={(value) => onOrderMethodChange(value === ALL_VALUE ? null : value)}
            disabled={disabled}
          >
            <SelectTrigger id="shop-review-order-method" className="w-full">
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value={ALL_VALUE}>{SHOP_REVIEW_COPY.FILTER_ALL}</SelectItem>
                {ORDER_METHOD_OPTIONS.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
        </div>

        <div className="flex flex-1 flex-col gap-2">
          <Label htmlFor="shop-review-sort-type">{SHOP_REVIEW_COPY.FILTER_SORT_TYPE_LABEL}</Label>
          <Select
            value={sortType ?? ALL_VALUE}
            onValueChange={(value) => onSortTypeChange(value === ALL_VALUE ? null : value)}
            disabled={disabled}
          >
            <SelectTrigger id="shop-review-sort-type" className="w-full">
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                {/* 정렬 "전체"는 필터 해제 = 점주 저장 설정을 따른다는 뜻이다 */}
                <SelectItem value={ALL_VALUE}>{SHOP_REVIEW_COPY.FILTER_ALL}</SelectItem>
                {REVIEW_SORT_TYPE_OPTIONS.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
        </div>

        <div className="flex items-center gap-2 md:h-9">
          <Checkbox
            id="shop-review-has-image"
            checked={hasImage === true}
            disabled={disabled}
            onCheckedChange={(checked) => onHasImageChange(checked === true ? true : null)}
          />
          <Label htmlFor="shop-review-has-image" className="font-normal">
            {SHOP_REVIEW_COPY.FILTER_HAS_IMAGE_LABEL}
          </Label>
        </div>

        <Button type="button" variant="outline" onClick={onReset} disabled={disabled}>
          {SHOP_REVIEW_COPY.FILTER_RESET}
        </Button>
      </div>
    </div>
  );
}
