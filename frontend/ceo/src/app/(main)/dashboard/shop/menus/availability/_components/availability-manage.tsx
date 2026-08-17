"use client";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { Lock, Store } from "lucide-react";
import { toast } from "sonner";

import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Empty, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty";
import { AVAILABILITY_TABS, RELEASE_TARGETS } from "@/feature/product/constants";
import type {
  AvailabilityChangeOutcome,
  AvailabilityMenuGroup,
  AvailabilityOptionGroup,
  AvailabilityTab,
  OptionSelection,
  ProductReleaseTarget,
} from "@/feature/product/domain";
import { PRODUCT_AVAILABILITY_COPY, PRODUCT_MESSAGE } from "@/feature/product/message";
import type { ShopSummary } from "@/feature/shop/domain";

import { ShopSelector } from "../../../_components/shop-selector";
import { AvailabilityBulkBar } from "./availability-bulk-bar";
import { AvailabilityFailureNotice } from "./availability-failure-notice";
import { AvailabilityFilterBar } from "./availability-filter-bar";
import { AvailabilityMenuList } from "./availability-menu-list";
import { AvailabilityOptionList } from "./availability-option-list";
import { SoldOutPeriodDialog } from "./sold-out-period-dialog";
import { optionSelectionKey, useAvailabilityMutation } from "./use-availability-mutation";

export interface AvailabilityFilters {
  keyword?: string;
  soldOutOnly?: boolean;
  hiddenOnly?: boolean;
  tab: AvailabilityTab;
}

interface AvailabilityManageProps {
  shops: ShopSummary[];
  shopId?: number;
  filters: AvailabilityFilters;
  /** 메뉴 탭 데이터. 조회 실패 시 undefined 로 넘어와 필터바만 살린다 */
  menuGroups?: AvailabilityMenuGroup[];
  optionGroups?: AvailabilityOptionGroup[];
  /** 접근 불가 사유(403 `SHOP_ACCESS_DENIED` / 404 `SHOP_NOT_FOUND`) */
  errorCode?: string;
  errorMessage?: string;
}

/**
 * 기간 Dialog 를 어떤 대상에 대해 열었는지.
 *
 * `bulk` 는 현재 선택된 항목 전체, `menu`/`option` 은 행의 [기간변경] 버튼으로 연 단건이다 —
 * 단건은 선택 상태를 건드리지 않고 그 항목만 대상으로 삼는다.
 */
type PeriodDialogTarget =
  | { scope: "bulk" }
  | { scope: "menu"; productId: number }
  | { scope: "option"; option: OptionSelection };

export function AvailabilityManage({
  shops,
  shopId,
  filters,
  menuGroups,
  optionGroups,
  errorCode,
  errorMessage,
}: AvailabilityManageProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isNavigating, startNavigation] = React.useTransition();

  const [selectedProductIds, setSelectedProductIds] = React.useState<ReadonlySet<number>>(() => new Set());
  // 일반 옵션과 공통 옵션은 다른 테이블이라 id 가 겹친다 — `optionType:optionId` 를 키로 쓴다.
  const [selectedOptions, setSelectedOptions] = React.useState<ReadonlyMap<string, OptionSelection>>(() => new Map());
  const [failures, setFailures] = React.useState<AvailabilityChangeOutcome["failed"]>([]);
  const [periodDialogTarget, setPeriodDialogTarget] = React.useState<PeriodDialogTarget | null>(null);

  const isMenuTab = filters.tab === AVAILABILITY_TABS.MENU;
  // 빈 목록 문구를 "등록된 메뉴가 없습니다"와 "조건에 맞는 항목이 없습니다"로 가른다.
  const isFiltered = filters.keyword !== undefined || filters.soldOutOnly === true || filters.hiddenOnly === true;

  const accessDeniedMessage =
    errorCode === "SHOP_ACCESS_DENIED"
      ? PRODUCT_MESSAGE.SHOP_ACCESS_DENIED
      : errorCode === "SHOP_NOT_FOUND"
        ? PRODUCT_MESSAGE.SHOP_NOT_FOUND
        : undefined;

  /**
   * 선택 해제.
   *
   * **`keepFailures` 는 부분실패에서 쓴다.** 일부라도 반영됐으면 선택은 비워야 하지만
   * (이미 처리된 항목을 다시 누르지 않게), 실패 안내는 화면에 남아야 한다 —
   * 부분실패는 HTTP 200 이라 안내가 사라지면 점주가 전건 성공으로 오인한다.
   */
  const clearSelection = React.useCallback((options?: { keepFailures?: boolean }) => {
    setSelectedProductIds(new Set());
    setSelectedOptions(new Map());
    if (options?.keepFailures !== true) setFailures([]);
  }, []);

  /**
   * 탭·필터·검색은 URL searchParams 로 관리한다 — 서버가 필터링하므로 클라이언트 상태로 두면
   * 매 변경마다 수동 재조회가 필요하다.
   *
   * **이동할 때 선택을 비운다.** 화면에 보이지 않는 항목이 선택된 채로 일괄 처리되면
   * 점주가 의도하지 않은 메뉴가 품절된다.
   */
  const pushParams = React.useCallback(
    (next: Record<string, string | undefined>) => {
      const params = new URLSearchParams(searchParams.toString());
      for (const [key, value] of Object.entries(next)) {
        if (value === undefined) params.delete(key);
        else params.set(key, value);
      }
      clearSelection();
      startNavigation(() => {
        router.push(`?${params.toString()}`);
      });
    },
    [clearSelection, router, searchParams],
  );

  const { isMutating, runMenuMutation, runOptionMutation } = useAvailabilityMutation({
    onOutcome: (outcome) => setFailures(outcome.failed),
    onCleared: clearSelection,
  });

  /**
   * 선택 목록은 **화면에 실제로 보이는 행으로 좁혀서** 쓴다.
   *
   * revalidation 으로 목록이 갱신되면 삭제되거나 다른 탭에서 상태가 바뀐 행의 id 가 선택에 남는데,
   * 그대로 두면 `selectedCount` 는 0 이 아닌데 아래 `selectionState` 는 그 행을 찾지 못해
   * 하단 바의 개수와 버튼 구성이 어긋난다. 보이지 않는 항목을 일괄 처리 대상에 싣지 않는
   * 이 화면의 원칙과도 같은 이유다.
   */
  const visibleMenuRows = React.useMemo(
    () => (menuGroups ?? []).flatMap((group) => group.products).filter((row) => selectedProductIds.has(row.id)),
    [menuGroups, selectedProductIds],
  );
  const visibleOptionRows = React.useMemo(
    () =>
      (optionGroups ?? [])
        .flatMap((group) => group.options)
        .filter((row) => selectedOptions.has(optionSelectionKey(row))),
    [optionGroups, selectedOptions],
  );

  const selectedProductIdList = React.useMemo(() => visibleMenuRows.map((row) => row.id), [visibleMenuRows]);
  const selectedOptionList = React.useMemo(
    () => visibleOptionRows.map((row) => ({ optionId: row.id, optionType: row.optionType })),
    [visibleOptionRows],
  );
  const selectedCount = isMenuTab ? selectedProductIdList.length : selectedOptionList.length;

  /**
   * 선택된 행들의 현재 상태 — 하단 바의 버튼 구성을 결정한다.
   *
   * 품절 중인 대상만 따로 뽑아 두는 이유는 **일괄 기간변경 때문**이다. 선택에 판매중 항목이
   * 섞인 채로 보내면 서버가 그것들을 `PRODUCT_NOT_SOLD_OUT` 으로 실패 처리해(`backend.md` §3-3),
   * 점주가 직접 고른 판매중 메뉴가 전부 빨간 실패 목록으로 나열된다.
   */
  const selectionState = React.useMemo(() => {
    const rows = isMenuTab ? visibleMenuRows : visibleOptionRows;

    return {
      hasOnSale: rows.some((row) => !row.soldOut && row.visible),
      hasSoldOut: rows.some((row) => row.soldOut),
      hasHidden: rows.some((row) => !row.visible),
      soldOutProductIds: visibleMenuRows.filter((row) => row.soldOut).map((row) => row.id),
      soldOutOptions: visibleOptionRows
        .filter((row) => row.soldOut)
        .map((row) => ({ optionId: row.id, optionType: row.optionType })),
    };
  }, [isMenuTab, visibleMenuRows, visibleOptionRows]);

  function handleSoldOut() {
    // soldOutUntil 을 보내지 않는다 — 서버가 영업시간 기반 다음 오픈 시각으로 채운다.
    if (isMenuTab) runMenuMutation("soldOut", { shopId, productIds: selectedProductIdList });
    else runOptionMutation("soldOut", { shopId, options: selectedOptionList });
  }

  function handleHide() {
    if (isMenuTab) runMenuMutation("hide", { shopId, productIds: selectedProductIdList });
    else runOptionMutation("hide", { shopId, options: selectedOptionList });
  }

  function handleRelease(target: ProductReleaseTarget) {
    if (isMenuTab) runMenuMutation("release", { shopId, productIds: selectedProductIdList, target });
    else runOptionMutation("release", { shopId, options: selectedOptionList, target });
  }

  /** 행 우측 개별 해제 — 선택 상태와 무관하게 그 행만 대상으로 한다 */
  function handleReleaseRow(
    row: { soldOut: boolean; visible: boolean },
    ids: { productId?: number; option?: OptionSelection },
  ) {
    // 마지막 갈래를 HIDDEN 이 아니라 ALL 로 둔다 — 판매중 행이 들어와도(현재는 버튼이 가려져
    // 도달 불가하지만) 해제는 멱등이라 실패하지 않는다(`backend.md` §3-3).
    const target: ProductReleaseTarget = row.soldOut && row.visible ? RELEASE_TARGETS.SOLD_OUT : RELEASE_TARGETS.ALL;

    if (ids.productId !== undefined) runMenuMutation("release", { shopId, productIds: [ids.productId], target });
    else if (ids.option) runOptionMutation("release", { shopId, options: [ids.option], target });
  }

  function handleApplyPeriod(soldOutUntil: string) {
    const target = periodDialogTarget;
    if (!target) return;

    if (target.scope === "menu") {
      runMenuMutation("changeSoldOutUntil", { shopId, productIds: [target.productId], soldOutUntil });
    } else if (target.scope === "option") {
      runOptionMutation("changeSoldOutUntil", { shopId, options: [target.option], soldOutUntil });
    } else if (isMenuTab) {
      // 일괄은 선택 중 **품절 상태인 것만** 보낸다 — 위 selectionState 주석 참고.
      runMenuMutation("changeSoldOutUntil", {
        shopId,
        productIds: selectionState.soldOutProductIds,
        soldOutUntil,
      });
    } else {
      runOptionMutation("changeSoldOutUntil", { shopId, options: selectionState.soldOutOptions, soldOutUntil });
    }

    setPeriodDialogTarget(null);
  }

  const isBusy = isNavigating || isMutating;

  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{PRODUCT_AVAILABILITY_COPY.PAGE_TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">
          {PRODUCT_AVAILABILITY_COPY.PAGE_DESCRIPTION}
        </CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          {accessDeniedMessage === undefined && shopId !== undefined && (
            <ShopSelector
              shops={shops}
              shopId={shopId}
              disabled={isBusy}
              onChange={(nextShopId) => pushParams({ shopId: String(nextShopId) })}
            />
          )}
        </CardAction>
      </CardHeader>

      <CardContent className="flex flex-col gap-6 pb-24">
        {accessDeniedMessage !== undefined ? (
          <Empty>
            <EmptyHeader>
              <EmptyMedia variant="icon">
                <Lock />
              </EmptyMedia>
              <EmptyTitle>{accessDeniedMessage}</EmptyTitle>
            </EmptyHeader>
          </Empty>
        ) : shops.length === 0 || shopId === undefined ? (
          <Empty>
            <EmptyHeader>
              <EmptyMedia variant="icon">
                <Store />
              </EmptyMedia>
              <EmptyTitle>{PRODUCT_MESSAGE.SHOP_EMPTY}</EmptyTitle>
            </EmptyHeader>
          </Empty>
        ) : (
          <>
            <AvailabilityFilterBar filters={filters} disabled={isBusy} onChange={(next) => pushParams(next)} />

            {failures.length > 0 && <AvailabilityFailureNotice failures={failures} onDismiss={() => setFailures([])} />}

            {isMenuTab ? (
              <AvailabilityMenuList
                groups={menuGroups}
                errorMessage={errorMessage}
                filtered={isFiltered}
                selectedIds={selectedProductIds}
                disabled={isBusy}
                onSelectionChange={setSelectedProductIds}
                onReleaseRow={(row) => handleReleaseRow(row, { productId: row.id })}
                onChangePeriod={(productId) => setPeriodDialogTarget({ scope: "menu", productId })}
              />
            ) : (
              <AvailabilityOptionList
                groups={optionGroups}
                errorMessage={errorMessage}
                filtered={isFiltered}
                selected={selectedOptions}
                disabled={isBusy}
                onSelectionChange={setSelectedOptions}
                onReleaseRow={(row) =>
                  handleReleaseRow(row, { option: { optionId: row.id, optionType: row.optionType } })
                }
                onChangePeriod={(row) =>
                  setPeriodDialogTarget({
                    scope: "option",
                    option: { optionId: row.id, optionType: row.optionType },
                  })
                }
              />
            )}

            <AvailabilityBulkBar
              selectedCount={selectedCount}
              selectionState={selectionState}
              disabled={isBusy}
              onSoldOut={handleSoldOut}
              onHide={handleHide}
              onRelease={handleRelease}
              onChangePeriod={() => {
                if (selectedCount === 0) {
                  toast.error(PRODUCT_MESSAGE.TARGET_REQUIRED);
                  return;
                }
                setPeriodDialogTarget({ scope: "bulk" });
              }}
            />

            <SoldOutPeriodDialog
              open={periodDialogTarget !== null}
              pending={isMutating}
              onOpenChange={(open) => {
                if (!open) setPeriodDialogTarget(null);
              }}
              onApply={handleApplyPeriod}
            />
          </>
        )}
      </CardContent>
    </Card>
  );
}
