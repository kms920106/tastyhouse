"use client";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { ClipboardList, History, Lock, MessageSquare, PackageX, Store } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { PRODUCT_AVAILABILITY_COPY } from "@/feature/product/message";
import { SHOP_MANAGE_TABS, type ShopManageTab } from "@/feature/shop/constants";
import type { ShopBasicInfo, ShopOperationInfo, ShopOrderAvailability, ShopSummary } from "@/feature/shop/domain";
import { SHOP_CHANGE_HISTORY_COPY, SHOP_PAGE_COPY, SHOP_REQUEST_COPY } from "@/feature/shop/message";
import type { ShopNoticeItem } from "@/feature/shop-notice/domain";
import { SHOP_REVIEW_COPY } from "@/feature/shop-review/message";

import { BasicInfoTab } from "./basic-info-tab";
import { OperationInfoTab } from "./operation-info-tab";
import { OrderInfoTab } from "./order-info-tab";
import { ShopSelector } from "./shop-selector";

interface ShopManageProps {
  shops: ShopSummary[];
  shopId?: number;
  tab: ShopManageTab;
  basicInfo?: ShopBasicInfo;
  operationInfo?: ShopOperationInfo;
  /** 주문정보 탭 데이터. 이 조회만 실패하면 undefined 로 넘어와 해당 탭에서만 실패를 알린다 */
  orderAvailability?: ShopOrderAvailability;
  /** 사장님 공지 목록. 조회 실패해도 기본정보 탭을 막지 않으므로 undefined 를 허용한다 */
  notices?: ShopNoticeItem[];
  /** 접근 불가 사유(403 `SHOP_ACCESS_DENIED` / 404 `SHOP_NOT_FOUND`). 있으면 탭 대신 인라인 안내를 띄운다 */
  errorCode?: string;
}

export function ShopManage({
  shops,
  shopId,
  tab,
  basicInfo,
  operationInfo,
  orderAvailability,
  notices,
  errorCode,
}: ShopManageProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isPending, startTransition] = React.useTransition();

  // 접근 불가 사유는 서버가 내려준 errorCode 로만 판정한다 — 데이터 부재(undefined)와 섞지 않는다.
  const accessDeniedMessage =
    errorCode === "SHOP_ACCESS_DENIED"
      ? SHOP_PAGE_COPY.SHOP_ACCESS_DENIED
      : errorCode === "SHOP_NOT_FOUND"
        ? SHOP_PAGE_COPY.SHOP_NOT_FOUND
        : undefined;

  function pushParams(next: { shopId?: number; tab?: ShopManageTab }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.shopId !== undefined) params.set("shopId", String(next.shopId));
    if (next.tab !== undefined) params.set("tab", next.tab);
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{SHOP_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{SHOP_PAGE_COPY.DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          {/* 접근 불가 상태에서는 선택기(가게 2개 이상에서만 뜨고 유효한 shopId 가 필요하다) 대신
              내 가게로 되돌아가는 버튼을 둔다 — 막다른 화면을 남기지 않는다. */}
          {accessDeniedMessage !== undefined && shops.length > 0 && (
            <Button
              type="button"
              variant="outline"
              disabled={isPending}
              onClick={() => pushParams({ shopId: shops[0].id })}
            >
              <Store />
              {SHOP_PAGE_COPY.BACK_TO_MY_SHOP}
            </Button>
          )}
          {accessDeniedMessage === undefined && shopId !== undefined && (
            <>
              <ShopSelector
                shops={shops}
                shopId={shopId}
                disabled={isPending}
                onChange={(nextShopId) => pushParams({ shopId: nextShopId })}
              />
              {/* 변경이력은 설정 항목이 아닌 조회 전용 화면이라 시트가 아니라 전용 라우트로 이동한다. */}
              <Button
                type="button"
                variant="outline"
                onClick={() => router.push(`/dashboard/shop/change-history?shopId=${shopId}`)}
              >
                <History />
                {SHOP_CHANGE_HISTORY_COPY.ENTRY_TITLE}
              </Button>
              {/* 요청처리 현황은 설정 항목이 아닌 조회 전용 화면이라 시트가 아니라 전용 라우트로 이동한다. */}
              <Button
                type="button"
                variant="outline"
                onClick={() => router.push(`/dashboard/shop/requests?shopId=${shopId}`)}
              >
                <ClipboardList />
                {SHOP_REQUEST_COPY.ENTRY_TITLE}
              </Button>
              {/* 리뷰 관리도 설정 항목이 아닌 조회·답변 화면이라 시트가 아니라 전용 라우트로 이동한다. */}
              <Button
                type="button"
                variant="outline"
                onClick={() => router.push(`/dashboard/shop/reviews?shopId=${shopId}`)}
              >
                <MessageSquare />
                {SHOP_REVIEW_COPY.ENTRY_TITLE}
              </Button>
              {/* 품절·숨김은 설정 항목이 아니라 메뉴·옵션 일괄 조작 화면이라 시트가 아니라 전용 라우트로 이동한다. */}
              <Button
                type="button"
                variant="outline"
                onClick={() => router.push(`/dashboard/shop/menus/availability?shopId=${shopId}`)}
              >
                <PackageX />
                {PRODUCT_AVAILABILITY_COPY.ENTRY_TITLE}
              </Button>
            </>
          )}
        </CardAction>
      </CardHeader>
      <CardContent>
        {accessDeniedMessage !== undefined ? (
          <Empty>
            <EmptyHeader>
              <EmptyMedia variant="icon">
                <Lock />
              </EmptyMedia>
              <EmptyTitle>{accessDeniedMessage}</EmptyTitle>
              <EmptyDescription>{SHOP_PAGE_COPY.SHOP_ACCESS_DENIED_DESCRIPTION}</EmptyDescription>
            </EmptyHeader>
          </Empty>
        ) : shopId === undefined || !basicInfo || !operationInfo ? (
          <Empty>
            <EmptyHeader>
              <EmptyMedia variant="icon">
                <Store />
              </EmptyMedia>
              <EmptyTitle>{SHOP_PAGE_COPY.EMPTY_TITLE}</EmptyTitle>
              <EmptyDescription>{SHOP_PAGE_COPY.EMPTY_DESCRIPTION}</EmptyDescription>
            </EmptyHeader>
          </Empty>
        ) : (
          <Tabs value={tab} onValueChange={(value) => pushParams({ tab: value as ShopManageTab })}>
            <TabsList>
              <TabsTrigger value={SHOP_MANAGE_TABS.BASIC}>{SHOP_PAGE_COPY.BASIC_TAB}</TabsTrigger>
              <TabsTrigger value={SHOP_MANAGE_TABS.OPERATION}>{SHOP_PAGE_COPY.OPERATION_TAB}</TabsTrigger>
              <TabsTrigger value={SHOP_MANAGE_TABS.ORDER}>{SHOP_PAGE_COPY.ORDER_TAB}</TabsTrigger>
            </TabsList>
            <TabsContent value={SHOP_MANAGE_TABS.BASIC}>
              <BasicInfoTab shopId={shopId} basicInfo={basicInfo} notices={notices ?? []} />
            </TabsContent>
            <TabsContent value={SHOP_MANAGE_TABS.OPERATION}>
              {/* 최소주문금액·예약주문은 가게 상세(basicInfo)에서 오지만 주문 운영 설정이므로 운영정보 탭에서 노출한다 */}
              <OperationInfoTab
                shopId={shopId}
                operationInfo={operationInfo}
                minOrderAmount={basicInfo?.minOrderAmount}
                scheduledOrderEnabled={basicInfo?.scheduledOrderEnabled}
              />
            </TabsContent>
            <TabsContent value={SHOP_MANAGE_TABS.ORDER}>
              <OrderInfoTab orderAvailability={orderAvailability} />
            </TabsContent>
          </Tabs>
        )}
      </CardContent>
    </Card>
  );
}
