"use client";

import * as React from "react";

import { Star } from "lucide-react";

import { StatusBadge } from "@/components/status-badge";
import { AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import type { CeoReplyPhrase } from "@/feature/ceo-reply-phrase/domain";
import {
  isBlindRequestTerminal,
  type ReviewBlindReasonOption,
  type ShopReviewListItem,
} from "@/feature/shop-review/domain";
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
  /** 자주 쓰는 문구. 0개면 답변 폼이 선택 영역을 렌더하지 않는다 */
  phrases: CeoReplyPhrase[];
  onOpenDetail: (reviewId: number) => void;
}

export function ShopReviewItem({ shopId, item, blindReasons, phrases, onOpenDetail }: ShopReviewItemProps) {
  const [isBlindSheetOpen, setIsBlindSheetOpen] = React.useState(false);

  const isAnswered = item.ownerReplyContent !== null;
  // 목록에는 최근 상태 하나만 내려오므로 그 값으로 1회 제한 소진을 판정한다.
  const isBlindRequestUsed = isBlindRequestTerminal(item.blindRequestStatus);

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
          {/* 게시중단(관리자 조치)과 사장님만보기(작성자 선택)는 독립이라 둘 다 뜰 수 있다 */}
          {item.hidden && <Badge variant="destructive">{SHOP_REVIEW_COPY.BADGE_BLINDED}</Badge>}
          {item.ownerOnly && <Badge variant="secondary">{SHOP_REVIEW_COPY.BADGE_OWNER_ONLY}</Badge>}
          {/*
            게시중단 요청 이력이 있으면 그 최신 상태를 알린다 — 버튼 disabled 판정만으로는
            "왜 못 누르는지"가 드러나지 않는다. 라벨은 서버가 준 한글명을 그대로 쓴다.
          */}
          {item.blindRequestStatus !== null && item.blindRequestStatusDescription !== null && (
            <StatusBadge status={item.blindRequestStatus} label={item.blindRequestStatusDescription} />
          )}
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
            ownerOnly={item.ownerOnly}
            replyable={item.replyable}
            replyDeadline={item.replyDeadline}
            phrases={phrases}
          />

          <div className="flex flex-wrap justify-end gap-2">
            <Button type="button" variant="outline" size="sm" onClick={() => onOpenDetail(item.id)}>
              {SHOP_REVIEW_COPY.DETAIL_ACTION}
            </Button>
            {/* 1회 제한 — 종결 상태면 서버 409 를 맞기 전에 막고 사유를 알린다 */}
            <Button
              type="button"
              variant="outline"
              size="sm"
              disabled={isBlindRequestUsed}
              title={isBlindRequestUsed ? SHOP_REVIEW_COPY.BLIND_REQUEST_ALREADY_USED_GUIDE : undefined}
              onClick={() => setIsBlindSheetOpen(true)}
            >
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
