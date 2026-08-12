"use client";

import { StatusBadge } from "@/components/status-badge";
import { AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { Button } from "@/components/ui/button";
import type { ShopRequestListItem } from "@/feature/shop/domain";
import { SHOP_REQUEST_COPY } from "@/feature/shop/message";
import { formatDateTime } from "@/lib/date";

interface ShopRequestItemProps {
  item: ShopRequestListItem;
  onOpenDetail: (requestId: number) => void;
}

export function ShopRequestItem({ item, onOpenDetail }: ShopRequestItemProps) {
  return (
    <AccordionItem value={String(item.requestId)}>
      <AccordionTrigger className="text-sm">
        <div className="flex min-w-0 flex-1 flex-wrap items-center gap-2">
          <span className="font-medium">{item.summary}</span>
          <StatusBadge status={item.status} label={item.statusDescription} />
          <span className="text-muted-foreground text-xs">{item.requestTypeDescription}</span>
          <span className="ml-auto text-muted-foreground text-xs">{formatDateTime(item.requestedAt)}</span>
        </div>
      </AccordionTrigger>
      <AccordionContent>
        <dl className="flex flex-col gap-3">
          <div className="flex flex-col gap-1">
            <dt className="text-muted-foreground text-xs">{SHOP_REQUEST_COPY.REQUESTED_AT}</dt>
            <dd className="text-sm">{formatDateTime(item.requestedAt)}</dd>
          </div>
          <div className="flex flex-col gap-1">
            <dt className="text-muted-foreground text-xs">{SHOP_REQUEST_COPY.PROCESSED_AT}</dt>
            <dd className="text-sm">
              {item.processedAt ? formatDateTime(item.processedAt) : SHOP_REQUEST_COPY.VALUE_ABSENT}
            </dd>
          </div>
          {item.rejectReason && (
            <div className="flex flex-col gap-1">
              <dt className="text-muted-foreground text-xs">{SHOP_REQUEST_COPY.REJECT_REASON}</dt>
              {/* 관리자가 자유 입력한 사유라 줄바꿈을 살린다. */}
              <dd className="whitespace-pre-line text-destructive text-sm">{item.rejectReason}</dd>
            </div>
          )}
          <div className="flex flex-col gap-1">
            <dt className="text-muted-foreground text-xs">{SHOP_REQUEST_COPY.CONTRACT_AMENDING}</dt>
            <dd className="text-sm">
              {item.contractAmending ? SHOP_REQUEST_COPY.CONTRACT_AMENDING_YES : SHOP_REQUEST_COPY.CONTRACT_AMENDING_NO}
            </dd>
          </div>
          <div className="flex flex-col gap-1">
            <dt className="text-muted-foreground text-xs">{SHOP_REQUEST_COPY.COMMENT_COUNT}</dt>
            <dd className="text-sm">{item.commentCount}</dd>
          </div>
        </dl>

        <div className="mt-4 flex justify-end">
          <Button type="button" variant="outline" size="sm" onClick={() => onOpenDetail(item.requestId)}>
            {SHOP_REQUEST_COPY.DETAIL_ACTION}
          </Button>
        </div>
      </AccordionContent>
    </AccordionItem>
  );
}
