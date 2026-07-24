"use client";

import * as React from "react";

import { ChevronLeft, ChevronRight } from "lucide-react";

import type { ApiPagination } from "@/api/shared/types";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
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
import type { MemberListItem } from "@/feature/member/domain";
import { fetchPointBalanceAction, fetchPointHistoriesAction } from "@/feature/point/actions";
import { POINT_TYPE_OPTIONS } from "@/feature/point/constants";
import type { PointBalance, PointHistoryItem, PointType } from "@/feature/point/domain";
import { formatSignedPoint, pointTypeBadgeVariant, pointTypeLabel } from "@/feature/point/format";
import { POINT_MESSAGE, POINT_PAGE_COPY } from "@/feature/point/message";
import { formatDateTime } from "@/lib/date";

import { DeductPointDialog } from "./deduct-point-dialog";
import { EarnPointDialog } from "./earn-point-dialog";

const HISTORY_PAGE_SIZE = 10;

interface MemberPointSheetProps {
  member: MemberListItem | null;
  onOpenChange: (open: boolean) => void;
}

export function MemberPointSheet({ member, onOpenChange }: MemberPointSheetProps) {
  const memberId = member?.id ?? null;

  const [balance, setBalance] = React.useState<PointBalance | null>(null);
  const [histories, setHistories] = React.useState<PointHistoryItem[]>([]);
  const [pagination, setPagination] = React.useState<ApiPagination | null>(null);
  const [typeFilter, setTypeFilter] = React.useState<string>("all");
  const [page, setPage] = React.useState(0);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [earning, setEarning] = React.useState(false);
  const [deducting, setDeducting] = React.useState(false);

  const reload = React.useCallback(() => {
    if (memberId == null) return;

    let active = true;
    setIsLoading(true);
    setError(null);

    const type = typeFilter === "all" ? undefined : (typeFilter as PointType);

    void Promise.all([
      fetchPointBalanceAction(memberId),
      fetchPointHistoriesAction(memberId, { type, page, size: HISTORY_PAGE_SIZE }),
    ]).then(([balanceResult, historyResult]) => {
      if (!active) return;

      if (balanceResult.success && balanceResult.data) {
        setBalance(balanceResult.data);
      } else {
        setError(balanceResult.message ?? POINT_MESSAGE.BALANCE_LOAD_FAILED);
      }

      if (historyResult.success) {
        setHistories(historyResult.data ?? []);
        setPagination(historyResult.pagination ?? null);
      } else {
        setError(historyResult.message ?? POINT_MESSAGE.HISTORY_LOAD_FAILED);
      }

      setIsLoading(false);
    });

    return () => {
      active = false;
    };
  }, [memberId, typeFilter, page]);

  // 회원/필터/페이지 변경 시 재조회
  React.useEffect(() => {
    if (memberId == null) return;
    const cleanup = reload();
    return cleanup;
  }, [memberId, reload]);

  // 시트가 닫히면 다음 조회를 위해 필터/페이지를 초기화한다.
  function handleOpenChange(open: boolean) {
    if (!open) {
      setTypeFilter("all");
      setPage(0);
    }
    onOpenChange(open);
  }

  const totalPages = Math.max(pagination?.totalPages ?? 1, 1);
  const canPrev = page > 0;
  const canNext = page < totalPages - 1;

  return (
    <Sheet open={memberId != null} onOpenChange={handleOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{POINT_PAGE_COPY.TITLE}</SheetTitle>
          <SheetDescription>
            {member ? `"${member.nickname}" 회원의 포인트를 관리합니다.` : POINT_PAGE_COPY.DESCRIPTION}
          </SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-5 overflow-y-auto px-4">
          <div className="grid grid-cols-2 gap-3">
            <Card>
              <CardContent className="space-y-1 px-4">
                <p className="text-muted-foreground text-sm">사용 가능 포인트</p>
                <p className="font-semibold text-2xl tabular-nums">
                  {(balance?.availablePoints ?? 0).toLocaleString()}
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="space-y-1 px-4">
                <p className="text-muted-foreground text-sm">이번 달 소멸 예정</p>
                <p className="font-semibold text-2xl tabular-nums">
                  {(balance?.expiredThisMonth ?? 0).toLocaleString()}
                </p>
              </CardContent>
            </Card>
          </div>

          <Separator />

          <div className="flex items-center justify-between gap-2">
            <h4 className="font-medium text-sm">포인트 이력</h4>
            <Select
              value={typeFilter}
              onValueChange={(value) => {
                setTypeFilter(value);
                setPage(0);
              }}
              disabled={isLoading}
            >
              <SelectTrigger size="sm">
                <span className="text-muted-foreground">유형:</span>
                <SelectValue />
              </SelectTrigger>
              <SelectContent position="popper" align="end">
                <SelectGroup>
                  <SelectItem value="all">전체</SelectItem>
                  {POINT_TYPE_OPTIONS.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectGroup>
              </SelectContent>
            </Select>
          </div>

          {isLoading ? (
            <div className="space-y-3">
              <Skeleton className="h-12 w-full" />
              <Skeleton className="h-12 w-full" />
              <Skeleton className="h-12 w-full" />
            </div>
          ) : error ? (
            <p className="text-destructive text-sm">{error}</p>
          ) : histories.length === 0 ? (
            <p className="text-muted-foreground text-sm">포인트 이력이 없습니다.</p>
          ) : (
            <ul className="space-y-2">
              {histories.map((item, index) => (
                <li
                  // biome-ignore lint/suspicious/noArrayIndexKey: 이력 응답에 고유 식별자가 없어 페이지 내 순서 기준 키 사용
                  key={`${item.createdAt}-${item.pointAmount}-${index}`}
                  className="flex items-start justify-between gap-3 rounded-md border p-3"
                >
                  <div className="space-y-1">
                    <Badge variant={pointTypeBadgeVariant(item.pointType)}>{pointTypeLabel(item.pointType)}</Badge>
                    <p className="whitespace-pre-wrap break-words text-sm leading-snug">{item.reason}</p>
                    <p className="text-muted-foreground text-xs tabular-nums">{formatDateTime(item.createdAt)}</p>
                  </div>
                  <span
                    className={
                      item.pointAmount < 0
                        ? "font-semibold text-destructive text-sm tabular-nums"
                        : "font-semibold text-sm tabular-nums"
                    }
                  >
                    {formatSignedPoint(item.pointAmount)}
                  </span>
                </li>
              ))}
            </ul>
          )}

          {pagination && totalPages > 1 ? (
            <div className="flex items-center justify-center gap-3">
              <Button
                variant="outline"
                size="icon"
                className="size-8"
                disabled={!canPrev || isLoading}
                onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                aria-label="이전 페이지"
              >
                <ChevronLeft className="size-4" />
              </Button>
              <span className="text-muted-foreground text-sm tabular-nums">
                {page + 1} / {totalPages}
              </span>
              <Button
                variant="outline"
                size="icon"
                className="size-8"
                disabled={!canNext || isLoading}
                onClick={() => setPage((prev) => prev + 1)}
                aria-label="다음 페이지"
              >
                <ChevronRight className="size-4" />
              </Button>
            </div>
          ) : null}
        </div>

        <SheetFooter>
          <div className="flex gap-2">
            <Button className="flex-1" onClick={() => setEarning(true)} disabled={memberId == null}>
              적립
            </Button>
            <Button
              variant="destructive"
              className="flex-1"
              onClick={() => setDeducting(true)}
              disabled={memberId == null}
            >
              차감
            </Button>
          </div>
          <SheetClose asChild>
            <Button variant="outline">닫기</Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>

      <EarnPointDialog memberId={memberId} open={earning} onOpenChange={setEarning} onSuccess={reload} />
      <DeductPointDialog memberId={memberId} open={deducting} onOpenChange={setDeducting} onSuccess={reload} />
    </Sheet>
  );
}
