"use client";

import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { SHOP_OPERATION_COPY } from "@/feature/shop/message";

interface DeliveryAreaFooterProps {
  /** 저장하면 새로 열릴 지역 수 */
  addedCount: number;
  /** 저장하면 닫힐 지역 수 */
  removedCount: number;
  onCancel: () => void;
  onSave: () => void;
  isDirty: boolean;
  isPending: boolean;
  /** 저장 후 배달지역이 0건이 되는지 — 경고를 띄운다 */
  willBeEmpty: boolean;
}

/**
 * 변경 요약 + 취소/저장.
 *
 * `+N / -M` 을 상시 보여준다. 지도로 칠하는 편집은 무엇이 바뀌었는지 눈으로 세기 어려워,
 * 저장 직전에야 결과를 알면 되돌리기 비용이 크다.
 */
export function DeliveryAreaFooter({
  addedCount,
  removedCount,
  onCancel,
  onSave,
  isDirty,
  isPending,
  willBeEmpty,
}: DeliveryAreaFooterProps) {
  return (
    <div className="flex flex-wrap items-center justify-between gap-3 border-t bg-background px-4 py-3">
      <div className="flex flex-col gap-1">
        <div className="flex items-center gap-3 text-sm tabular-nums">
          <span className="text-muted-foreground">변경</span>
          <span className="font-medium text-primary">+{addedCount}</span>
          <span className="font-medium text-destructive">-{removedCount}</span>
        </div>
        {willBeEmpty ? (
          <p className="text-destructive text-xs">{SHOP_OPERATION_COPY.DELIVERY_AREA_EMPTY_WARNING}</p>
        ) : (
          // 지도는 축소하면 빈 구역이 보이지 않는다 — 저장 전에 확대 확인을 권한다.
          isDirty && (
            <p className="text-muted-foreground text-xs">{SHOP_OPERATION_COPY.DELIVERY_AREA_MISSING_CHECK_HINT}</p>
          )
        )}
      </div>

      <div className="flex items-center gap-2">
        <Button type="button" variant="outline" onClick={onCancel} disabled={isPending}>
          취소
        </Button>
        {/* 바뀐 게 없으면 저장을 막는다 — 빈 요청으로 서버 검증을 돌릴 이유가 없다 */}
        <Button type="button" onClick={onSave} disabled={!isDirty || isPending}>
          {isPending && <Spinner className="size-4" />}
          저장
        </Button>
      </div>
    </div>
  );
}
