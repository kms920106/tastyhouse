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
import { fetchBannerAction } from "@/feature/banner/actions";
import type { BannerDetail } from "@/feature/banner/domain";
import { BANNER_MESSAGE, BANNER_TYPE_LABEL } from "@/feature/banner/message";
import { formatDateTime } from "@/lib/date";

interface BannerDetailSheetProps {
  /** 조회할 배너 ID. null 이면 닫힌 상태. */
  bannerId: number | null;
  onOpenChange: (open: boolean) => void;
}

export function BannerDetailSheet({ bannerId, onOpenChange }: BannerDetailSheetProps) {
  const [detail, setDetail] = React.useState<BannerDetail | null>(null);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    if (bannerId == null) {
      return;
    }

    let active = true;
    setIsLoading(true);
    setError(null);
    setDetail(null);

    void fetchBannerAction(bannerId).then((result) => {
      const { success, message, data } = result;

      if (!active) return;
      if (success && data) {
        setDetail(data);
      } else {
        setError(message ?? BANNER_MESSAGE.DETAIL_LOAD_FAILED);
      }
      setIsLoading(false);
    });

    return () => {
      active = false;
    };
  }, [bannerId]);

  return (
    <Sheet open={bannerId != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>배너 상세</SheetTitle>
          <SheetDescription>배너의 상세 정보를 확인합니다.</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-5 overflow-y-auto px-4">
          {isLoading ? (
            <div className="space-y-3">
              <Skeleton className="h-40 w-full" />
              <Skeleton className="h-6 w-3/4" />
              <Skeleton className="h-4 w-1/3" />
            </div>
          ) : error ? (
            <p className="text-destructive text-sm">{error}</p>
          ) : detail ? (
            <>
              {/* biome-ignore lint/performance/noImgElement: CDN 원본 URL, next/image remotePatterns 미설정 */}
              <img
                src={detail.image.url}
                alt={detail.title ?? "배너 이미지"}
                className="w-full rounded-md border object-cover"
              />

              <div className="flex items-start justify-between gap-3">
                <h3 className="font-semibold text-lg leading-snug">{detail.title ?? "-"}</h3>
                <div className="flex gap-1.5">
                  <Badge variant="outline">{BANNER_TYPE_LABEL[detail.type]}</Badge>
                  <Badge variant={detail.visible ? "default" : "secondary"}>{detail.visible ? "노출" : "미노출"}</Badge>
                </div>
              </div>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">ID</dt>
                <dd className="tabular-nums">{detail.id}</dd>
                <dt className="text-muted-foreground">링크 URL</dt>
                <dd className="break-all">{detail.linkUrl ?? "-"}</dd>
                <dt className="text-muted-foreground">노출 시작일시</dt>
                <dd className="tabular-nums">{formatDateTime(detail.startDate)}</dd>
                <dt className="text-muted-foreground">노출 종료일시</dt>
                <dd className="tabular-nums">{formatDateTime(detail.endDate)}</dd>
                <dt className="text-muted-foreground">정렬 순서</dt>
                <dd className="tabular-nums">{detail.sort}</dd>
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
