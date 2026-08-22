"use client";

import { X } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Empty, EmptyDescription, EmptyHeader, EmptyTitle } from "@/components/ui/empty";
import type { OptionGroupMergeSuggestion } from "@/feature/product/domain";
import { formatPrice } from "@/feature/product/format";
import { OPTION_GROUP_MERGE_COPY } from "@/feature/product/message";

interface MergeSuggestionListProps {
  suggestions: OptionGroupMergeSuggestion[];
  /**
   * 지금 기준 선택 단계로 넘어간 묶음의 서명.
   *
   * 다른 카드의 [이 묶음 합치기]를 누르면 이전 선택이 덮어써지므로, 어느 묶음이 활성인지
   * 카드에 표시하지 않으면 사용자가 무엇을 합치려는 중인지 알 수 없다.
   */
  selectedSignature?: string;
  disabled?: boolean;
  onExclude: (suggestion: OptionGroupMergeSuggestion) => void;
  onSelect: (suggestion: OptionGroupMergeSuggestion) => void;
}

/**
 * 추천 묶음 카드 목록.
 *
 * 카드 하나가 "이름·옵션 구성이 같은 옵션그룹 N개" 묶음이다. 어떤 그룹이 기준으로 살아남을지는
 * 이 화면이 정하지 않는다 — [이 묶음 합치기]를 누르면 기준 선택 단계로 넘긴다.
 */
export function MergeSuggestionList({
  suggestions,
  selectedSignature,
  disabled,
  onExclude,
  onSelect,
}: MergeSuggestionListProps) {
  if (suggestions.length === 0) {
    return (
      <Empty>
        <EmptyHeader>
          <EmptyTitle>{OPTION_GROUP_MERGE_COPY.EMPTY_SUGGESTIONS}</EmptyTitle>
          <EmptyDescription>{OPTION_GROUP_MERGE_COPY.EMPTY_SUGGESTIONS_DESCRIPTION}</EmptyDescription>
        </EmptyHeader>
      </Empty>
    );
  }

  return (
    <ul className="flex flex-col gap-4">
      {suggestions.map((suggestion) => (
        // 같은 이름의 묶음이 여러 개 있을 수 있어 이름이 아니라 서명을 key 로 쓴다.
        <li
          key={suggestion.signature}
          data-selected={suggestion.signature === selectedSignature}
          className="flex flex-col gap-3 rounded-md border p-4 data-[selected=true]:border-primary data-[selected=true]:bg-muted/40"
        >
          <div className="flex flex-wrap items-center gap-2">
            <span className="font-medium text-sm">{suggestion.name}</span>
            <Badge variant="secondary">{OPTION_GROUP_MERGE_COPY.SUGGESTION_GROUP_COUNT(suggestion.groupCount)}</Badge>
            <span className="text-muted-foreground text-xs">
              {OPTION_GROUP_MERGE_COPY.SUGGESTION_LINKED_COUNT(suggestion.linkedProductCount)}
            </span>
            <div className="ml-auto flex shrink-0 gap-2">
              <Button
                type="button"
                size="sm"
                variant={suggestion.signature === selectedSignature ? "secondary" : "default"}
                disabled={disabled}
                onClick={() => onSelect(suggestion)}
              >
                {suggestion.signature === selectedSignature
                  ? OPTION_GROUP_MERGE_COPY.BUTTON_MERGE_THIS_SELECTED
                  : OPTION_GROUP_MERGE_COPY.BUTTON_MERGE_THIS}
              </Button>
              {/* 제외는 영구적이라 여기서 바로 처리하지 않고 확인 다이얼로그를 부모가 띄운다. */}
              <Button type="button" variant="ghost" size="sm" disabled={disabled} onClick={() => onExclude(suggestion)}>
                <X />
                {OPTION_GROUP_MERGE_COPY.BUTTON_EXCLUDE}
              </Button>
            </div>
          </div>

          <div className="flex flex-col gap-1">
            <span className="text-muted-foreground text-xs">{OPTION_GROUP_MERGE_COPY.SUGGESTION_OPTIONS_LABEL}</span>
            <div className="flex flex-wrap gap-x-3 gap-y-1">
              {suggestion.options.map((option) => (
                <span key={option.id} className="text-sm">
                  {option.name}
                  <span className="text-muted-foreground"> {formatPrice(option.additionalPrice)}</span>
                </span>
              ))}
            </div>
          </div>

          <div className="flex flex-col gap-1">
            <span className="text-muted-foreground text-xs">{OPTION_GROUP_MERGE_COPY.SUGGESTION_GROUPS_LABEL}</span>
            <ul className="flex flex-col gap-1">
              {suggestion.groups.map((group) => (
                <li key={group.id} className="text-muted-foreground text-xs">
                  {/* 연결 메뉴명을 보여줘야 어떤 그룹이 어디 걸린 것인지 알고 기준을 고를 수 있다. */}
                  {group.linkedProductNames.join(", ")}
                </li>
              ))}
            </ul>
          </div>
        </li>
      ))}
    </ul>
  );
}
