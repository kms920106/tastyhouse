"use client";

import * as React from "react";

import { Search } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Empty, EmptyHeader, EmptyTitle } from "@/components/ui/empty";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { AVAILABILITY_KEYWORD_MAX_LENGTH, OPTION_GROUP_TYPES } from "@/feature/product/constants";
import type { MenuOptionGroup } from "@/feature/product/domain";
import {
  OPTION_GROUP_MERGE_COPY,
  OPTION_GROUP_SCREEN_COPY,
  PRODUCT_OPTION_GROUP_COPY,
} from "@/feature/product/message";

interface MergeManualPickerProps {
  optionGroups: MenuOptionGroup[];
  /** URL 로 승격된 검색어. 입력 중인 값이 아니라 서버가 이미 반영한 값이다 */
  keyword?: string;
  selectedIds: number[];
  disabled?: boolean;
  onKeywordSubmit: (keyword: string | undefined) => void;
  onToggle: (optionGroupId: number, checked: boolean) => void;
}

/**
 * 직접 선택 — 합칠 옵션그룹을 체크박스로 고른다.
 *
 * 검색은 로컬 상태로 두고 **제출(Enter) 시점에만** URL 로 승격한다 —
 * `availability-filter-bar.tsx` 와 같은 이유로, 타이핑마다 서버 조회가 일어나는 것을 막는다.
 */
export function MergeManualPicker({
  optionGroups,
  keyword,
  selectedIds,
  disabled,
  onKeywordSubmit,
  onToggle,
}: MergeManualPickerProps) {
  const [draftKeyword, setDraftKeyword] = React.useState(keyword ?? "");

  // 뒤로가기 등으로 URL 이 바뀌면 입력값도 따라가야 한다.
  React.useEffect(() => {
    setDraftKeyword(keyword ?? "");
  }, [keyword]);

  function submitKeyword(event: React.FormEvent) {
    event.preventDefault();
    const trimmed = draftKeyword.trim();
    onKeywordSubmit(trimmed === "" ? undefined : trimmed);
  }

  return (
    <div className="flex flex-col gap-4">
      <form className="flex w-full gap-2 md:w-80" onSubmit={submitKeyword}>
        <Input
          value={draftKeyword}
          maxLength={AVAILABILITY_KEYWORD_MAX_LENGTH}
          placeholder={OPTION_GROUP_MERGE_COPY.SEARCH_PLACEHOLDER}
          disabled={disabled}
          onChange={(event) => setDraftKeyword(event.target.value)}
        />
        <Button type="submit" variant="outline" disabled={disabled}>
          <Search />
          <span className="sr-only">{OPTION_GROUP_MERGE_COPY.SEARCH_SUBMIT}</span>
        </Button>
      </form>

      <div className="flex flex-wrap items-center gap-2">
        <span className="font-medium text-sm">{OPTION_GROUP_MERGE_COPY.MANUAL_SELECT_LABEL}</span>
        <span className="text-muted-foreground text-xs">
          {OPTION_GROUP_MERGE_COPY.MANUAL_SELECTED_COUNT(selectedIds.length)}
        </span>
        <span className="text-muted-foreground text-xs">{OPTION_GROUP_MERGE_COPY.MANUAL_SELECT_HELP}</span>
      </div>

      {optionGroups.length === 0 ? (
        <Empty>
          <EmptyHeader>
            <EmptyTitle>
              {keyword === undefined
                ? OPTION_GROUP_MERGE_COPY.EMPTY_GROUPS
                : OPTION_GROUP_MERGE_COPY.EMPTY_SEARCH_RESULT}
            </EmptyTitle>
          </EmptyHeader>
        </Empty>
      ) : (
        <ul className="flex flex-col">
          {optionGroups.map((group) => {
            const inputId = `merge-pick-${group.id}`;

            return (
              <li key={group.id} className="flex items-center gap-3 border-b py-2 last:border-b-0">
                <Checkbox
                  id={inputId}
                  checked={selectedIds.includes(group.id)}
                  disabled={disabled}
                  onCheckedChange={(checked) => onToggle(group.id, checked === true)}
                />
                <Label htmlFor={inputId} className="flex min-w-0 flex-1 flex-wrap items-center gap-2 font-normal">
                  <span className="truncate text-sm">{group.name}</span>
                  {/* 유형이 다르면 서버가 합치기를 거부하므로(TYPE_MISMATCH) 고르기 전에 보여준다. */}
                  {group.groupType === OPTION_GROUP_TYPES.CUP_DEPOSIT && (
                    <Badge variant="outline">{PRODUCT_OPTION_GROUP_COPY.BADGE_CUP_DEPOSIT}</Badge>
                  )}
                  <span className="text-muted-foreground text-xs">
                    {OPTION_GROUP_SCREEN_COPY.OPTION_COUNT(group.options.length)}
                  </span>
                  <span className="text-muted-foreground text-xs">
                    {OPTION_GROUP_SCREEN_COPY.LINKED_COUNT(group.linkedProductCount)}
                  </span>
                </Label>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
