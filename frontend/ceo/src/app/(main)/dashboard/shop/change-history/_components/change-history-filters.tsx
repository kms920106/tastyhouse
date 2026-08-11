"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import type { ShopChangeCategoryOption } from "@/feature/shop/domain";
import { SHOP_CHANGE_HISTORY_COPY } from "@/feature/shop/message";

/**
 * Radix `Select` 의 `value` 는 항상 string 이어야 하므로(`frontend/ceo/CLAUDE.md`)
 * "전체"를 `undefined` 가 아니라 이 sentinel 로 다루고, 상위로 올릴 때만 `null` 로 바꿔
 * `params.delete` 를 태운다.
 */
const ALL_VALUE = "ALL";

interface ChangeHistoryFiltersProps {
  categories: ShopChangeCategoryOption[];
  category?: string;
  changeType?: string;
  changedDate: string;
  minDate: string;
  maxDate: string;
  disabled?: boolean;
  onCategoryChange: (category: string | null) => void;
  onChangeTypeChange: (changeType: string | null) => void;
  onChangedDateChange: (changedDate: string) => void;
  onSearch: () => void;
}

export function ChangeHistoryFilters({
  categories,
  category,
  changeType,
  changedDate,
  minDate,
  maxDate,
  disabled,
  onCategoryChange,
  onChangeTypeChange,
  onChangedDateChange,
  onSearch,
}: ChangeHistoryFiltersProps) {
  // 대분류를 고르지 않았으면 전체 중분류를 평탄화해 보여준다.
  const selectedCategory = categories.find((item) => item.code === category);
  const changeTypeOptions = selectedCategory
    ? selectedCategory.changeTypes
    : categories.flatMap((item) => item.changeTypes);

  return (
    <div className="flex flex-col gap-3 md:flex-row md:items-end">
      <div className="flex flex-1 flex-col gap-2">
        <Label htmlFor="change-history-category">{SHOP_CHANGE_HISTORY_COPY.CATEGORY_LABEL}</Label>
        <Select
          value={category ?? ALL_VALUE}
          onValueChange={(value) => onCategoryChange(value === ALL_VALUE ? null : value)}
          disabled={disabled}
        >
          <SelectTrigger id="change-history-category" className="w-full">
            <SelectValue />
          </SelectTrigger>
          <SelectContent position="popper" align="start">
            <SelectGroup>
              <SelectItem value={ALL_VALUE}>{SHOP_CHANGE_HISTORY_COPY.FILTER_ALL}</SelectItem>
              {categories.map((item) => (
                <SelectItem key={item.code} value={item.code}>
                  {item.name}
                </SelectItem>
              ))}
            </SelectGroup>
          </SelectContent>
        </Select>
      </div>

      <div className="flex flex-1 flex-col gap-2">
        <Label htmlFor="change-history-change-type">{SHOP_CHANGE_HISTORY_COPY.CHANGE_TYPE_LABEL}</Label>
        <Select
          value={changeType ?? ALL_VALUE}
          onValueChange={(value) => onChangeTypeChange(value === ALL_VALUE ? null : value)}
          disabled={disabled}
        >
          <SelectTrigger id="change-history-change-type" className="w-full">
            <SelectValue />
          </SelectTrigger>
          <SelectContent position="popper" align="start">
            <SelectGroup>
              <SelectItem value={ALL_VALUE}>{SHOP_CHANGE_HISTORY_COPY.FILTER_ALL}</SelectItem>
              {changeTypeOptions.map((item) => (
                <SelectItem key={item.code} value={item.code}>
                  {item.name}
                </SelectItem>
              ))}
            </SelectGroup>
          </SelectContent>
        </Select>
      </div>

      <div className="flex flex-col gap-2">
        <Label htmlFor="change-history-date">{SHOP_CHANGE_HISTORY_COPY.DATE_LABEL}</Label>
        {/* min/max 는 브라우저 단 1차 방어이고, 6개월 범위의 진짜 방어선은 서버 검증이다. */}
        <Input
          id="change-history-date"
          type="date"
          value={changedDate}
          min={minDate}
          max={maxDate}
          disabled={disabled}
          onChange={(event) => {
            if (event.target.value) onChangedDateChange(event.target.value);
          }}
          className="w-full md:w-44"
        />
      </div>

      {/* 필터는 변경 즉시 반영되므로 이 버튼은 시각적 확인용 재조회다. */}
      <Button type="button" variant="outline" onClick={onSearch} disabled={disabled}>
        {SHOP_CHANGE_HISTORY_COPY.SEARCH}
      </Button>
    </div>
  );
}
