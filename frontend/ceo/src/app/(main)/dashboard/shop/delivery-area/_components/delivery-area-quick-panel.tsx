"use client";

import * as React from "react";

import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import type { DeliveryAreaRadiusPreview } from "@/feature/shop/domain";
import { SHOP_OPERATION_COPY } from "@/feature/shop/message";
import type { DeliveryAreaRadiusFormValues } from "@/feature/shop/schema";

import { DeliveryAreaAdjustmentSheet } from "../../_components/delivery-area-adjustment-sheet";
import { DeliveryAreaDongSearch } from "./delivery-area-dong-search";
import { DeliveryAreaDongTree } from "./delivery-area-dong-tree";
import { DeliveryAreaRadiusForm } from "./delivery-area-radius-form";
import { DeliveryAreaSelectionList, type SelectedRegion } from "./delivery-area-selection-list";

interface DeliveryAreaQuickPanelProps {
  shopId: number;
  selectedDongIds: Set<number>;
  lockedDongIds: Set<number>;
  regions: SelectedRegion[];
  radiusPreview: DeliveryAreaRadiusPreview | null;
  onRadiusChange: (radiusKm: number) => void;
  onApplyRadius: (values: DeliveryAreaRadiusFormValues) => void;
  /** 이름은 호출한 쪽이 아는 만큼 함께 올린다 — 셸이 저장 전 표시용으로 캐시한다 */
  onToggleDong: (dongId: number, regionName?: string) => void;
  onToggleMany: (dongs: { adminDongId: number; regionName: string }[], selected: boolean) => void;
  onFocusDong: (dongId: number) => void;
  isPending: boolean;
}

/**
 * 빠른설정 패널 — 반경 / 행정동 선택.
 *
 * 행정동 탭 안에서는 검색과 트리를 **탭이 아니라 상하로** 둔다. 검색은 "이름을 아는 동을
 * 바로 찍는" 길이고 트리는 "훑어보며 고르는" 길이라 성격이 다르고, 탭으로 갈라 두면
 * 검색하다 트리로 돌아갈 때마다 상태가 끊긴다.
 */
export function DeliveryAreaQuickPanel({
  shopId,
  selectedDongIds,
  lockedDongIds,
  regions,
  radiusPreview,
  onRadiusChange,
  onApplyRadius,
  onToggleDong,
  onToggleMany,
  onFocusDong,
  isPending,
}: DeliveryAreaQuickPanelProps) {
  const [adjustmentOpen, setAdjustmentOpen] = React.useState(false);

  return (
    <div className="flex h-full flex-col gap-4 overflow-y-auto p-4">
      <Tabs defaultValue="radius" className="flex flex-col gap-4">
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="radius">{SHOP_OPERATION_COPY.DELIVERY_AREA_RADIUS_LABEL}</TabsTrigger>
          <TabsTrigger value="tree">{SHOP_OPERATION_COPY.DELIVERY_AREA_TREE_LABEL}</TabsTrigger>
        </TabsList>

        <TabsContent value="radius">
          <DeliveryAreaRadiusForm
            onRadiusChange={onRadiusChange}
            onApply={onApplyRadius}
            preview={radiusPreview}
            isPending={isPending}
          />
        </TabsContent>

        <TabsContent value="tree" className="flex flex-col gap-4">
          <DeliveryAreaDongSearch
            selectedDongIds={selectedDongIds}
            lockedDongIds={lockedDongIds}
            onToggleDong={onToggleDong}
            onFocusDong={onFocusDong}
            disabled={isPending}
          />

          <Separator />

          <DeliveryAreaDongTree
            selectedDongIds={selectedDongIds}
            lockedDongIds={lockedDongIds}
            onToggleDong={onToggleDong}
            onToggleMany={onToggleMany}
            disabled={isPending}
          />
        </TabsContent>
      </Tabs>

      <Separator />

      <DeliveryAreaSelectionList regions={regions} onRemove={onToggleDong} disabled={isPending} />

      <Separator />

      {/* 조정 신청 진입 — 기존 배달가능지역 시트 하단에 있던 자리를 이 라우트로 옮겨 왔다 */}
      <section className="flex flex-col gap-2">
        <span className="text-muted-foreground text-xs leading-snug">{SHOP_OPERATION_COPY.ADJUSTMENT_GUIDE}</span>
        <Button type="button" variant="outline" onClick={() => setAdjustmentOpen(true)} disabled={isPending}>
          {SHOP_OPERATION_COPY.ADJUSTMENT_TITLE}
        </Button>
      </section>

      <DeliveryAreaAdjustmentSheet open={adjustmentOpen} onOpenChange={setAdjustmentOpen} shopId={shopId} />
    </div>
  );
}
