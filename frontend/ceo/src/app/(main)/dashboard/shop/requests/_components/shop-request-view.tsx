"use client";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { ClipboardList, Store } from "lucide-react";

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
import type {
  ShopRequestComment,
  ShopRequestDetail,
  ShopRequestListItem,
  ShopRequestStatusOption,
  ShopRequestTypeOption,
  ShopSummary,
} from "@/feature/shop/domain";
import { SHOP_REQUEST_COPY } from "@/feature/shop/message";
import { cn } from "@/lib/utils";

import { ShopSelector } from "../../_components/shop-selector";
import { ShopRequestDetailSheet } from "./shop-request-detail-sheet";
import { ShopRequestFilters } from "./shop-request-filters";
import { ShopRequestItem } from "./shop-request-item";

/** 시작일 > 종료일 요청은 필터바 아래 인라인 안내로 처리한다 */
const DATE_RANGE_INVALID_CODE = "SHOP_REQUEST_DATE_RANGE_INVALID";

export interface ShopRequestFilterState {
  requestType?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
}

interface ShopRequestViewProps {
  shops: ShopSummary[];
  /** 가게 0건이면 undefined */
  shopId?: number;
  requestTypes: ShopRequestTypeOption[];
  statuses: ShopRequestStatusOption[];
  filters: ShopRequestFilterState;
  page: number;
  /** 목록 조회만 실패하면 undefined 로 넘어와 필터바는 살아있다 */
  items?: ShopRequestListItem[];
  totalPages?: number;
  errorCode?: string;
  errorMessage?: string;
  /** `?requestId=` 로 서버가 함께 조회한 상세. 실패하면 undefined 라 시트를 열지 않는다 */
  detail?: ShopRequestDetail;
  comments?: ShopRequestComment[];
  detailErrorCode?: string;
  detailErrorMessage?: string;
}

export function ShopRequestView({
  shops,
  shopId,
  requestTypes,
  statuses,
  filters,
  page,
  items,
  totalPages = 0,
  errorCode,
  errorMessage,
  detail,
  comments,
  detailErrorMessage,
}: ShopRequestViewProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isPending, startTransition] = React.useTransition();

  function pushParams(next: {
    shopId?: number;
    requestType?: string | null;
    status?: string | null;
    startDate?: string | null;
    endDate?: string | null;
    requestId?: number | null;
    page?: number;
  }) {
    const params = new URLSearchParams(searchParams.toString());

    if (next.shopId !== undefined) params.set("shopId", String(next.shopId));

    // null 은 "전체"(또는 시트 닫기) — URL 에서 키를 지워 기본값으로 되돌린다.
    const optionalKeys = ["requestType", "status", "startDate", "endDate", "requestId"] as const;
    for (const key of optionalKeys) {
      const value = next[key];
      if (value === undefined) continue;
      if (value === null) params.delete(key);
      else params.set(key, String(value));
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

  /** 가게 선택은 유지하고 필터만 전부 지운다 */
  function handleReset() {
    const params = new URLSearchParams();
    if (shopId !== undefined) params.set("shopId", String(shopId));
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  const inlineError = errorMessage
    ? errorCode === DATE_RANGE_INVALID_CODE
      ? SHOP_REQUEST_COPY.DATE_RANGE_INVALID
      : errorCode === "SHOP_ACCESS_DENIED"
        ? SHOP_REQUEST_COPY.SHOP_ACCESS_DENIED
        : errorCode === "SHOP_NOT_FOUND"
          ? SHOP_REQUEST_COPY.SHOP_NOT_FOUND
          : SHOP_REQUEST_COPY.LOAD_FAILED
    : undefined;

  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{SHOP_REQUEST_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{SHOP_REQUEST_COPY.DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          {shopId !== undefined && (
            <ShopSelector
              shops={shops}
              shopId={shopId}
              disabled={isPending}
              // 가게를 바꾸면 열려 있던 상세는 다른 가게의 요청이므로 함께 닫는다.
              onChange={(nextShopId) => pushParams({ shopId: nextShopId, requestId: null, page: 0 })}
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
              <EmptyTitle>{SHOP_REQUEST_COPY.NO_SHOP_TITLE}</EmptyTitle>
              <EmptyDescription>{SHOP_REQUEST_COPY.NO_SHOP_DESCRIPTION}</EmptyDescription>
            </EmptyHeader>
          </Empty>
        ) : (
          <>
            <ShopRequestFilters
              requestTypes={requestTypes}
              statuses={statuses}
              requestType={filters.requestType}
              status={filters.status}
              startDate={filters.startDate}
              endDate={filters.endDate}
              disabled={isPending}
              onRequestTypeChange={(requestType) => pushParams({ requestType, page: 0 })}
              onStatusChange={(status) => pushParams({ status, page: 0 })}
              onPeriodChange={({ startDate, endDate }) => pushParams({ startDate, endDate, page: 0 })}
              onReset={handleReset}
            />

            {inlineError && <p className="text-destructive text-sm">{inlineError}</p>}
            {/* 상세만 실패했으면 목록은 그대로 두고 안내만 띄운다. */}
            {detailErrorMessage && <p className="text-destructive text-sm">{SHOP_REQUEST_COPY.DETAIL_LOAD_FAILED}</p>}

            <div className={cn("flex flex-col gap-4", isPending && "pointer-events-none opacity-60")}>
              {!items || items.length === 0 ? (
                <Empty>
                  <EmptyHeader>
                    <EmptyMedia variant="icon">
                      <ClipboardList />
                    </EmptyMedia>
                    <EmptyTitle>{SHOP_REQUEST_COPY.EMPTY_TITLE}</EmptyTitle>
                    <EmptyDescription>{SHOP_REQUEST_COPY.EMPTY_DESCRIPTION}</EmptyDescription>
                  </EmptyHeader>
                </Empty>
              ) : (
                <Accordion type="single" collapsible className="w-full">
                  {items.map((item) => (
                    <ShopRequestItem
                      key={item.requestId}
                      item={item}
                      onOpenDetail={(requestId) => pushParams({ requestId })}
                    />
                  ))}
                </Accordion>
              )}

              {totalPages > 1 && (
                <Pagination>
                  <PaginationContent>
                    {page > 0 && (
                      <PaginationItem>
                        <PaginationPrevious href={pageHref(page - 1)} text={SHOP_REQUEST_COPY.PREVIOUS_PAGE} />
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
                        <PaginationNext href={pageHref(page + 1)} text={SHOP_REQUEST_COPY.NEXT_PAGE} />
                      </PaginationItem>
                    )}
                  </PaginationContent>
                </Pagination>
              )}
            </div>

            {/* 상세는 서버가 `?requestId=` 를 읽어 함께 내려준다(repository 가 server-only 라 클라이언트 fetch 불가). */}
            {detail && (
              <ShopRequestDetailSheet
                shopId={shopId}
                detail={detail}
                comments={comments ?? []}
                commentsFailed={comments === undefined}
                onClose={() => pushParams({ requestId: null })}
              />
            )}
          </>
        )}
      </CardContent>
    </Card>
  );
}
