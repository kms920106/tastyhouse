"use client";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { History, Store } from "lucide-react";

import { Accordion } from "@/components/ui/accordion";
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import type { ShopChangeCategoryOption, ShopChangeHistoryItem, ShopSummary } from "@/feature/shop/domain";
import { SHOP_CHANGE_HISTORY_COPY } from "@/feature/shop/message";
import { cn } from "@/lib/utils";

import { ShopSelector } from "../../_components/shop-selector";
import { ChangeHistoryFilters } from "./change-history-filters";
import { ChangeHistoryItem } from "./change-history-item";

/** 6개월 초과 조회 요청은 필터바 아래 인라인 안내로 처리한다 */
const DATE_OUT_OF_RANGE_CODE = "SHOP_CHANGE_HISTORY_DATE_OUT_OF_RANGE";

export interface ChangeHistoryFilterState {
  category?: string;
  changeType?: string;
  changedDate: string;
}

interface ChangeHistoryViewProps {
  shops: ShopSummary[];
  /** 가게 0건이면 undefined */
  shopId?: number;
  /** URL 의 shopId 가 내 가게 목록에 없어 첫 가게로 대체됐는지 */
  isShopFallback?: boolean;
  categories: ShopChangeCategoryOption[];
  filters: ChangeHistoryFilterState;
  page: number;
  /** `<input type="date">` 의 선택 가능 범위 (오늘 - 6개월 ~ 오늘) */
  minDate: string;
  maxDate: string;
  /** 목록 조회만 실패하면 undefined 로 넘어와 필터바는 살아있다 */
  items?: ShopChangeHistoryItem[];
  totalPages?: number;
  errorCode?: string;
  errorMessage?: string;
}

export function ChangeHistoryView({
  shops,
  shopId,
  isShopFallback = false,
  categories,
  filters,
  page,
  minDate,
  maxDate,
  items,
  totalPages = 0,
  errorCode,
  errorMessage,
}: ChangeHistoryViewProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isPending, startTransition] = React.useTransition();

  function pushParams(next: {
    shopId?: number;
    category?: string | null;
    changeType?: string | null;
    changedDate?: string;
    page?: number;
  }) {
    const params = new URLSearchParams(searchParams.toString());

    if (next.shopId !== undefined) params.set("shopId", String(next.shopId));
    if (next.changedDate !== undefined) params.set("changedDate", next.changedDate);

    // null 은 "전체" 선택 — URL 에서 키를 지워 기본값으로 되돌린다.
    if (next.category !== undefined) {
      if (next.category === null) params.delete("category");
      else params.set("category", next.category);
    }
    if (next.changeType !== undefined) {
      if (next.changeType === null) params.delete("changeType");
      else params.set("changeType", next.changeType);
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

  const isDateOutOfRange = errorCode === DATE_OUT_OF_RANGE_CODE;
  const inlineError = errorMessage
    ? isDateOutOfRange
      ? SHOP_CHANGE_HISTORY_COPY.DATE_OUT_OF_RANGE
      : SHOP_CHANGE_HISTORY_COPY.LIST_LOAD_FAILED
    : undefined;

  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{SHOP_CHANGE_HISTORY_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{SHOP_CHANGE_HISTORY_COPY.DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          {shopId !== undefined && (
            <ShopSelector
              shops={shops}
              shopId={shopId}
              disabled={isPending}
              onChange={(nextShopId) => pushParams({ shopId: nextShopId, page: 0 })}
            />
          )}
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        {shopId === undefined ? (
          <Empty>
            <EmptyHeader>
              <EmptyMedia variant="icon">
                <Store />
              </EmptyMedia>
              <EmptyTitle>{SHOP_CHANGE_HISTORY_COPY.SHOP_EMPTY_TITLE}</EmptyTitle>
              <EmptyDescription>{SHOP_CHANGE_HISTORY_COPY.SHOP_EMPTY_DESCRIPTION}</EmptyDescription>
            </EmptyHeader>
          </Empty>
        ) : (
          <>
            {isShopFallback && (
              <p className="text-muted-foreground text-sm">{SHOP_CHANGE_HISTORY_COPY.SHOP_FALLBACK_NOTICE}</p>
            )}

            <ChangeHistoryFilters
              categories={categories}
              category={filters.category}
              changeType={filters.changeType}
              changedDate={filters.changedDate}
              minDate={minDate}
              maxDate={maxDate}
              disabled={isPending}
              onCategoryChange={(category) => pushParams({ category, changeType: null, page: 0 })}
              onChangeTypeChange={(changeType) => pushParams({ changeType, page: 0 })}
              onChangedDateChange={(changedDate) => pushParams({ changedDate, page: 0 })}
              onSearch={() => startTransition(() => router.refresh())}
            />

            {inlineError && <p className="text-destructive text-sm">{inlineError}</p>}

            <div className={cn("flex flex-col gap-4", isPending && "pointer-events-none opacity-60")}>
              {!items || items.length === 0 ? (
                <Empty>
                  <EmptyHeader>
                    <EmptyMedia variant="icon">
                      <History />
                    </EmptyMedia>
                    <EmptyTitle>{SHOP_CHANGE_HISTORY_COPY.LIST_EMPTY_TITLE}</EmptyTitle>
                    <EmptyDescription>{SHOP_CHANGE_HISTORY_COPY.LIST_EMPTY_DESCRIPTION}</EmptyDescription>
                  </EmptyHeader>
                </Empty>
              ) : (
                <Accordion type="single" collapsible className="w-full">
                  {items.map((item) => (
                    <ChangeHistoryItem key={item.id} item={item} />
                  ))}
                </Accordion>
              )}

              {totalPages > 1 && (
                <Pagination>
                  <PaginationContent>
                    {page > 0 && (
                      <PaginationItem>
                        <PaginationPrevious href={pageHref(page - 1)} text={SHOP_CHANGE_HISTORY_COPY.PREVIOUS_PAGE} />
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
                        <PaginationNext href={pageHref(page + 1)} text={SHOP_CHANGE_HISTORY_COPY.NEXT_PAGE} />
                      </PaginationItem>
                    )}
                  </PaginationContent>
                </Pagination>
              )}
            </div>
          </>
        )}
      </CardContent>
    </Card>
  );
}
