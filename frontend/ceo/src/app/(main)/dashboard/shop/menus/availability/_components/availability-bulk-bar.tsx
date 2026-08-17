"use client";

import { Button } from "@/components/ui/button";
import { RELEASE_TARGETS } from "@/feature/product/constants";
import type { ProductReleaseTarget } from "@/feature/product/domain";
import { PRODUCT_AVAILABILITY_COPY } from "@/feature/product/message";

interface SelectionState {
  hasOnSale: boolean;
  hasSoldOut: boolean;
  hasHidden: boolean;
}

interface AvailabilityBulkBarProps {
  selectedCount: number;
  selectionState: SelectionState;
  disabled?: boolean;
  onSoldOut: () => void;
  onHide: () => void;
  onRelease: (target: ProductReleaseTarget) => void;
  onChangePeriod: () => void;
}

/**
 * 하단 sticky 일괄 조작 바.
 *
 * 버튼 구성은 **선택된 항목의 현재 상태**에 따라 바뀐다(원문 PDF 3페이지).
 *
 * | 선택 상태 | 노출 버튼 |
 * |---|---|
 * | 전부 판매중 | 숨김 · 품절 |
 * | 전부 품절 | 품절 해제 |
 * | 전부 숨김 | 숨김 해제 |
 * | 품절·숨김 혼재 | 품절·숨김 해제 |
 * | 판매중 + 품절/숨김 혼재 | 숨김 · 품절 · 품절·숨김 해제 |
 *
 * 선택 0건이면 숨김/품절을 `disabled` 로 둔다.
 *
 * **표에 없는 `기간변경` 을 하나 더 둔다.** `docs/tasks/frontend.md` 는 위 표(379-385행)와
 * "여러 항목을 선택한 상태에서도 열 수 있다(일괄 기간 변경)"(434행)를 동시에 요구하는데,
 * 일괄 기간변경으로 들어갈 문이 하단 바 말고는 없다 — 행의 [기간변경] 버튼은 단건 전용이다.
 * 표만 따르면 434행을 구현할 수 없으므로 품절 대상이 선택됐을 때만 이 버튼을 노출한다.
 * 기획이 표를 확정 스펙으로 본다면 이 버튼을 빼고 434행을 함께 걷어내야 한다.
 */
export function AvailabilityBulkBar({
  selectedCount,
  selectionState,
  disabled,
  onSoldOut,
  onHide,
  onRelease,
  onChangePeriod,
}: AvailabilityBulkBarProps) {
  const { hasOnSale, hasSoldOut, hasHidden } = selectionState;
  const isEmpty = selectedCount === 0;

  // 선택이 없을 때도 바는 남긴다 — 원문 스크린샷이 `선택한 메뉴 0개를` + 비활성 버튼 상태를 보여준다.
  const showSoldOutAndHide = isEmpty || hasOnSale;
  const showReleaseAll = hasSoldOut && hasHidden;
  const showReleaseSoldOut = hasSoldOut && !hasHidden;
  const showReleaseHidden = hasHidden && !hasSoldOut;

  return (
    <div className="bg-background/95 sticky bottom-0 z-10 -mx-6 flex flex-wrap items-center justify-between gap-3 border-t px-6 py-3 backdrop-blur">
      <span className="text-sm font-medium">
        {PRODUCT_AVAILABILITY_COPY.BULK_PREFIX} {selectedCount}
        {PRODUCT_AVAILABILITY_COPY.BULK_SUFFIX}
      </span>

      <div className="flex flex-wrap items-center gap-2">
        {showSoldOutAndHide && (
          <>
            <Button type="button" variant="outline" disabled={disabled === true || isEmpty} onClick={onHide}>
              {PRODUCT_AVAILABILITY_COPY.BUTTON_HIDE}
            </Button>
            <Button type="button" variant="outline" disabled={disabled === true || isEmpty} onClick={onSoldOut}>
              {PRODUCT_AVAILABILITY_COPY.BUTTON_SOLD_OUT}
            </Button>
          </>
        )}

        {showReleaseSoldOut && (
          <Button
            type="button"
            variant="outline"
            disabled={disabled}
            onClick={() => onRelease(RELEASE_TARGETS.SOLD_OUT)}
          >
            {PRODUCT_AVAILABILITY_COPY.BUTTON_RELEASE_SOLD_OUT}
          </Button>
        )}

        {showReleaseHidden && (
          <Button type="button" variant="outline" disabled={disabled} onClick={() => onRelease(RELEASE_TARGETS.HIDDEN)}>
            {PRODUCT_AVAILABILITY_COPY.BUTTON_RELEASE_HIDDEN}
          </Button>
        )}

        {showReleaseAll && (
          <Button type="button" variant="outline" disabled={disabled} onClick={() => onRelease(RELEASE_TARGETS.ALL)}>
            {PRODUCT_AVAILABILITY_COPY.BUTTON_RELEASE_ALL}
          </Button>
        )}

        {/* 기간변경은 품절 상태의 항목이 선택됐을 때만 의미가 있다 — 서버도 판매중 항목은
            `PRODUCT_NOT_SOLD_OUT` 으로 실패 처리한다. */}
        {hasSoldOut && (
          <Button type="button" disabled={disabled} onClick={onChangePeriod}>
            {PRODUCT_AVAILABILITY_COPY.BUTTON_CHANGE_PERIOD}
          </Button>
        )}
      </div>
    </div>
  );
}
