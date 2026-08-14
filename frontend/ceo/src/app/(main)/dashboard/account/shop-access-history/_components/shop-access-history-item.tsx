"use client";

import { StatusBadge } from "@/components/status-badge";
import { AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import type { CeoShopAccessHistoryItem as CeoShopAccessHistoryItemModel } from "@/feature/ceo/domain";
import { CEO_SHOP_ACCESS_HISTORY_COPY } from "@/feature/ceo/message";
import { formatDateTime } from "@/lib/date";

interface ShopAccessHistoryItemProps {
  item: CeoShopAccessHistoryItemModel;
}

export function ShopAccessHistoryItem({ item }: ShopAccessHistoryItemProps) {
  return (
    <AccordionItem value={String(item.id)}>
      <AccordionTrigger className="text-sm">
        <div className="flex min-w-0 flex-1 flex-wrap items-center gap-2">
          {/* 라벨은 서버가 준 한글(`actionTypeName`)을 그대로 넘긴다 — 프론트에서 매핑하지 않는다. */}
          <StatusBadge status={item.actionType} label={item.actionTypeName} />
          <span className="font-medium">{item.shopName}</span>
          <span className="ml-auto text-muted-foreground text-xs">{formatDateTime(item.occurredAt)}</span>
        </div>
      </AccordionTrigger>
      <AccordionContent>
        <dl className="flex flex-col gap-3">
          <div className="flex flex-col gap-1">
            <dt className="text-muted-foreground text-xs">{CEO_SHOP_ACCESS_HISTORY_COPY.SHOP_NAME}</dt>
            <dd className="text-sm">{item.shopName}</dd>
          </div>
          <div className="flex flex-col gap-1">
            <dt className="text-muted-foreground text-xs">{CEO_SHOP_ACCESS_HISTORY_COPY.FILTER_ACTION_TYPE_LABEL}</dt>
            <dd className="text-sm">{item.actionTypeName}</dd>
          </div>
          <div className="flex flex-col gap-1">
            <dt className="text-muted-foreground text-xs">{CEO_SHOP_ACCESS_HISTORY_COPY.OCCURRED_AT}</dt>
            <dd className="text-sm">{formatDateTime(item.occurredAt)}</dd>
          </div>
        </dl>
      </AccordionContent>
    </AccordionItem>
  );
}
