"use client";

import { AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import type { MenuOption, MenuOptionGroup } from "@/feature/product/domain";
import { OPTION_GROUP_SCREEN_COPY, PRODUCT_OPTION_GROUP_COPY } from "@/feature/product/message";

import { OptionList } from "./option-list";

interface OptionGroupAccordionProps {
  group: MenuOptionGroup;
  disabled?: boolean;
  onEditGroup: () => void;
  onDeleteGroup: () => void;
  onAddOption: () => void;
  onEditOption: (option: MenuOption) => void;
  onDeleteOption: (option: MenuOption) => void;
  onReorderOptions: (optionIds: number[]) => void;
}

export function OptionGroupAccordion({
  group,
  disabled,
  onEditGroup,
  onDeleteGroup,
  onAddOption,
  onEditOption,
  onDeleteOption,
  onReorderOptions,
}: OptionGroupAccordionProps) {
  return (
    <AccordionItem value={String(group.id)}>
      <AccordionTrigger className="text-sm">
        <div className="flex min-w-0 flex-1 flex-wrap items-center gap-2">
          <span className="font-medium">{group.name}</span>
          {group.required && <Badge>{OPTION_GROUP_SCREEN_COPY.BADGE_REQUIRED}</Badge>}
          {group.multipleSelect && <Badge variant="secondary">{OPTION_GROUP_SCREEN_COPY.BADGE_MULTIPLE_SELECT}</Badge>}
          <span className="text-muted-foreground text-xs">
            {OPTION_GROUP_SCREEN_COPY.SELECT_RANGE(group.minSelect, group.maxSelect)}
          </span>
          <span className="text-muted-foreground text-xs">
            {OPTION_GROUP_SCREEN_COPY.OPTION_COUNT(group.options.length)}
          </span>
          {/* 연결 메뉴 수는 그룹 삭제의 파급 범위라 접힌 상태에서도 보이게 헤더 오른쪽에 둔다. */}
          <span className="ml-auto text-muted-foreground text-xs">
            {OPTION_GROUP_SCREEN_COPY.LINKED_COUNT(group.linkedProductCount)}
          </span>
        </div>
      </AccordionTrigger>

      <AccordionContent>
        <div className="flex flex-col gap-3">
          {group.description && <p className="text-muted-foreground text-sm">{group.description}</p>}

          <OptionList
            options={group.options}
            disabled={disabled}
            onEdit={onEditOption}
            onDelete={onDeleteOption}
            onReorder={onReorderOptions}
          />

          <div className="flex flex-wrap justify-end gap-2">
            <Button type="button" variant="outline" size="sm" disabled={disabled} onClick={onAddOption}>
              {PRODUCT_OPTION_GROUP_COPY.BUTTON_ADD_OPTION}
            </Button>
            <Button type="button" variant="outline" size="sm" disabled={disabled} onClick={onEditGroup}>
              {PRODUCT_OPTION_GROUP_COPY.BUTTON_EDIT_GROUP}
            </Button>
            <Button type="button" variant="destructive" size="sm" disabled={disabled} onClick={onDeleteGroup}>
              {PRODUCT_OPTION_GROUP_COPY.BUTTON_DELETE_GROUP}
            </Button>
          </div>
        </div>
      </AccordionContent>
    </AccordionItem>
  );
}
