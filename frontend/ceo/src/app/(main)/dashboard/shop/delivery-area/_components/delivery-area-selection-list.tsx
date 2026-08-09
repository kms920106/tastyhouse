"use client";

import { Lock, X } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { DELIVERY_AREA_MAX_COUNT } from "@/feature/shop/constants";
import { SHOP_OPERATION_COPY } from "@/feature/shop/message";
import { cn } from "@/lib/utils";

/** 목록에 표시할 한 건 — ID 만으로는 이름을 알 수 없어 셸이 이름까지 붙여 내려준다 */
export interface SelectedRegion {
  adminDongId: number;
  regionName: string;
  isLocked: boolean;
  /** 지도 도형에서 환산된 지역인지 — 개별 제거 대신 도형을 고쳐야 한다 */
  fromPolygon: boolean;
}

interface DeliveryAreaSelectionListProps {
  regions: SelectedRegion[];
  onRemove: (adminDongId: number) => void;
  disabled?: boolean;
}

/**
 * 선택된 행정동 목록.
 *
 * 지도의 채움·외곽선과 함께 "선택됨"을 표현하는 세 번째 경로다. 색만으로 선택을 구분하지
 * 않기 위해 목록이 항상 함께 있어야 한다.
 */
export function DeliveryAreaSelectionList({ regions, onRemove, disabled = false }: DeliveryAreaSelectionListProps) {
  const isOverLimit = regions.length > DELIVERY_AREA_MAX_COUNT;

  return (
    <div className="flex flex-col gap-2">
      <div className="flex items-center justify-between gap-2">
        <span className="font-medium text-sm">{SHOP_OPERATION_COPY.DELIVERY_AREA_LIST_LEGEND}</span>
        {/* 선택 변화를 스크린리더에 알린다 — 지도는 시각 채널이라 이것이 유일한 통지 경로다 */}
        <span
          aria-live="polite"
          className={cn("text-muted-foreground text-xs tabular-nums", isOverLimit && "text-destructive")}
        >
          {regions.length}개 지역 선택됨
        </span>
      </div>

      {isOverLimit && (
        <p className="text-destructive text-xs">
          배달가능지역은 최대 {DELIVERY_AREA_MAX_COUNT}개까지 등록할 수 있습니다.
        </p>
      )}

      {regions.length === 0 ? (
        <p className="rounded-md border border-dashed p-4 text-center text-muted-foreground text-sm">
          {SHOP_OPERATION_COPY.DELIVERY_AREA_LIST_EMPTY}
        </p>
      ) : (
        <ul className="flex max-h-64 flex-col overflow-y-auto">
          {regions.map((region) => (
            <li key={region.adminDongId} className="flex items-center gap-2 border-b py-2 last:border-b-0">
              <span className="min-w-0 flex-1 truncate text-sm">{region.regionName}</span>

              {region.fromPolygon && (
                <span className="shrink-0 rounded border px-1.5 py-0.5 text-[10px] text-muted-foreground">지도</span>
              )}

              {region.isLocked ? (
                <Tooltip>
                  <TooltipTrigger asChild>
                    <span className="shrink-0 p-1 text-muted-foreground">
                      <Lock className="size-4" />
                      <span className="sr-only">{SHOP_OPERATION_COPY.DELIVERY_AREA_LOCKED_BY_TIP}</span>
                    </span>
                  </TooltipTrigger>
                  <TooltipContent>{SHOP_OPERATION_COPY.DELIVERY_AREA_LOCKED_BY_TIP}</TooltipContent>
                </Tooltip>
              ) : (
                <Button
                  type="button"
                  size="icon"
                  variant="ghost"
                  className="shrink-0"
                  onClick={() => onRemove(region.adminDongId)}
                  disabled={disabled}
                  aria-label={`${region.regionName} 삭제`}
                >
                  <X className="size-4" />
                </Button>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
