"use client";

import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import type { MenuOptionGroup } from "@/feature/product/domain";
import { OPTION_GROUP_MERGE_COPY, OPTION_GROUP_SCREEN_COPY } from "@/feature/product/message";

interface MergeBaseSelectorProps {
  /** 선택된 옵션그룹들. 이 중 하나가 기준이 되어 살아남는다 */
  candidates: MenuOptionGroup[];
  baseOptionGroupId?: number;
  disabled?: boolean;
  onChange: (baseOptionGroupId: number) => void;
}

/**
 * 기준 옵션그룹 선택.
 *
 * 라디오로 두는 이유는 기준이 **정확히 하나**여야 하고, 고른 것만 남고 나머지가 흡수되는
 * 비대칭 관계라서다 — 체크박스로 두면 "여러 개를 기준으로" 고를 수 있다고 오해된다.
 */
export function MergeBaseSelector({ candidates, baseOptionGroupId, disabled, onChange }: MergeBaseSelectorProps) {
  return (
    <div className="flex flex-col gap-2">
      <span className="font-medium text-sm">{OPTION_GROUP_MERGE_COPY.BASE_SELECT_LABEL}</span>
      <span className="text-muted-foreground text-xs">{OPTION_GROUP_MERGE_COPY.BASE_SELECT_HELP}</span>

      <RadioGroup
        value={baseOptionGroupId === undefined ? "" : String(baseOptionGroupId)}
        onValueChange={(value) => onChange(Number(value))}
        disabled={disabled}
        className="gap-2"
      >
        {candidates.map((group) => {
          const inputId = `merge-base-${group.id}`;

          return (
            <div key={group.id} className="flex items-center gap-3">
              <RadioGroupItem id={inputId} value={String(group.id)} />
              <Label htmlFor={inputId} className="flex min-w-0 flex-1 flex-wrap items-center gap-2 font-normal">
                <span className="truncate text-sm">{group.name}</span>
                <span className="text-muted-foreground text-xs">
                  {OPTION_GROUP_SCREEN_COPY.LINKED_COUNT(group.linkedProductCount)}
                </span>
              </Label>
            </div>
          );
        })}
      </RadioGroup>
    </div>
  );
}
