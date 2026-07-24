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
import { fetchPartnershipRequestAction } from "@/feature/partnership-request/actions";
import type { PartnershipRequestDetail } from "@/feature/partnership-request/domain";
import { partnershipStatusBadgeVariant, partnershipStatusLabel } from "@/feature/partnership-request/format";
import { PARTNERSHIP_MESSAGE } from "@/feature/partnership-request/message";
import { formatDateTime } from "@/lib/date";

interface PartnershipRequestDetailSheetProps {
  /** 조회할 제휴 신청 ID. null 이면 닫힌 상태. */
  partnershipRequestId: number | null;
  onOpenChange: (open: boolean) => void;
}

export function PartnershipRequestDetailSheet({
  partnershipRequestId,
  onOpenChange,
}: PartnershipRequestDetailSheetProps) {
  const [detail, setDetail] = React.useState<PartnershipRequestDetail | null>(null);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    if (partnershipRequestId == null) {
      return;
    }

    let active = true;
    setIsLoading(true);
    setError(null);
    setDetail(null);

    void fetchPartnershipRequestAction(partnershipRequestId).then((result) => {
      const { success, message, data } = result;

      if (!active) return;
      if (success && data) {
        setDetail(data);
      } else {
        setError(message ?? PARTNERSHIP_MESSAGE.DETAIL_LOAD_FAILED);
      }
      setIsLoading(false);
    });

    return () => {
      active = false;
    };
  }, [partnershipRequestId]);

  return (
    <Sheet open={partnershipRequestId != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-lg">
        <SheetHeader>
          <SheetTitle>제휴 신청 상세</SheetTitle>
          <SheetDescription>제휴 신청의 상세 정보를 확인합니다.</SheetDescription>
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
                  <h3 className="font-semibold text-lg leading-snug">{detail.businessName}</h3>
                  <p className="text-muted-foreground text-sm">{detail.contactName}</p>
                </div>
                <Badge variant={partnershipStatusBadgeVariant(detail.status)}>
                  {partnershipStatusLabel(detail.status)}
                </Badge>
              </div>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">주소</dt>
                <dd>{detail.address}</dd>
                <dt className="text-muted-foreground">상세주소</dt>
                <dd>{detail.addressDetail}</dd>
                <dt className="text-muted-foreground">담당자명</dt>
                <dd>{detail.contactName}</dd>
                <dt className="text-muted-foreground">연락처</dt>
                <dd className="tabular-nums">{detail.contactPhone}</dd>
              </dl>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">상담 요청 일시</dt>
                <dd className="tabular-nums">{formatDateTime(detail.consultationRequestedAt)}</dd>
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
