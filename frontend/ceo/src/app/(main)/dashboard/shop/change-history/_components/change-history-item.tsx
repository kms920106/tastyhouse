"use client";

import { AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { Badge } from "@/components/ui/badge";
import type { ShopChangeHistoryItem as ShopChangeHistoryItemModel } from "@/feature/shop/domain";
import { SHOP_CHANGE_HISTORY_COPY } from "@/feature/shop/message";
import { formatDateTime } from "@/lib/date";

interface ChangeHistoryItemProps {
  item: ShopChangeHistoryItemModel;
}

export function ChangeHistoryItem({ item }: ChangeHistoryItemProps) {
  return (
    <AccordionItem value={String(item.id)}>
      <AccordionTrigger className="text-sm">
        <div className="flex min-w-0 flex-1 flex-wrap items-center gap-2">
          <span className="font-medium">{item.changeTypeName}</span>
          <Badge variant="secondary">{item.actionTypeName}</Badge>
          <span className="text-muted-foreground text-xs">{item.categoryName}</span>
          <span className="ml-auto text-muted-foreground text-xs">{formatDateTime(item.changedAt)}</span>
        </div>
      </AccordionTrigger>
      <AccordionContent>
        <dl className="flex flex-col gap-3">
          <div className="flex flex-col gap-1">
            <dt className="text-muted-foreground text-xs">{SHOP_CHANGE_HISTORY_COPY.BEFORE_LABEL}</dt>
            {/* 서버가 사람이 읽는 형태로 굳혀 내려주는 요약 문자열 — 파싱하지 않고 줄바꿈만 살린다. */}
            <dd className="whitespace-pre-line text-sm">
              {item.previousValue ?? SHOP_CHANGE_HISTORY_COPY.VALUE_ABSENT}
            </dd>
          </div>
          <div className="flex flex-col gap-1">
            <dt className="text-muted-foreground text-xs">{SHOP_CHANGE_HISTORY_COPY.AFTER_LABEL}</dt>
            <dd className="whitespace-pre-line text-sm">{item.newValue ?? SHOP_CHANGE_HISTORY_COPY.VALUE_ABSENT}</dd>
          </div>
        </dl>
      </AccordionContent>
    </AccordionItem>
  );
}
