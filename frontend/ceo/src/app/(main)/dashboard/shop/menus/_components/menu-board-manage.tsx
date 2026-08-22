"use client";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { closestCenter, DndContext } from "@dnd-kit/core";
import { restrictToVerticalAxis } from "@dnd-kit/modifiers";
import { Lock, Store } from "lucide-react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Empty, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty";
import {
  createMenuAction,
  createMenuCategoryAction,
  deleteMenuCategoryAction,
  deleteMenusAction,
  updateMenuCategoryAction,
} from "@/feature/product/actions";
import { MENU_TABS } from "@/feature/product/constants";
import type { AvailabilityChangeOutcome, MenuBoardGroup, MenuCategory } from "@/feature/product/domain";
import { PRODUCT_MENU_COPY, PRODUCT_MENU_MESSAGE, PRODUCT_MESSAGE } from "@/feature/product/message";
import type { MenuCategoryFormValues } from "@/feature/product/schema";
import type { ShopSummary } from "@/feature/shop/domain";

import { ShopSelector } from "../../_components/shop-selector";
import { MenuBoardFailureNotice } from "./menu-board-failure-notice";
import { MenuCreateDialog, type MenuCreateSubmitValues } from "./menu-create-dialog";
import { MenuDeleteDialog, type MenuDeleteTarget } from "./menu-delete-dialog";
import { MenuGroupFormDialog, type MenuGroupFormTarget } from "./menu-group-form-dialog";
import { MenuGroupList } from "./menu-group-list";
import { MenuTabBar } from "./menu-tab-bar";
import { useMenuSort } from "./use-menu-sort";

interface MenuBoardManageProps {
  shops: ShopSummary[];
  shopId?: number;
  /** 메뉴그룹 목록. 메뉴가 없는 빈 그룹도 화면에 남기기 위해 목록 응답과 별도로 받는다 */
  categories?: MenuCategory[];
  /** 메뉴판 목록. 조회 실패 시 undefined 로 넘어와 셸만 살린다 */
  groups?: MenuBoardGroup[];
  /** 접근 불가 사유(403 `SHOP_ACCESS_DENIED` / 404 `SHOP_NOT_FOUND`) */
  errorCode?: string;
  errorMessage?: string;
}

/** 메뉴그룹 폼 다이얼로그의 열림 상태. `target: null` 이 추가 모드라 별도 플래그가 필요하다 */
type GroupFormState = { open: false } | { open: true; target: MenuGroupFormTarget | null };

export function MenuBoardManage({ shops, shopId, categories, groups, errorCode, errorMessage }: MenuBoardManageProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isNavigating, startNavigation] = React.useTransition();
  const [isMutating, startMutation] = React.useTransition();

  const [selectedProductIds, setSelectedProductIds] = React.useState<ReadonlySet<number>>(() => new Set());
  const [failures, setFailures] = React.useState<AvailabilityChangeOutcome["failed"]>([]);
  const [isCreateOpen, setCreateOpen] = React.useState(false);
  const [groupForm, setGroupForm] = React.useState<GroupFormState>({ open: false });
  const [deleteTarget, setDeleteTarget] = React.useState<MenuDeleteTarget | null>(null);

  const accessDeniedMessage =
    errorCode === "SHOP_ACCESS_DENIED"
      ? PRODUCT_MESSAGE.SHOP_ACCESS_DENIED
      : errorCode === "SHOP_NOT_FOUND"
        ? PRODUCT_MESSAGE.SHOP_NOT_FOUND
        : undefined;

  /**
   * 선택 해제.
   *
   * **`keepFailures` 는 부분실패에서 쓴다.** 일부라도 삭제됐으면 선택은 비워야 하지만
   * (이미 사라진 항목을 다시 누르지 않게), 실패 안내는 화면에 남아야 한다 —
   * 부분실패는 HTTP 200 이라 안내가 사라지면 점주가 전건 성공으로 오인한다.
   */
  const clearSelection = React.useCallback((options?: { keepFailures?: boolean }) => {
    setSelectedProductIds(new Set());
    if (options?.keepFailures !== true) setFailures([]);
  }, []);

  /**
   * 가게 전환은 URL searchParams 로 한다.
   *
   * **이동할 때 선택을 비운다.** 화면에 보이지 않는 항목이 선택된 채로 일괄 삭제되면
   * 점주가 의도하지 않은 메뉴가 사라진다.
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

  /**
   * 화면에 그릴 메뉴판.
   *
   * 메뉴 목록 응답은 **메뉴를 품은 그룹만** 내려주므로, 방금 만든 빈 메뉴그룹이 사라져 보인다.
   * 그룹 목록을 기준으로 잡고 메뉴를 채워 넣어 빈 그룹도 드롭 대상으로 남긴다.
   * 미분류(`categoryId === null`)는 카테고리 목록에 없으므로 메뉴가 있을 때만 맨 뒤에 붙인다.
   */
  const mergedGroups = React.useMemo(() => {
    if (groups === undefined) return undefined;

    const productsByCategoryId = new Map(groups.map((group) => [group.categoryId, group]));

    const categorized: MenuBoardGroup[] = (categories ?? []).map((category) => {
      const matched = productsByCategoryId.get(category.id);
      return {
        categoryId: category.id,
        categoryName: category.name,
        sort: category.sort,
        products: matched?.products ?? [],
      };
    });

    const uncategorized = productsByCategoryId.get(null);
    return uncategorized === undefined || uncategorized.products.length === 0
      ? categorized
      : [...categorized, uncategorized];
  }, [categories, groups]);

  const { sensors, displayGroups, isSorting, handleDragStart, handleDragEnd, handleDragCancel } = useMenuSort({
    shopId,
    groups: mergedGroups ?? [],
  });

  /**
   * 선택 목록은 **화면에 실제로 보이는 행으로 좁혀서** 쓴다.
   *
   * revalidation 으로 목록이 갱신되면 이미 삭제된 행의 id 가 선택에 남는데, 그대로 두면
   * 하단 바의 개수가 실제 대상과 어긋난다.
   */
  const selectedProductIdList = React.useMemo(
    () =>
      displayGroups
        .flatMap((group) => group.products)
        .filter((row) => selectedProductIds.has(row.id))
        .map((row) => row.id),
    [displayGroups, selectedProductIds],
  );

  const isBusy = isNavigating || isMutating || isSorting;

  function handleCreateMenu(values: MenuCreateSubmitValues) {
    if (shopId === undefined) return;

    startMutation(async () => {
      const result = await createMenuAction({ shopId, ...values });
      // 중복 메뉴명·금칙어·특수문자는 서버가 내려준 한국어 문구를 그대로 노출한다 —
      // 프론트에서 errorCode → 문구 맵을 다시 만들지 않는 것이 이 앱의 관례다.
      if (!result.success) {
        toast.error(result.message ?? PRODUCT_MENU_MESSAGE.MENU_CREATE_FAILED);
        return;
      }

      toast.success(PRODUCT_MENU_MESSAGE.MENU_CREATE_SUCCESS);
      setCreateOpen(false);
    });
  }

  function handleSubmitGroup(values: MenuCategoryFormValues) {
    if (shopId === undefined || !groupForm.open) return;

    const target = groupForm.target;

    startMutation(async () => {
      const result =
        target === null
          ? await createMenuCategoryAction(shopId, values.name, values.description)
          : await updateMenuCategoryAction(target.categoryId, shopId, values.name, values.description);

      if (!result.success) {
        toast.error(
          result.message ??
            (target === null
              ? PRODUCT_MENU_MESSAGE.CATEGORY_CREATE_FAILED
              : PRODUCT_MENU_MESSAGE.CATEGORY_UPDATE_FAILED),
        );
        return;
      }

      toast.success(
        target === null ? PRODUCT_MENU_MESSAGE.CATEGORY_CREATE_SUCCESS : PRODUCT_MENU_MESSAGE.CATEGORY_UPDATE_SUCCESS,
      );
      setGroupForm({ open: false });
    });
  }

  /**
   * 삭제 확정.
   *
   * 메뉴 일괄 삭제는 **HTTP 200 + 부분 실패**라 세 갈래로 나눈다
   * (`use-availability-mutation.ts` 가 세운 패턴).
   */
  function handleConfirmDelete() {
    if (shopId === undefined || deleteTarget === null) return;

    if (deleteTarget.scope === "group") {
      const categoryId = deleteTarget.categoryId;
      startMutation(async () => {
        const result = await deleteMenuCategoryAction(categoryId, shopId);
        if (!result.success) {
          // 소속 메뉴가 있으면 `PRODUCT_CATEGORY_HAS_PRODUCTS` — 서버 문구를 그대로 쓴다.
          toast.error(result.message ?? PRODUCT_MENU_MESSAGE.CATEGORY_DELETE_FAILED);
          return;
        }

        toast.success(PRODUCT_MENU_MESSAGE.CATEGORY_DELETE_SUCCESS);
        setDeleteTarget(null);
      });
      return;
    }

    const productIds = selectedProductIdList;
    startMutation(async () => {
      const result = await deleteMenusAction(shopId, productIds);

      // 요청 전체 거부(4xx)는 서버가 내려준 문구를 그대로 노출한다.
      if (!result.success || !result.data) {
        toast.error(result.message ?? PRODUCT_MENU_MESSAGE.MENU_DELETE_FAILED);
        return;
      }

      const outcome = result.data;
      setFailures(outcome.failed);
      setDeleteTarget(null);

      const succeeded = outcome.succeededIds.length;
      const failed = outcome.failed.length;

      if (failed === 0) {
        toast.success(PRODUCT_MENU_MESSAGE.MENU_DELETE_SUCCESS);
        clearSelection();
        return;
      }

      if (succeeded > 0) {
        toast.warning(PRODUCT_MESSAGE.PARTIAL_FAILURE_SUMMARY(succeeded, failed), {
          description: PRODUCT_MESSAGE.PARTIAL_FAILURE,
        });
        // 일부라도 삭제됐으면 선택을 비우되 **실패 목록은 남긴다** — 인자 없이 부르면 방금
        // 세팅한 실패 안내까지 지워져, 일부만 지워진 결과가 전건 성공처럼 보인다.
        clearSelection({ keepFailures: true });
        return;
      }

      // 전건 실패는 선택을 남긴다 — 조건을 고쳐 그대로 재시도할 수 있게 한다.
      toast.error(PRODUCT_MENU_MESSAGE.MENU_DELETE_FAILED);
    });
  }

  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{PRODUCT_MENU_COPY.PAGE_TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{PRODUCT_MENU_COPY.PAGE_DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          {accessDeniedMessage === undefined && shopId !== undefined && (
            <>
              <ShopSelector
                shops={shops}
                shopId={shopId}
                disabled={isBusy}
                onChange={(nextShopId) => pushParams({ shopId: String(nextShopId) })}
              />
              <Button type="button" variant="outline" disabled={isBusy} onClick={() => setCreateOpen(true)}>
                {PRODUCT_MENU_COPY.BUTTON_ADD_MENU}
              </Button>
              <Button
                type="button"
                variant="outline"
                disabled={isBusy}
                onClick={() => setGroupForm({ open: true, target: null })}
              >
                {PRODUCT_MENU_COPY.BUTTON_ADD_GROUP}
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={() => router.push(`/dashboard/shop/menus/option-groups?shopId=${shopId}`)}
              >
                {PRODUCT_MENU_COPY.BUTTON_MANAGE_OPTION_GROUPS}
              </Button>
            </>
          )}
        </CardAction>
      </CardHeader>

      <CardContent className="flex flex-col gap-6 pb-24">
        {/* 탭은 접근 불가·가게 없음 상태에서도 보여준다 — 옵션 탭으로 빠져나갈 길을 막지 않는다. */}
        <MenuTabBar activeTab={MENU_TABS.MENU} shopId={shopId} disabled={isBusy} />

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
            {failures.length > 0 && <MenuBoardFailureNotice failures={failures} onDismiss={() => setFailures([])} />}

            {/* 세로 목록이라 가로 이동은 의미가 없다 — `restrictToVerticalAxis` 로 묶어
                드래그 중 손이 옆으로 흔들려도 놓을 자리가 흔들리지 않게 한다. */}
            <DndContext
              id="menu-board-manage"
              sensors={sensors}
              collisionDetection={closestCenter}
              modifiers={[restrictToVerticalAxis]}
              onDragStart={handleDragStart}
              onDragEnd={handleDragEnd}
              onDragCancel={handleDragCancel}
            >
              <MenuGroupList
                groups={mergedGroups === undefined ? undefined : displayGroups}
                errorMessage={errorMessage}
                selectedIds={selectedProductIds}
                disabled={isBusy}
                onSelectionChange={setSelectedProductIds}
                onEditGroup={(group) => {
                  // 미분류는 수정 대상이 아니므로 목록이 버튼을 내주지 않는다 — 타입만 좁힌다.
                  if (group.categoryId === null) return;
                  setGroupForm({
                    open: true,
                    target: {
                      categoryId: group.categoryId,
                      name: group.categoryName ?? "",
                      description: "",
                    },
                  });
                }}
                onDeleteGroup={(group) => {
                  if (group.categoryId === null) return;
                  setDeleteTarget({
                    scope: "group",
                    categoryId: group.categoryId,
                    name: group.categoryName ?? "",
                  });
                }}
                onOpenDetail={(productId) => router.push(`/dashboard/shop/menus/${productId}?shopId=${shopId}`)}
              />
            </DndContext>

            {/* 하단 sticky 삭제 바. 선택이 없어도 남겨 개수와 비활성 상태를 함께 보여준다. */}
            <div className="bg-background/95 sticky bottom-0 z-10 -mx-6 flex flex-wrap items-center justify-between gap-3 border-t px-6 py-3 backdrop-blur">
              <span className="text-sm font-medium">
                {PRODUCT_MENU_COPY.BULK_PREFIX} {selectedProductIdList.length}
                {PRODUCT_MENU_COPY.BULK_SUFFIX}
              </span>
              <Button
                type="button"
                variant="destructive"
                disabled={isBusy || selectedProductIdList.length === 0}
                onClick={() => setDeleteTarget({ scope: "menus", count: selectedProductIdList.length })}
              >
                {PRODUCT_MENU_COPY.BUTTON_DELETE}
              </Button>
            </div>

            <MenuCreateDialog
              open={isCreateOpen}
              pending={isMutating}
              categories={categories ?? []}
              onOpenChange={setCreateOpen}
              onSubmit={handleCreateMenu}
            />

            <MenuGroupFormDialog
              open={groupForm.open}
              pending={isMutating}
              target={groupForm.open ? groupForm.target : null}
              onOpenChange={(open) => {
                if (!open) setGroupForm({ open: false });
              }}
              onSubmit={handleSubmitGroup}
            />

            <MenuDeleteDialog
              target={deleteTarget}
              pending={isMutating}
              onOpenChange={(open) => {
                if (!open) setDeleteTarget(null);
              }}
              onConfirm={handleConfirmDelete}
            />
          </>
        )}
      </CardContent>
    </Card>
  );
}
