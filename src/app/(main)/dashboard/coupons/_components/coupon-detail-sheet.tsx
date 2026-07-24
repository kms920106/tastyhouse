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
import { fetchCouponAction } from "@/feature/coupon/actions";
import type { CouponDetail } from "@/feature/coupon/domain";
import {
  discountTypeLabel,
  formatDiscountValue,
  formatMaxDiscountAmount,
  formatMaxDiscountCount,
} from "@/feature/coupon/format";
import { COUPON_MESSAGE } from "@/feature/coupon/message";
import { formatDateTime } from "@/lib/date";

interface CouponDetailSheetProps {
  /** 조회할 쿠폰 ID. null 이면 닫힌 상태. */
  couponId: number | null;
  onOpenChange: (open: boolean) => void;
}

export function CouponDetailSheet({ couponId, onOpenChange }: CouponDetailSheetProps) {
  const [detail, setDetail] = React.useState<CouponDetail | null>(null);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    if (couponId == null) {
      return;
    }

    let active = true;
    setIsLoading(true);
    setError(null);
    setDetail(null);

    void fetchCouponAction(couponId).then((result) => {
      const { success, message, data } = result;

      if (!active) return;
      if (success && data) {
        setDetail(data);
      } else {
        setError(message ?? COUPON_MESSAGE.DETAIL_LOAD_FAILED);
      }
      setIsLoading(false);
    });

    return () => {
      active = false;
    };
  }, [couponId]);

  return (
    <Sheet open={couponId != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>쿠폰 상세</SheetTitle>
          <SheetDescription>쿠폰의 상세 정보를 확인합니다.</SheetDescription>
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
                <h3 className="font-semibold text-lg leading-snug">{detail.name}</h3>
                <Badge variant={detail.visible ? "default" : "secondary"}>{detail.visible ? "노출" : "미노출"}</Badge>
              </div>

              {detail.description ? (
                <p className="whitespace-pre-wrap break-words text-muted-foreground text-sm leading-relaxed">
                  {detail.description}
                </p>
              ) : null}

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">ID</dt>
                <dd className="tabular-nums">{detail.id}</dd>
                <dt className="text-muted-foreground">할인 유형</dt>
                <dd>{discountTypeLabel(detail.discountType)}</dd>
                <dt className="text-muted-foreground">할인 값</dt>
                <dd className="tabular-nums">{formatDiscountValue(detail.discountType, detail.discountAmount)}</dd>
                <dt className="text-muted-foreground">최대 할인 금액</dt>
                <dd className="tabular-nums">{formatMaxDiscountAmount(detail.maxDiscountAmount)}</dd>
                <dt className="text-muted-foreground">최소 주문 금액</dt>
                <dd className="tabular-nums">{detail.minOrderAmount.toLocaleString("ko-KR")}원</dd>
                <dt className="text-muted-foreground">최대 발급 수량</dt>
                <dd className="tabular-nums">{formatMaxDiscountCount(detail.maxDiscountCount)}</dd>
              </dl>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">발급 기간</dt>
                <dd className="tabular-nums">
                  {formatDateTime(detail.issueStartAt)} ~ {formatDateTime(detail.issueEndAt)}
                </dd>
                <dt className="text-muted-foreground">사용 기간</dt>
                <dd className="tabular-nums">
                  {formatDateTime(detail.useStartAt)} ~ {formatDateTime(detail.useEndAt)}
                </dd>
              </dl>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">생성일시</dt>
                <dd className="tabular-nums">{formatDateTime(detail.createdAt)}</dd>
                <dt className="text-muted-foreground">수정일시</dt>
                <dd className="tabular-nums">{formatDateTime(detail.updatedAt)}</dd>
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
