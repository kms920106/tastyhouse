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
import { fetchEventAction } from "@/feature/event/actions";
import type { EventDetail } from "@/feature/event/domain";
import { eventStatusBadgeVariant, eventStatusLabel } from "@/feature/event/format";
import { EVENT_MESSAGE } from "@/feature/event/message";
import { formatDateTime } from "@/lib/date";

interface EventDetailSheetProps {
  /** 조회할 이벤트 ID. null 이면 닫힌 상태. */
  eventId: number | null;
  onOpenChange: (open: boolean) => void;
}

export function EventDetailSheet({ eventId, onOpenChange }: EventDetailSheetProps) {
  const [detail, setDetail] = React.useState<EventDetail | null>(null);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    if (eventId == null) {
      return;
    }

    let active = true;
    setIsLoading(true);
    setError(null);
    setDetail(null);

    void fetchEventAction(eventId).then((result) => {
      const { success, message, data } = result;

      if (!active) return;
      if (success && data) {
        setDetail(data);
      } else {
        setError(message ?? EVENT_MESSAGE.DETAIL_LOAD_FAILED);
      }
      setIsLoading(false);
    });

    return () => {
      active = false;
    };
  }, [eventId]);

  return (
    <Sheet open={eventId != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>이벤트 상세</SheetTitle>
          <SheetDescription>이벤트의 상세 정보를 확인합니다.</SheetDescription>
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
                  <h3 className="font-semibold text-lg leading-snug">{detail.name}</h3>
                  {detail.subtitle ? <p className="text-muted-foreground text-sm">{detail.subtitle}</p> : null}
                </div>
                <Badge variant={eventStatusBadgeVariant(detail.status)}>{eventStatusLabel(detail.status)}</Badge>
              </div>

              {detail.description ? (
                <p className="whitespace-pre-wrap break-words text-muted-foreground text-sm leading-relaxed">
                  {detail.description}
                </p>
              ) : null}

              {detail.thumbnailFile || detail.bannerFile ? (
                <div className="space-y-3">
                  {detail.thumbnailFile ? (
                    <div className="space-y-1">
                      <p className="text-muted-foreground text-xs">썸네일</p>
                      {/* biome-ignore lint/performance/noImgElement: CDN 이미지 미리보기 */}
                      <img
                        src={detail.thumbnailFile.url}
                        alt={detail.thumbnailFile.name}
                        className="h-32 w-full rounded-md border object-cover"
                      />
                    </div>
                  ) : null}
                  {detail.bannerFile ? (
                    <div className="space-y-1">
                      <p className="text-muted-foreground text-xs">배너</p>
                      {/* biome-ignore lint/performance/noImgElement: CDN 이미지 미리보기 */}
                      <img
                        src={detail.bannerFile.url}
                        alt={detail.bannerFile.name}
                        className="h-32 w-full rounded-md border object-cover"
                      />
                    </div>
                  ) : null}
                </div>
              ) : null}

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">ID</dt>
                <dd className="tabular-nums">{detail.id}</dd>
                <dt className="text-muted-foreground">상태</dt>
                <dd>{eventStatusLabel(detail.status)}</dd>
                <dt className="text-muted-foreground">기간</dt>
                <dd className="tabular-nums">
                  {formatDateTime(detail.startAt)} ~ {formatDateTime(detail.endAt)}
                </dd>
              </dl>

              {detail.contentHtml ? (
                <>
                  <Separator />
                  <div className="space-y-1">
                    <p className="text-muted-foreground text-sm">본문 HTML</p>
                    <pre className="max-h-48 overflow-auto whitespace-pre-wrap break-words rounded-md border bg-muted/40 p-3 font-mono text-xs">
                      {detail.contentHtml}
                    </pre>
                  </div>
                </>
              ) : null}

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
