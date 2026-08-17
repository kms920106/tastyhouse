"use client";

import * as React from "react";

import { Search } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { AVAILABILITY_KEYWORD_MAX_LENGTH, AVAILABILITY_TABS } from "@/feature/product/constants";
import { PRODUCT_AVAILABILITY_COPY } from "@/feature/product/message";

import type { AvailabilityFilters } from "./availability-manage";

interface AvailabilityFilterBarProps {
  filters: AvailabilityFilters;
  disabled?: boolean;
  onChange: (next: Record<string, string | undefined>) => void;
}

export function AvailabilityFilterBar({ filters, disabled, onChange }: AvailabilityFilterBarProps) {
  // 검색어만 로컬 상태다 — 타이핑마다 URL 을 갱신하면 매 글자 서버 조회가 일어난다.
  // 확정(제출/Enter) 시점에 URL 로 승격한다.
  const [keyword, setKeyword] = React.useState(filters.keyword ?? "");

  // 뒤로가기·필터 초기화 등으로 URL 이 바뀌면 입력값도 따라가야 한다.
  React.useEffect(() => {
    setKeyword(filters.keyword ?? "");
  }, [filters.keyword]);

  function submitKeyword(event: React.FormEvent) {
    event.preventDefault();
    const trimmed = keyword.trim();
    onChange({ keyword: trimmed === "" ? undefined : trimmed });
  }

  /** 체크 해제는 파라미터를 지운다 — `false` 를 남기면 "미지정 = 전체"와 구분되지 않는다 */
  function toggleFilter(key: "soldOutOnly" | "hiddenOnly", checked: boolean) {
    onChange({ [key]: checked ? "true" : undefined });
  }

  return (
    <div className="flex flex-col gap-3">
      <Tabs value={filters.tab} onValueChange={(value) => onChange({ tab: value })}>
        <TabsList>
          <TabsTrigger value={AVAILABILITY_TABS.MENU} disabled={disabled}>
            {PRODUCT_AVAILABILITY_COPY.TAB_MENU}
          </TabsTrigger>
          <TabsTrigger value={AVAILABILITY_TABS.OPTION} disabled={disabled}>
            {PRODUCT_AVAILABILITY_COPY.TAB_OPTION}
          </TabsTrigger>
        </TabsList>
      </Tabs>

      <div className="flex flex-col gap-3 md:flex-row md:items-center">
        <form className="flex w-full gap-2 md:w-80" onSubmit={submitKeyword}>
          <Input
            value={keyword}
            maxLength={AVAILABILITY_KEYWORD_MAX_LENGTH}
            placeholder={PRODUCT_AVAILABILITY_COPY.SEARCH_PLACEHOLDER}
            disabled={disabled}
            onChange={(event) => setKeyword(event.target.value)}
          />
          <Button type="submit" variant="outline" disabled={disabled}>
            <Search />
            <span className="sr-only">{PRODUCT_AVAILABILITY_COPY.SEARCH_SUBMIT}</span>
          </Button>
        </form>

        <div className="flex items-center gap-6">
          <div className="flex items-center gap-2">
            <Checkbox
              id="availability-filter-sold-out"
              checked={filters.soldOutOnly === true}
              disabled={disabled}
              onCheckedChange={(checked) => toggleFilter("soldOutOnly", checked === true)}
            />
            <Label htmlFor="availability-filter-sold-out">{PRODUCT_AVAILABILITY_COPY.FILTER_SOLD_OUT}</Label>
          </div>
          <div className="flex items-center gap-2">
            <Checkbox
              id="availability-filter-hidden"
              checked={filters.hiddenOnly === true}
              disabled={disabled}
              onCheckedChange={(checked) => toggleFilter("hiddenOnly", checked === true)}
            />
            <Label htmlFor="availability-filter-hidden">{PRODUCT_AVAILABILITY_COPY.FILTER_HIDDEN}</Label>
          </div>
        </div>
      </div>
    </div>
  );
}
