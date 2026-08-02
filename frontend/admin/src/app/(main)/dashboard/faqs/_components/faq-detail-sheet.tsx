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
import { fetchFaqAction } from "@/feature/faq/actions";
import type { FaqDetail } from "@/feature/faq/domain";
import { FAQ_MESSAGE } from "@/feature/faq/message";
import { formatDateTime } from "@/lib/date";

interface FaqDetailSheetProps {
  /** 조회할 FAQ ID. null 이면 닫힌 상태. */
  faqId: number | null;
  onOpenChange: (open: boolean) => void;
  categoryNameById: Map<number, string>;
}

export function FaqDetailSheet({ faqId, onOpenChange, categoryNameById }: FaqDetailSheetProps) {
  const [detail, setDetail] = React.useState<FaqDetail | null>(null);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    if (faqId == null) {
      return;
    }

    let active = true;
    setIsLoading(true);
    setError(null);
    setDetail(null);

    void fetchFaqAction(faqId).then((result) => {
      const { success, message, data } = result;

      if (!active) return;
      if (success && data) {
        setDetail(data);
      } else {
        setError(message ?? FAQ_MESSAGE.DETAIL_LOAD_FAILED);
      }
      setIsLoading(false);
    });

    return () => {
      active = false;
    };
  }, [faqId]);

  return (
    <Sheet open={faqId != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>FAQ 상세</SheetTitle>
          <SheetDescription>FAQ 항목의 상세 정보를 확인합니다.</SheetDescription>
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
                <h3 className="font-semibold text-lg leading-snug">{detail.question}</h3>
                <Badge variant={detail.visible ? "default" : "secondary"}>{detail.visible ? "노출" : "미노출"}</Badge>
              </div>

              <Separator />

              <div className="whitespace-pre-wrap break-words text-sm leading-relaxed">{detail.answer}</div>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">ID</dt>
                <dd className="tabular-nums">{detail.id}</dd>
                <dt className="text-muted-foreground">카테고리</dt>
                <dd>{categoryNameById.get(detail.faqCategoryId) ?? "-"}</dd>
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
