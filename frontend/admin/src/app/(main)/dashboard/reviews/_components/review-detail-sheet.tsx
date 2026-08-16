"use client";

import * as React from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { Skeleton } from "@/components/ui/skeleton";
import { fetchReviewAction } from "@/feature/review/actions";
import type { ReviewDetail } from "@/feature/review/domain";
import { formatRating, formatWillRevisit } from "@/feature/review/format";
import { REVIEW_MESSAGE, REVIEW_VISIBILITY_COPY } from "@/feature/review/message";
import { formatDateTime } from "@/lib/date";

interface ReviewDetailSheetProps {
  /** 조회할 리뷰 ID. null 이면 닫힌 상태. */
  reviewId: number | null;
  onOpenChange: (open: boolean) => void;
}

export function ReviewDetailSheet({ reviewId, onOpenChange }: ReviewDetailSheetProps) {
  const [detail, setDetail] = React.useState<ReviewDetail | null>(null);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    if (reviewId == null) {
      return;
    }

    let active = true;
    setIsLoading(true);
    setError(null);
    setDetail(null);

    void fetchReviewAction(reviewId).then((result) => {
      const { success, message, data } = result;

      if (!active) return;
      if (success && data) {
        setDetail(data);
      } else {
        setError(message ?? REVIEW_MESSAGE.DETAIL_LOAD_FAILED);
      }
      setIsLoading(false);
    });

    return () => {
      active = false;
    };
  }, [reviewId]);

  return (
    <Sheet open={reviewId != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>리뷰 상세</SheetTitle>
          <SheetDescription>리뷰의 상세 정보를 확인합니다.</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-5 overflow-y-auto px-4">
          {isLoading ? (
            <div className="space-y-3">
              <Skeleton className="h-6 w-3/4" />
              <Skeleton className="h-4 w-1/3" />
              <Skeleton className="h-32 w-full" />
            </div>
          ) : error ? (
            <p className="text-destructive text-sm">{error}</p>
          ) : detail ? (
            <>
              <div className="flex items-start justify-between gap-3">
                <div className="flex items-center gap-3">
                  {detail.memberProfileImageUrl ? (
                    // biome-ignore lint/performance/noImgElement: CDN URL 프로필 이미지
                    <img
                      src={detail.memberProfileImageUrl}
                      alt={detail.memberNickname}
                      className="size-10 shrink-0 rounded-full border object-cover"
                    />
                  ) : (
                    <div className="flex size-10 shrink-0 items-center justify-center rounded-full border bg-muted text-muted-foreground text-xs">
                      {detail.memberNickname.slice(0, 1)}
                    </div>
                  )}
                  <div>
                    <p className="font-semibold text-sm leading-snug">{detail.memberNickname}</p>
                    <p className="text-muted-foreground text-xs">{detail.shopName}</p>
                  </div>
                </div>
                <div className="flex shrink-0 flex-wrap justify-end gap-1">
                  <Badge variant={detail.hidden ? "destructive" : "default"}>{detail.hidden ? "숨김" : "노출"}</Badge>
                  <Badge variant={detail.ownerOnly ? "secondary" : "outline"}>
                    {detail.ownerOnly ? REVIEW_VISIBILITY_COPY.OWNER_ONLY : REVIEW_VISIBILITY_COPY.PUBLIC}
                  </Badge>
                </div>
              </div>

              <p className="whitespace-pre-wrap break-words text-sm leading-relaxed">{detail.content}</p>

              {detail.imageUrls.length ? (
                <div className="grid grid-cols-3 gap-2">
                  {detail.imageUrls.map((url, index) => (
                    // biome-ignore lint/performance/noImgElement: CDN URL 미리보기
                    <img
                      key={url}
                      src={url}
                      alt={`리뷰 이미지 ${index + 1}`}
                      className="aspect-square w-full rounded-md border object-cover"
                    />
                  ))}
                </div>
              ) : null}

              {detail.tagNames.length ? (
                <div className="flex flex-wrap gap-1">
                  {detail.tagNames.map((tag) => (
                    <Badge key={tag} variant="outline">
                      #{tag}
                    </Badge>
                  ))}
                </div>
              ) : null}

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">상점</dt>
                <dd>{detail.shopName}</dd>
                <dt className="text-muted-foreground">인근 역</dt>
                <dd>{detail.stationName}</dd>
                <dt className="text-muted-foreground">재방문 의사</dt>
                <dd>{formatWillRevisit(detail.willRevisit)}</dd>
                <dt className="text-muted-foreground">{REVIEW_VISIBILITY_COPY.LABEL}</dt>
                <dd>{detail.ownerOnly ? REVIEW_VISIBILITY_COPY.OWNER_ONLY : REVIEW_VISIBILITY_COPY.PUBLIC}</dd>
              </dl>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">총점</dt>
                <dd className="tabular-nums">{formatRating(detail.totalRating)}</dd>
                <dt className="text-muted-foreground">맛</dt>
                <dd className="tabular-nums">{formatRating(detail.tasteRating)}</dd>
                <dt className="text-muted-foreground">양</dt>
                <dd className="tabular-nums">{formatRating(detail.amountRating)}</dd>
                <dt className="text-muted-foreground">가격</dt>
                <dd className="tabular-nums">{formatRating(detail.priceRating)}</dd>
                <dt className="text-muted-foreground">분위기</dt>
                <dd className="tabular-nums">{formatRating(detail.atmosphereRating)}</dd>
                <dt className="text-muted-foreground">친절도</dt>
                <dd className="tabular-nums">{formatRating(detail.kindnessRating)}</dd>
                <dt className="text-muted-foreground">위생</dt>
                <dd className="tabular-nums">{formatRating(detail.hygieneRating)}</dd>
              </dl>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">작성일시</dt>
                <dd className="tabular-nums">{formatDateTime(detail.createdAt)}</dd>
              </dl>
            </>
          ) : null}
        </div>

        <SheetFooter>
          <SheetClose asChild>
            <Button variant="outline">닫기</Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
