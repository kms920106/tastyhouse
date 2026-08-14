"use client";

import * as React from "react";

import { Star } from "lucide-react";

import { AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import type { ReviewBlindReasonOption, ShopReviewListItem } from "@/feature/shop-review/domain";
import { formatRating } from "@/feature/shop-review/format";
import { SHOP_REVIEW_COPY } from "@/feature/shop-review/message";
import { formatDateTime } from "@/lib/date";

import { BlindRequestSheet } from "./blind-request-sheet";
import { OwnerReplyForm } from "./owner-reply-form";
import { ReviewImageDialog } from "./review-image-dialog";

interface ShopReviewItemProps {
  shopId: number;
  item: ShopReviewListItem;
  blindReasons: ReviewBlindReasonOption[];
  onOpenDetail: (reviewId: number) => void;
}

export function ShopReviewItem({ shopId, item, blindReasons, onOpenDetail }: ShopReviewItemProps) {
  const [isBlindSheetOpen, setIsBlindSheetOpen] = React.useState(false);

  const isAnswered = item.ownerReplyContent !== null;

  return (
    <AccordionItem value={String(item.id)}>
      <AccordionTrigger className="text-sm">
        <div className="flex min-w-0 flex-1 flex-wrap items-center gap-2">
          <span className="flex items-center gap-1 font-medium tabular-nums">
            <Star className="size-3.5 fill-current" aria-hidden />
            {formatRating(item.totalRating)}
          </span>
          <span className="min-w-0 truncate">{item.memberNickname}</span>
          {/* 주문과 연결되지 않은 리뷰는 주문유형이 없다 */}
          <Badge variant="outline">{item.orderMethodDescription ?? SHOP_REVIEW_COPY.BADGE_UNVERIFIED}</Badge>
          <Badge variant={isAnswered ? "default" : "secondary"}>
            {isAnswered ? SHOP_REVIEW_COPY.BADGE_ANSWERED : SHOP_REVIEW_COPY.BADGE_UNANSWERED}
          </Badge>
          {item.hidden && <Badge variant="destructive">{SHOP_REVIEW_COPY.BADGE_BLINDED}</Badge>}
          <span className="ml-auto text-muted-foreground text-xs">{formatDateTime(item.createdAt)}</span>
        </div>
      </AccordionTrigger>
      <AccordionContent>
        <div className="flex flex-col gap-4">
          {/* 고객이 자유 입력한 내용이라 줄바꿈을 살린다. */}
          <p className="whitespace-pre-line text-sm">{item.content}</p>

          <ReviewImageDialog imageUrls={item.imageUrls} />

          {item.productNames.length > 0 && (
            <div className="flex flex-col gap-1">
              <span className="text-muted-foreground text-xs">{SHOP_REVIEW_COPY.ORDER_PRODUCTS}</span>
              <div className="flex flex-wrap gap-1">
                {item.productNames.map((productName) => (
                  <Badge key={productName} variant="secondary">
                    {productName}
                  </Badge>
                ))}
              </div>
            </div>
          )}

          <OwnerReplyForm
            shopId={shopId}
            reviewId={item.id}
            replyContent={item.ownerReplyContent}
            replyCreatedAt={item.ownerReplyCreatedAt}
          />

          <div className="flex flex-wrap justify-end gap-2">
            <Button type="button" variant="outline" size="sm" onClick={() => onOpenDetail(item.id)}>
              {SHOP_REVIEW_COPY.DETAIL_ACTION}
            </Button>
            <Button type="button" variant="outline" size="sm" onClick={() => setIsBlindSheetOpen(true)}>
              {SHOP_REVIEW_COPY.BLIND_REQUEST_ACTION}
            </Button>
          </div>
        </div>

        <BlindRequestSheet
          shopId={shopId}
          reviewId={item.id}
          blindReasons={blindReasons}
          open={isBlindSheetOpen}
          onOpenChange={setIsBlindSheetOpen}
        />
      </AccordionContent>
    </AccordionItem>
  );
}
