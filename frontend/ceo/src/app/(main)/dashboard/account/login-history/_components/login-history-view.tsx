"use client";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { KeyRound } from "lucide-react";

import { Accordion } from "@/components/ui/accordion";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import type { CeoLoginHistoryItem } from "@/feature/ceo/domain";
import { CEO_LOGIN_HISTORY_COPY } from "@/feature/ceo/message";
import { cn } from "@/lib/utils";

import { LoginHistoryFilters } from "./login-history-filters";
import { LoginHistoryItem } from "./login-history-item";

/** 보관 기간(90일) 초과 요청은 필터바 아래 인라인 안내로 처리한다 */
const DATE_OUT_OF_RANGE_CODE = "CEO_LOGIN_HISTORY_DATE_OUT_OF_RANGE";
/** 시작일 > 종료일 — 도메인 중립 상수를 서버가 재사용한다(`docs/tasks/backend.md` §2-1) */
const DATE_RANGE_INVALID_CODE = "SHOP_REQUEST_DATE_RANGE_INVALID";

export interface LoginHistoryFilterState {
  result?: string;
  startDate?: string;
  endDate?: string;
}

interface LoginHistoryViewProps {
  filters: LoginHistoryFilterState;
  page: number;
  /** 피커의 선택 가능 범위 (오늘 - 90일 ~ 오늘) */
  minDate: string;
  maxDate: string;
  /** 목록 조회만 실패하면 undefined 로 넘어와 필터바는 살아있다 */
  items?: CeoLoginHistoryItem[];
  totalPages?: number;
  errorCode?: string;
  errorMessage?: string;
}

export function LoginHistoryView({
  filters,
  page,
  minDate,
  maxDate,
  items,
  totalPages = 0,
  errorCode,
  errorMessage,
}: LoginHistoryViewProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isPending, startTransition] = React.useTransition();

  function pushParams(next: {
    result?: string | null;
    startDate?: string | null;
    endDate?: string | null;
    page?: number;
  }) {
    const params = new URLSearchParams(searchParams.toString());

    // null 은 "전체" 선택 — URL 에서 키를 지워 기본값으로 되돌린다.
    const optionalKeys = ["result", "startDate", "endDate"] as const;
    for (const key of optionalKeys) {
      const value = next[key];
      if (value === undefined) continue;
      if (value === null) params.delete(key);
      else params.set(key, value);
    }

    // 필터가 바뀌면 page 를 0 으로 리셋한다 — 3페이지를 보다가 필터를 바꿨을 때 빈 페이지가 나오는 것을 막는다.
    if (next.page !== undefined) params.set("page", String(next.page));

    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  function pageHref(nextPage: number) {
    const params = new URLSearchParams(searchParams.toString());
    params.set("page", String(nextPage));
    return `?${params.toString()}`;
  }

  /** 계정 단위 화면이라 유지할 컨텍스트가 없다 — 필터를 전부 지운다 */
  function handleReset() {
    startTransition(() => {
      router.push("?");
    });
  }

  const inlineError = errorMessage
    ? errorCode === DATE_OUT_OF_RANGE_CODE
      ? CEO_LOGIN_HISTORY_COPY.DATE_OUT_OF_RANGE
      : errorCode === DATE_RANGE_INVALID_CODE
        ? CEO_LOGIN_HISTORY_COPY.DATE_RANGE_INVALID
        : CEO_LOGIN_HISTORY_COPY.LOAD_FAILED
    : undefined;

  return (
    <Card>
      <CardHeader className="border-b">
        <CardTitle className="text-xl leading-none">{CEO_LOGIN_HISTORY_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-lg leading-snug">{CEO_LOGIN_HISTORY_COPY.DESCRIPTION}</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        <p className="text-muted-foreground text-sm">{CEO_LOGIN_HISTORY_COPY.RETENTION_NOTICE}</p>

        <LoginHistoryFilters
          result={filters.result}
          startDate={filters.startDate}
          endDate={filters.endDate}
          minDate={minDate}
          maxDate={maxDate}
          disabled={isPending}
          onResultChange={(result) => pushParams({ result, page: 0 })}
          onPeriodChange={({ startDate, endDate }) => pushParams({ startDate, endDate, page: 0 })}
          onSearch={() => startTransition(() => router.refresh())}
          onReset={handleReset}
        />

        {inlineError && <p className="text-destructive text-sm">{inlineError}</p>}

        <div className={cn("flex flex-col gap-4", isPending && "pointer-events-none opacity-60")}>
          {!items || items.length === 0 ? (
            <Empty>
              <EmptyHeader>
                <EmptyMedia variant="icon">
                  <KeyRound />
                </EmptyMedia>
                <EmptyTitle>{CEO_LOGIN_HISTORY_COPY.EMPTY_TITLE}</EmptyTitle>
                {/* 백필하지 않으므로 기존 사용자는 처음 이 화면을 열면 반드시 빈 목록을 본다. */}
                <EmptyDescription>{CEO_LOGIN_HISTORY_COPY.EMPTY_DESCRIPTION}</EmptyDescription>
              </EmptyHeader>
            </Empty>
          ) : (
            <Accordion type="single" collapsible className="w-full">
              {items.map((item) => (
                <LoginHistoryItem key={item.id} item={item} />
              ))}
            </Accordion>
          )}

          {totalPages > 1 && (
            <Pagination>
              <PaginationContent>
                {page > 0 && (
                  <PaginationItem>
                    <PaginationPrevious href={pageHref(page - 1)} text={CEO_LOGIN_HISTORY_COPY.PREVIOUS_PAGE} />
                  </PaginationItem>
                )}
                {Array.from({ length: totalPages }, (_, index) => index).map((pageIndex) => (
                  <PaginationItem key={pageIndex}>
                    <PaginationLink href={pageHref(pageIndex)} isActive={pageIndex === page}>
                      {pageIndex + 1}
                    </PaginationLink>
                  </PaginationItem>
                ))}
                {page < totalPages - 1 && (
                  <PaginationItem>
                    <PaginationNext href={pageHref(page + 1)} text={CEO_LOGIN_HISTORY_COPY.NEXT_PAGE} />
                  </PaginationItem>
                )}
              </PaginationContent>
            </Pagination>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
