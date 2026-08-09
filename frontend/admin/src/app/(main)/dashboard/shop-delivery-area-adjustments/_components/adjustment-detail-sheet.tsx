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
import { fetchDeliveryAreaAdjustmentDetailAction } from "@/feature/shop/actions";
import { DELIVERY_AREA_ADJUSTMENT_STATUS_LABEL } from "@/feature/shop/constants";
import type { DeliveryAreaAdjustmentDetail, DeliveryAreaAdjustmentStatus } from "@/feature/shop/domain";
import { DELIVERY_AREA_ADJUSTMENT_MESSAGE } from "@/feature/shop/message";
import { formatDateTime } from "@/lib/date";

interface AdjustmentDetailSheetProps {
  /** 조회할 조정 신청 ID. null 이면 닫힌 상태. */
  requestId: number | null;
  onOpenChange: (open: boolean) => void;
}

function statusBadgeVariant(status: DeliveryAreaAdjustmentStatus) {
  if (status === "COMPLETED") return "default" as const;
  if (status === "REJECTED") return "destructive" as const;
  if (status === "IN_PROGRESS") return "secondary" as const;
  return "outline" as const;
}

export function AdjustmentDetailSheet({ requestId, onOpenChange }: AdjustmentDetailSheetProps) {
  const [detail, setDetail] = React.useState<DeliveryAreaAdjustmentDetail | null>(null);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    if (requestId == null) {
      return;
    }

    let active = true;
    setIsLoading(true);
    setError(null);
    setDetail(null);

    void fetchDeliveryAreaAdjustmentDetailAction(requestId).then((result) => {
      const { success, message, data } = result;

      if (!active) return;
      if (success && data) {
        setDetail(data);
      } else {
        setError(message ?? DELIVERY_AREA_ADJUSTMENT_MESSAGE.DETAIL_LOAD_FAILED);
      }
      setIsLoading(false);
    });

    return () => {
      active = false;
    };
  }, [requestId]);

  return (
    <Sheet open={requestId != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-lg">
        <SheetHeader>
          <SheetTitle>배달지역 조정 신청 상세</SheetTitle>
          <SheetDescription>조정 신청의 상세 정보와 정보제공 동의서를 확인합니다.</SheetDescription>
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
                <div>
                  <h3 className="font-semibold text-lg leading-snug">{detail.shopName}</h3>
                  <p className="text-muted-foreground text-sm">가게 ID {detail.shopId}</p>
                </div>
                <Badge variant={statusBadgeVariant(detail.status)}>
                  {DELIVERY_AREA_ADJUSTMENT_STATUS_LABEL[detail.status]}
                </Badge>
              </div>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">상대 가맹점</dt>
                <dd>{detail.counterpartShopName}</dd>
                <dt className="text-muted-foreground">사업자등록번호</dt>
                <dd className="tabular-nums">{detail.counterpartBusinessNumber}</dd>
                <dt className="text-muted-foreground">가맹본부</dt>
                <dd>{detail.franchiseName}</dd>
              </dl>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">중첩 사유</dt>
                <dd className="whitespace-pre-wrap">{detail.reason}</dd>
                {/* 동의서는 PDF 일 수 있어 이미지로 그릴 수 없다 — 다운로드 링크로 렌더한다. */}
                <dt className="text-muted-foreground">정보제공 동의서</dt>
                <dd>
                  {detail.consentFileUrl ? (
                    <a href={detail.consentFileUrl} target="_blank" rel="noreferrer" className="underline">
                      동의서 열기
                    </a>
                  ) : (
                    "-"
                  )}
                </dd>
                <dt className="text-muted-foreground">반려 사유</dt>
                <dd className="whitespace-pre-wrap">{detail.rejectReason ?? "-"}</dd>
              </dl>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">접수 일시</dt>
                <dd className="tabular-nums">{formatDateTime(detail.createdAt)}</dd>
                <dt className="text-muted-foreground">수정 일시</dt>
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
