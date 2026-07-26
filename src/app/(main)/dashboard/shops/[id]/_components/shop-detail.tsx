"use client";

import * as React from "react";

import { useRouter } from "next/navigation";

import { ArrowLeft, LayoutGrid, Pencil } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { SHOP_DETAIL_TABS } from "@/feature/shop/constants";
import type { ShopDetail as ShopDetailModel } from "@/feature/shop/domain";
import { formatDateTime } from "@/lib/date";

import { ShopFormSheet } from "../../_components/shop-form-sheet";
import { BusinessHoursTab } from "./business-hours-tab";
import { ClassificationTab } from "./classification-tab";
import { HygieneBadgesTab } from "./hygiene-badges-tab";
import { ImagesTab } from "./images-tab";

interface ShopDetailProps {
  shop: ShopDetailModel;
}

export function ShopDetail({ shop }: ShopDetailProps) {
  const router = useRouter();
  const [formOpen, setFormOpen] = React.useState(false);

  return (
    <div className="flex flex-col gap-4">
      <Card>
        <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
          <div className="flex items-center gap-2">
            <Button variant="ghost" size="icon" className="size-8" onClick={() => router.push("/dashboard/shops")}>
              <ArrowLeft className="size-4" />
            </Button>
            <CardTitle className="text-xl leading-none">{shop.name}</CardTitle>
            <Badge variant={shop.permanentlyClosed ? "destructive" : "default"}>
              {shop.permanentlyClosed ? "폐업" : "영업중"}
            </Badge>
          </div>
          <CardDescription className="max-w-lg leading-snug">{shop.roadAddress}</CardDescription>
          <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
            <Button
              size="sm"
              variant="outline"
              onClick={() => router.push(`/dashboard/shop-content-boards?shopId=${shop.id}`)}
            >
              <LayoutGrid /> 콘텐츠보드 검수
            </Button>
            <Button size="sm" onClick={() => setFormOpen(true)}>
              <Pencil /> 가게 수정
            </Button>
          </CardAction>
        </CardHeader>
        <CardContent className="space-y-4">
          <dl className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm md:grid-cols-4">
            <dt className="text-muted-foreground">ID</dt>
            <dd className="tabular-nums">{shop.id}</dd>
            <dt className="text-muted-foreground">지하철역 ID</dt>
            <dd className="tabular-nums">{shop.stationId}</dd>
            <dt className="text-muted-foreground">평점</dt>
            <dd className="tabular-nums">{shop.rating ?? "-"}</dd>
            <dt className="text-muted-foreground">전화번호</dt>
            <dd>{shop.phoneNumber ?? "-"}</dd>
            <dt className="text-muted-foreground">지번 주소</dt>
            <dd className="col-span-3">{shop.lotAddress}</dd>
            <dt className="text-muted-foreground">위도/경도</dt>
            <dd className="col-span-3 tabular-nums">
              {shop.latitude}, {shop.longitude}
            </dd>
          </dl>
          <Separator />
          <dl className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
            <dt className="text-muted-foreground">생성일시</dt>
            <dd className="tabular-nums">{formatDateTime(shop.createdAt)}</dd>
            <dt className="text-muted-foreground">수정일시</dt>
            <dd className="tabular-nums">{formatDateTime(shop.updatedAt)}</dd>
          </dl>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="pt-6">
          <Tabs defaultValue={SHOP_DETAIL_TABS.BUSINESS_HOURS}>
            <TabsList>
              <TabsTrigger value={SHOP_DETAIL_TABS.BUSINESS_HOURS}>운영정보</TabsTrigger>
              <TabsTrigger value={SHOP_DETAIL_TABS.CLASSIFICATION}>편의·음식·주문</TabsTrigger>
              <TabsTrigger value={SHOP_DETAIL_TABS.IMAGES}>이미지</TabsTrigger>
              <TabsTrigger value={SHOP_DETAIL_TABS.HYGIENE}>위생 인증</TabsTrigger>
            </TabsList>
            <TabsContent value={SHOP_DETAIL_TABS.BUSINESS_HOURS} className="pt-4">
              <BusinessHoursTab shopId={shop.id} />
            </TabsContent>
            <TabsContent value={SHOP_DETAIL_TABS.CLASSIFICATION} className="pt-4">
              <ClassificationTab shopId={shop.id} />
            </TabsContent>
            <TabsContent value={SHOP_DETAIL_TABS.IMAGES} className="pt-4">
              <ImagesTab shopId={shop.id} />
            </TabsContent>
            <TabsContent value={SHOP_DETAIL_TABS.HYGIENE} className="pt-4">
              <HygieneBadgesTab shopId={shop.id} />
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>

      <ShopFormSheet open={formOpen} onOpenChange={setFormOpen} shop={{ id: shop.id }} />
    </div>
  );
}
