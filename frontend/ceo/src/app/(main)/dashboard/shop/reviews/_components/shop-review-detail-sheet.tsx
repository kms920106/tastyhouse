"use client";

import * as React from "react";

import { toast } from "sonner";

import { StatusBadge } from "@/components/status-badge";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { cancelBlindRequestAction } from "@/feature/shop-review/actions";
import { BLIND_REQUEST_PENDING_STATUS, REVIEW_RATING_ASPECTS } from "@/feature/shop-review/constants";
import type { ReviewBlindReasonOption, ShopReviewDetail } from "@/feature/shop-review/domain";
import { formatRating, formatReviewNumber } from "@/feature/shop-review/format";
import { SHOP_REVIEW_COPY } from "@/feature/shop-review/message";
import { formatDateTime } from "@/lib/date";

import { BlindRequestSheet } from "./blind-request-sheet";
import { OwnerReplyForm } from "./owner-reply-form";
import { ReviewImageDialog } from "./review-image-dialog";

interface ShopReviewDetailSheetProps {
  shopId: number;
  detail: ShopReviewDetail;
  blindReasons: ReviewBlindReasonOption[];
  onClose: () => void;
}

/** 라벨-값 한 쌍. 호출부가 값 없음을 null 로 넘기면 `VALUE_ABSENT` 로 채운다 */
function DetailRow({ label, value }: { label: string; value: React.ReactNode | null }) {
  return (
    <div className="flex flex-col gap-1">
      <dt className="text-muted-foreground text-xs">{label}</dt>
      <dd className="text-sm">{value ?? SHOP_REVIEW_COPY.VALUE_ABSENT}</dd>
    </div>
  );
}

/**
 * 리뷰 상세 — 원문 「리뷰 정보」 ①~⑧.
 *
 * ⑥ "배달리뷰"는 이 저장소에 배달 전용 리뷰 개념이 없어 만들지 않고, 대신 항목별 평점 ·
 * 재방문 의사 · 태그를 그 자리에 둔다(`docs/tasks/frontend.md` A-3 ⑤ 표).
 */
export function ShopReviewDetailSheet({ shopId, detail, blindReasons, onClose }: ShopReviewDetailSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const [isBlindSheetOpen, setIsBlindSheetOpen] = React.useState(false);

  const aspectRatings: Record<(typeof REVIEW_RATING_ASPECTS)[number]["key"], number | null> = {
    taste: detail.tasteRating,
    amount: detail.amountRating,
    price: detail.priceRating,
    atmosphere: detail.atmosphereRating,
    kindness: detail.kindnessRating,
    hygiene: detail.hygieneRating,
  };

  function handleCancelBlindRequest(requestId: number) {
    startTransition(async () => {
      const { success, message } = await cancelBlindRequestAction(shopId, requestId);
      if (success) {
        toast.success(SHOP_REVIEW_COPY.BLIND_CANCEL_SUCCESS);
      } else {
        toast.error(message ?? SHOP_REVIEW_COPY.BLIND_CANCEL_FAILED);
      }
    });
  }

  return (
    <Sheet open onOpenChange={(open) => !open && onClose()}>
      <SheetContent className="flex flex-col gap-0 overflow-y-auto">
        <SheetHeader>
          <SheetTitle className="flex flex-wrap items-center gap-2">
            <span className="min-w-0 flex-1">{SHOP_REVIEW_COPY.DETAIL_TITLE}</span>
            {/* 게시중단(관리자 조치)과 사장님만보기(작성자 선택)는 독립이라 둘 다 뜰 수 있다 */}
            {detail.hidden && <Badge variant="destructive">{SHOP_REVIEW_COPY.BADGE_BLINDED}</Badge>}
            {detail.ownerOnly && <Badge variant="secondary">{SHOP_REVIEW_COPY.BADGE_OWNER_ONLY}</Badge>}
          </SheetTitle>
          <SheetDescription>{detail.memberNickname}</SheetDescription>
        </SheetHeader>

        <div className="flex flex-col gap-6 px-4 pb-6">
          {/* ===== ①② 주문유형 · 리뷰번호 ===== */}
          <dl className="flex flex-col gap-3">
            <DetailRow
              label={SHOP_REVIEW_COPY.ORDER_METHOD}
              value={
                <Badge variant="outline">{detail.orderMethodDescription ?? SHOP_REVIEW_COPY.BADGE_UNVERIFIED}</Badge>
              }
            />
            <DetailRow
              label={SHOP_REVIEW_COPY.REVIEW_NUMBER}
              value={<span className="font-mono text-xs">{formatReviewNumber(detail.reviewNumber)}</span>}
            />
            <DetailRow label={SHOP_REVIEW_COPY.WRITTEN_AT} value={formatDateTime(detail.createdAt)} />
            <DetailRow
              label={SHOP_REVIEW_COPY.STAT_AVERAGE_RATING}
              value={<span className="tabular-nums">{formatRating(detail.totalRating)}</span>}
            />
          </dl>

          {/* ===== ③ 리뷰 내용 ===== */}
          <Separator />
          <section className="flex flex-col gap-2">
            <span className="font-medium text-sm">{SHOP_REVIEW_COPY.REVIEW_CONTENT}</span>
            {/* 고객이 자유 입력한 내용이라 줄바꿈을 살린다. */}
            <p className="whitespace-pre-line text-sm">{detail.content}</p>
          </section>

          {/* ===== ④ 리뷰 사진 ===== */}
          {detail.imageUrls.length > 0 && (
            <>
              <Separator />
              <section className="flex flex-col gap-2">
                <span className="font-medium text-sm">{SHOP_REVIEW_COPY.REVIEW_IMAGES}</span>
                <ReviewImageDialog imageUrls={detail.imageUrls} />
              </section>
            </>
          )}

          {/* ===== ⑤ 주문 메뉴 ===== */}
          {detail.productNames.length > 0 && (
            <>
              <Separator />
              <section className="flex flex-col gap-2">
                <span className="font-medium text-sm">{SHOP_REVIEW_COPY.ORDER_PRODUCTS}</span>
                <div className="flex flex-wrap gap-1">
                  {detail.productNames.map((productName) => (
                    <Badge key={productName} variant="secondary">
                      {productName}
                    </Badge>
                  ))}
                </div>
              </section>
            </>
          )}

          {/* ===== ⑥ 항목별 평점 · 재방문 의사 · 태그 (원문의 "배달리뷰" 자리) ===== */}
          <Separator />
          <section className="flex flex-col gap-3">
            <span className="font-medium text-sm">{SHOP_REVIEW_COPY.ASPECT_RATING_TITLE}</span>
            <dl className="grid grid-cols-2 gap-x-6 gap-y-2 sm:grid-cols-3">
              {REVIEW_RATING_ASPECTS.map((aspect) => (
                <div key={aspect.key} className="flex items-baseline justify-between gap-2">
                  <dt className="text-muted-foreground text-xs">{aspect.label}</dt>
                  <dd className="font-medium text-sm tabular-nums">{formatRating(aspectRatings[aspect.key])}</dd>
                </div>
              ))}
            </dl>

            <dl className="flex flex-col gap-3">
              <DetailRow
                label={SHOP_REVIEW_COPY.WILL_REVISIT}
                value={
                  detail.willRevisit === null
                    ? null
                    : detail.willRevisit
                      ? SHOP_REVIEW_COPY.WILL_REVISIT_YES
                      : SHOP_REVIEW_COPY.WILL_REVISIT_NO
                }
              />
            </dl>

            {detail.tagNames.length > 0 && (
              <div className="flex flex-col gap-1">
                <span className="text-muted-foreground text-xs">{SHOP_REVIEW_COPY.REVIEW_TAGS}</span>
                <div className="flex flex-wrap gap-1">
                  {detail.tagNames.map((tagName) => (
                    <Badge key={tagName} variant="outline">
                      {tagName}
                    </Badge>
                  ))}
                </div>
              </div>
            )}
          </section>

          {/* ===== ⑧ 사장님 댓글 ===== */}
          <Separator />
          <OwnerReplyForm
            shopId={shopId}
            reviewId={detail.id}
            replyContent={detail.ownerReplyContent}
            replyCreatedAt={detail.ownerReplyCreatedAt}
            replyUpdatedAt={detail.ownerReplyUpdatedAt}
            ownerOnly={detail.ownerOnly}
            disabled={isPending}
          />

          {/* ===== ⑦ 게시중단 요청 이력 ===== */}
          <Separator />
          <section className="flex flex-col gap-3">
            <div className="flex items-center justify-between gap-2">
              <span className="font-medium text-sm">{SHOP_REVIEW_COPY.BLIND_HISTORY_SECTION_TITLE}</span>
              <Button type="button" variant="outline" size="sm" onClick={() => setIsBlindSheetOpen(true)}>
                {SHOP_REVIEW_COPY.BLIND_REQUEST_ACTION}
              </Button>
            </div>

            {detail.blindRequests.length === 0 ? (
              <p className="rounded-md border border-dashed p-4 text-center text-muted-foreground text-sm">
                {SHOP_REVIEW_COPY.BLIND_HISTORY_EMPTY}
              </p>
            ) : (
              <ul className="flex flex-col gap-2">
                {detail.blindRequests.map((request) => (
                  <li key={request.id} className="flex flex-col gap-2 rounded-md border p-3">
                    <div className="flex flex-wrap items-center gap-2">
                      <Badge variant="outline">{request.reasonDescription}</Badge>
                      <StatusBadge status={request.status} label={request.statusDescription} />
                      <span className="ml-auto text-muted-foreground text-xs">{formatDateTime(request.createdAt)}</span>
                    </div>

                    {request.detailReason && (
                      <div className="flex flex-col gap-1">
                        <span className="text-muted-foreground text-xs">
                          {SHOP_REVIEW_COPY.BLIND_HISTORY_DETAIL_REASON}
                        </span>
                        {/* 점주가 자유 입력한 사유라 줄바꿈을 살린다. */}
                        <p className="whitespace-pre-line text-sm">{request.detailReason}</p>
                      </div>
                    )}

                    {request.rejectReason && (
                      <div className="flex flex-col gap-1">
                        <span className="text-muted-foreground text-xs">
                          {SHOP_REVIEW_COPY.BLIND_HISTORY_REJECT_REASON}
                        </span>
                        {/* 관리자가 자유 입력한 사유라 줄바꿈을 살린다. */}
                        <p className="whitespace-pre-line text-destructive text-sm">{request.rejectReason}</p>
                      </div>
                    )}

                    {/* 취소는 대기중일 때만 가능하다(`docs/tasks/backend.md` 1-10) */}
                    {request.status === BLIND_REQUEST_PENDING_STATUS && (
                      <AlertDialog>
                        <AlertDialogTrigger asChild>
                          <Button
                            type="button"
                            variant="destructive"
                            size="sm"
                            className="self-end"
                            disabled={isPending}
                          >
                            {SHOP_REVIEW_COPY.BLIND_CANCEL_ACTION}
                          </Button>
                        </AlertDialogTrigger>
                        <AlertDialogContent>
                          <AlertDialogHeader>
                            <AlertDialogTitle>{SHOP_REVIEW_COPY.BLIND_CANCEL_CONFIRM_TITLE}</AlertDialogTitle>
                            <AlertDialogDescription>
                              {SHOP_REVIEW_COPY.BLIND_CANCEL_CONFIRM_DESCRIPTION}
                            </AlertDialogDescription>
                          </AlertDialogHeader>
                          <AlertDialogFooter>
                            <AlertDialogCancel>{SHOP_REVIEW_COPY.BLIND_CANCEL_CONFIRM_DISMISS}</AlertDialogCancel>
                            <AlertDialogAction onClick={() => handleCancelBlindRequest(request.id)}>
                              {SHOP_REVIEW_COPY.BLIND_CANCEL_CONFIRM_ACTION}
                            </AlertDialogAction>
                          </AlertDialogFooter>
                        </AlertDialogContent>
                      </AlertDialog>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </section>
        </div>

        <BlindRequestSheet
          shopId={shopId}
          reviewId={detail.id}
          blindReasons={blindReasons}
          open={isBlindSheetOpen}
          onOpenChange={setIsBlindSheetOpen}
        />
      </SheetContent>
    </Sheet>
  );
}
