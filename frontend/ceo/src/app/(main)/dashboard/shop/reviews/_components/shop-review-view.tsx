"use client";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { MessageSquare, Store } from "lucide-react";

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
import { Separator } from "@/components/ui/separator";
import type { ShopSummary } from "@/feature/shop/domain";
import type {
  ReviewBlindReasonOption,
  ShopReviewDetail,
  ShopReviewListItem,
  ShopReviewSortTypeSetting,
  ShopReviewStatistics,
  ShopReviewTab,
} from "@/feature/shop-review/domain";
import { SHOP_REVIEW_COPY, SHOP_REVIEW_ERROR_MESSAGE } from "@/feature/shop-review/message";
import { cn } from "@/lib/utils";

import { ShopSelector } from "../../_components/shop-selector";
import { ShopReviewDetailSheet } from "./shop-review-detail-sheet";
import { ShopReviewFilters } from "./shop-review-filters";
import { ShopReviewItem } from "./shop-review-item";
import { ShopReviewSortTypeForm } from "./shop-review-sort-type-form";
import { ShopReviewStatisticsPanel } from "./shop-review-statistics";

export interface ShopReviewFilterState {
  tab: ShopReviewTab;
  startDate?: string;
  endDate?: string;
  rating?: number;
  orderMethod?: string;
  hasImage?: boolean;
  sortType?: string;
}

interface ShopReviewViewProps {
  shops: ShopSummary[];
  /** 가게 0건이면 undefined */
  shopId?: number;
  filters: ShopReviewFilterState;
  page: number;
  /** 목록 조회만 실패하면 undefined 로 넘어와 필터바는 살아있다 */
  items?: ShopReviewListItem[];
  totalPages?: number;
  errorCode?: string;
  errorMessage?: string;
  /** 통계만 실패해도 목록은 그대로 보여준다 */
  statistics?: ShopReviewStatistics;
  statisticsFailed?: boolean;
  sortTypeSetting?: ShopReviewSortTypeSetting;
  blindReasons: ReviewBlindReasonOption[];
  /** `?reviewId=` 로 서버가 함께 조회한 상세. 실패하면 undefined 라 시트를 열지 않는다 */
  detail?: ShopReviewDetail;
  detailErrorMessage?: string;
}

/** 상위에서 `null` 로 올라온 값은 URL 에서 키를 지운다(= 필터 해제) */
type OptionalParam = string | number | boolean | null | undefined;

export function ShopReviewView({
  shops,
  shopId,
  filters,
  page,
  items,
  totalPages = 0,
  errorCode,
  errorMessage,
  statistics,
  statisticsFailed = false,
  sortTypeSetting,
  blindReasons,
  detail,
  detailErrorMessage,
}: ShopReviewViewProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isPending, startTransition] = React.useTransition();

  function pushParams(next: {
    shopId?: number;
    tab?: string;
    startDate?: OptionalParam;
    endDate?: OptionalParam;
    rating?: OptionalParam;
    orderMethod?: OptionalParam;
    hasImage?: OptionalParam;
    sortType?: OptionalParam;
    reviewId?: OptionalParam;
    page?: number;
  }) {
    const params = new URLSearchParams(searchParams.toString());

    if (next.shopId !== undefined) params.set("shopId", String(next.shopId));
    if (next.tab !== undefined) params.set("tab", next.tab);

    // null 은 "전체"(또는 시트 닫기) — URL 에서 키를 지워 기본값으로 되돌린다.
    const optionalKeys = ["startDate", "endDate", "rating", "orderMethod", "hasImage", "sortType", "reviewId"] as const;
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
    ? ((errorCode && SHOP_REVIEW_ERROR_MESSAGE[errorCode]) ?? SHOP_REVIEW_COPY.LOAD_FAILED)
    : undefined;

  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{SHOP_REVIEW_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{SHOP_REVIEW_COPY.DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          {shopId !== undefined && (
            <ShopSelector
              shops={shops}
              shopId={shopId}
              disabled={isPending}
              // 가게를 바꾸면 열려 있던 상세는 다른 가게의 리뷰이므로 함께 닫는다.
              onChange={(nextShopId) => pushParams({ shopId: nextShopId, reviewId: null, page: 0 })}
            />
          )}
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-col gap-6">
        {shopId === undefined ? (
          <Empty>
            <EmptyHeader>
              <EmptyMedia variant="icon">
                <Store />
              </EmptyMedia>
              <EmptyTitle>{SHOP_REVIEW_COPY.NO_SHOP_TITLE}</EmptyTitle>
              <EmptyDescription>{SHOP_REVIEW_COPY.NO_SHOP_DESCRIPTION}</EmptyDescription>
            </EmptyHeader>
          </Empty>
        ) : (
          <>
            {/* ① 앱 노출 정렬 설정 — 필터의 sortType 과 별개다(이쪽은 고객 앱 반영용) */}
            <ShopReviewSortTypeForm shopId={shopId} setting={sortTypeSetting} disabled={isPending} />

            <Separator />

            {/* ② 통계 — hasData=false 면 컴포넌트가 스스로 아무것도 렌더하지 않는다 */}
            <ShopReviewStatisticsPanel statistics={statistics} failed={statisticsFailed} />

            {/* ③ 필터 */}
            <ShopReviewFilters
              tab={filters.tab}
              startDate={filters.startDate}
              endDate={filters.endDate}
              rating={filters.rating}
              orderMethod={filters.orderMethod}
              hasImage={filters.hasImage}
              sortType={filters.sortType}
              disabled={isPending}
              onTabChange={(tab) => pushParams({ tab, page: 0 })}
              onPeriodChange={({ startDate, endDate }) => pushParams({ startDate, endDate, page: 0 })}
              onRatingChange={(rating) => pushParams({ rating, page: 0 })}
              onOrderMethodChange={(orderMethod) => pushParams({ orderMethod, page: 0 })}
              onHasImageChange={(hasImage) => pushParams({ hasImage, page: 0 })}
              onSortTypeChange={(sortType) => pushParams({ sortType, page: 0 })}
              onReset={handleReset}
            />

            {inlineError && <p className="text-destructive text-sm">{inlineError}</p>}
            {/* 상세만 실패했으면 목록은 그대로 두고 안내만 띄운다. */}
            {detailErrorMessage && <p className="text-destructive text-sm">{SHOP_REVIEW_COPY.DETAIL_LOAD_FAILED}</p>}

            {/* ④ 목록 */}
            <div className={cn("flex flex-col gap-4", isPending && "pointer-events-none opacity-60")}>
              {!items || items.length === 0 ? (
                <Empty>
                  <EmptyHeader>
                    <EmptyMedia variant="icon">
                      <MessageSquare />
                    </EmptyMedia>
                    <EmptyTitle>{SHOP_REVIEW_COPY.EMPTY_TITLE}</EmptyTitle>
                    <EmptyDescription>{SHOP_REVIEW_COPY.EMPTY_DESCRIPTION}</EmptyDescription>
                  </EmptyHeader>
                </Empty>
              ) : (
                <Accordion type="single" collapsible className="w-full">
                  {items.map((item) => (
                    <ShopReviewItem
                      key={item.id}
                      shopId={shopId}
                      item={item}
                      blindReasons={blindReasons}
                      onOpenDetail={(reviewId) => pushParams({ reviewId })}
                    />
                  ))}
                </Accordion>
              )}

              {/* ⑤ 페이지네이션 */}
              {totalPages > 1 && (
                <Pagination>
                  <PaginationContent>
                    {page > 0 && (
                      <PaginationItem>
                        <PaginationPrevious href={pageHref(page - 1)} text={SHOP_REVIEW_COPY.PREVIOUS_PAGE} />
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
                        <PaginationNext href={pageHref(page + 1)} text={SHOP_REVIEW_COPY.NEXT_PAGE} />
                      </PaginationItem>
                    )}
                  </PaginationContent>
                </Pagination>
              )}
            </div>

            {/* 상세는 서버가 `?reviewId=` 를 읽어 함께 내려준다(repository 가 server-only 라 클라이언트 fetch 불가). */}
            {detail && (
              <ShopReviewDetailSheet
                shopId={shopId}
                detail={detail}
                blindReasons={blindReasons}
                onClose={() => pushParams({ reviewId: null })}
              />
            )}
          </>
        )}
      </CardContent>
    </Card>
  );
}
