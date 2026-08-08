"use client";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { Store } from "lucide-react";

import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { SHOP_MANAGE_TABS, type ShopManageTab } from "@/feature/shop/constants";
import type { ShopBasicInfo, ShopOperationInfo, ShopSummary } from "@/feature/shop/domain";
import { SHOP_PAGE_COPY } from "@/feature/shop/message";

import { BasicInfoTab } from "./basic-info-tab";
import { OperationInfoTab } from "./operation-info-tab";
import { ShopSelector } from "./shop-selector";

interface ShopManageProps {
  shops: ShopSummary[];
  shopId?: number;
  tab: ShopManageTab;
  basicInfo?: ShopBasicInfo;
  operationInfo?: ShopOperationInfo;
}

export function ShopManage({ shops, shopId, tab, basicInfo, operationInfo }: ShopManageProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isPending, startTransition] = React.useTransition();

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
          {shopId !== undefined && (
            <ShopSelector
              shops={shops}
              shopId={shopId}
              disabled={isPending}
              onChange={(nextShopId) => pushParams({ shopId: nextShopId })}
            />
          )}
        </CardAction>
      </CardHeader>
      <CardContent>
        {shopId === undefined || !basicInfo || !operationInfo ? (
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
            </TabsList>
            <TabsContent value={SHOP_MANAGE_TABS.BASIC}>
              <BasicInfoTab shopId={shopId} basicInfo={basicInfo} />
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
          </Tabs>
        )}
      </CardContent>
    </Card>
  );
}
