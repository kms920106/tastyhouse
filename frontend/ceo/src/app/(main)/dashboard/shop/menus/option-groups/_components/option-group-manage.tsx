"use client";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { Lock, Store } from "lucide-react";
import { toast } from "sonner";

import { Accordion } from "@/components/ui/accordion";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty";
import {
  changeOptionOrderAction,
  createOptionAction,
  createOptionGroupAction,
  deleteOptionAction,
  deleteOptionGroupAction,
  updateOptionAction,
  updateOptionGroupAction,
} from "@/feature/product/actions";
import type { MenuOption, MenuOptionGroup } from "@/feature/product/domain";
import {
  OPTION_GROUP_SCREEN_COPY,
  PRODUCT_MENU_MESSAGE,
  PRODUCT_MESSAGE,
  PRODUCT_OPTION_GROUP_COPY,
} from "@/feature/product/message";
import type { ShopSummary } from "@/feature/shop/domain";

import { ShopSelector } from "../../../_components/shop-selector";
import { OptionFormDialog } from "./option-form-dialog";
import { OptionGroupAccordion } from "./option-group-accordion";
import { OptionGroupFormDialog, type OptionGroupSubmitValues } from "./option-group-form-dialog";

interface OptionGroupManageProps {
  shops: ShopSummary[];
  shopId?: number;
  /** 조회 실패 시 undefined 로 넘어와 셸(가게 선택기·추가 버튼)만 살린다 */
  optionGroups?: MenuOptionGroup[];
  /** 접근 불가 사유(403 `SHOP_ACCESS_DENIED` / 404 `SHOP_NOT_FOUND`) */
  errorCode?: string;
  errorMessage?: string;
}

/** 그룹 폼을 어떤 대상에 대해 열었는지. `create` 는 신규, 그 외는 수정 대상 그룹 */
type GroupDialogTarget = { mode: "create" } | { mode: "edit"; group: MenuOptionGroup };

/** 옵션 폼 대상. 추가는 소속 그룹만, 수정은 옵션까지 안다 */
type OptionDialogTarget =
  | { mode: "create"; optionGroupId: number }
  | { mode: "edit"; optionGroupId: number; option: MenuOption };

/** 삭제 확인 대상 — 그룹과 옵션이 안내 문구와 액션이 다르다 */
type DeleteTarget = { kind: "group"; group: MenuOptionGroup } | { kind: "option"; option: MenuOption };

export function OptionGroupManage({ shops, shopId, optionGroups, errorCode, errorMessage }: OptionGroupManageProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isNavigating, startNavigation] = React.useTransition();
  const [isMutating, startMutation] = React.useTransition();

  const [groupDialogTarget, setGroupDialogTarget] = React.useState<GroupDialogTarget | null>(null);
  const [optionDialogTarget, setOptionDialogTarget] = React.useState<OptionDialogTarget | null>(null);
  const [deleteTarget, setDeleteTarget] = React.useState<DeleteTarget | null>(null);

  const accessDeniedMessage =
    errorCode === "SHOP_ACCESS_DENIED"
      ? PRODUCT_MESSAGE.SHOP_ACCESS_DENIED
      : errorCode === "SHOP_NOT_FOUND"
        ? PRODUCT_MESSAGE.SHOP_NOT_FOUND
        : undefined;

  const isBusy = isNavigating || isMutating;

  /** 가게 전환은 URL searchParams 로 한다 — 서버가 가게별로 조회하므로 재조회가 자동으로 따라온다. */
  function handleShopChange(nextShopId: number) {
    const params = new URLSearchParams(searchParams.toString());
    params.set("shopId", String(nextShopId));

    startNavigation(() => {
      router.push(`?${params.toString()}`);
    });
  }

  /**
   * 액션 결과 공통 처리.
   *
   * 실패 문구는 서버가 내려준 한국어 `message` 를 그대로 쓴다 — 예컨대 옵션 삭제의
   * `PRODUCT_OPTION_MIN_SELECT_VIOLATION` 은 남은 옵션 수까지 담아 오므로, 프론트에서
   * errorCode → 문구 맵을 다시 만들면 그 구체성이 사라진다(이 앱의 확립된 관례).
   */
  function runMutation(
    action: () => Promise<{ success: boolean; message?: string }>,
    copy: { success: string; failure: string },
    onSuccess?: () => void,
  ) {
    startMutation(async () => {
      const result = await action();

      if (!result.success) {
        toast.error(result.message ?? copy.failure);
        return;
      }

      toast.success(copy.success);
      onSuccess?.();
    });
  }

  function handleSubmitGroup(values: OptionGroupSubmitValues) {
    const target = groupDialogTarget;
    if (!target || shopId === undefined) return;

    const input = { shopId, ...values };

    if (target.mode === "create") {
      runMutation(
        () => createOptionGroupAction(input),
        {
          success: PRODUCT_MENU_MESSAGE.OPTION_GROUP_CREATE_SUCCESS,
          failure: PRODUCT_MENU_MESSAGE.OPTION_GROUP_CREATE_FAILED,
        },
        () => setGroupDialogTarget(null),
      );
    } else {
      runMutation(
        () => updateOptionGroupAction(target.group.id, input),
        {
          success: PRODUCT_MENU_MESSAGE.OPTION_GROUP_UPDATE_SUCCESS,
          failure: PRODUCT_MENU_MESSAGE.OPTION_GROUP_UPDATE_FAILED,
        },
        () => setGroupDialogTarget(null),
      );
    }
  }

  function handleSubmitOption(values: { name: string; additionalPrice: number }) {
    const target = optionDialogTarget;
    if (!target || shopId === undefined) return;

    if (target.mode === "create") {
      runMutation(
        () => createOptionAction(target.optionGroupId, shopId, values.name, values.additionalPrice),
        { success: PRODUCT_MENU_MESSAGE.OPTION_CREATE_SUCCESS, failure: PRODUCT_MENU_MESSAGE.OPTION_CREATE_FAILED },
        () => setOptionDialogTarget(null),
      );
    } else {
      runMutation(
        () => updateOptionAction(target.option.id, shopId, values.name, values.additionalPrice),
        { success: PRODUCT_MENU_MESSAGE.OPTION_UPDATE_SUCCESS, failure: PRODUCT_MENU_MESSAGE.OPTION_UPDATE_FAILED },
        () => setOptionDialogTarget(null),
      );
    }
  }

  function handleConfirmDelete() {
    const target = deleteTarget;
    if (!target || shopId === undefined) return;

    if (target.kind === "group") {
      runMutation(
        () => deleteOptionGroupAction(target.group.id, shopId),
        {
          success: PRODUCT_MENU_MESSAGE.OPTION_GROUP_DELETE_SUCCESS,
          failure: PRODUCT_MENU_MESSAGE.OPTION_GROUP_DELETE_FAILED,
        },
        () => setDeleteTarget(null),
      );
    } else {
      runMutation(
        () => deleteOptionAction(target.option.id, shopId),
        { success: PRODUCT_MENU_MESSAGE.OPTION_DELETE_SUCCESS, failure: PRODUCT_MENU_MESSAGE.OPTION_DELETE_FAILED },
        () => setDeleteTarget(null),
      );
    }
  }

  function handleReorderOptions(optionGroupId: number, optionIds: number[]) {
    if (shopId === undefined) return;

    // 확정된 id 배열만 보낸다 — `sort` 숫자는 서버가 인덱스로 0..N-1 정규화한다(backend §5-3).
    runMutation(() => changeOptionOrderAction(optionGroupId, shopId, optionIds), {
      success: PRODUCT_MENU_MESSAGE.ORDER_CHANGE_SUCCESS,
      failure: PRODUCT_MENU_MESSAGE.ORDER_CHANGE_FAILED,
    });
  }

  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{PRODUCT_OPTION_GROUP_COPY.PAGE_TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">
          {PRODUCT_OPTION_GROUP_COPY.PAGE_DESCRIPTION}
        </CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          {accessDeniedMessage === undefined && shopId !== undefined && (
            <>
              <Button type="button" disabled={isBusy} onClick={() => setGroupDialogTarget({ mode: "create" })}>
                {PRODUCT_OPTION_GROUP_COPY.BUTTON_ADD_GROUP}
              </Button>
              <ShopSelector shops={shops} shopId={shopId} disabled={isBusy} onChange={handleShopChange} />
            </>
          )}
        </CardAction>
      </CardHeader>

      <CardContent className="flex flex-col gap-6">
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
        ) : optionGroups === undefined ? (
          // 조회 실패는 화면을 무너뜨리지 않는다 — 가게를 바꾸거나 새로고침해 재시도할 수 있어야 한다.
          <p className="text-destructive text-sm">{errorMessage ?? PRODUCT_MESSAGE.LOAD_FAILED}</p>
        ) : optionGroups.length === 0 ? (
          <Empty>
            <EmptyHeader>
              <EmptyTitle>{PRODUCT_OPTION_GROUP_COPY.EMPTY_GROUPS}</EmptyTitle>
              <EmptyDescription>{PRODUCT_OPTION_GROUP_COPY.EMPTY_GROUPS_DESCRIPTION}</EmptyDescription>
            </EmptyHeader>
          </Empty>
        ) : (
          <Accordion type="single" collapsible className="w-full">
            {optionGroups.map((group) => (
              <OptionGroupAccordion
                key={group.id}
                group={group}
                disabled={isBusy}
                onEditGroup={() => setGroupDialogTarget({ mode: "edit", group })}
                onDeleteGroup={() => setDeleteTarget({ kind: "group", group })}
                onAddOption={() => setOptionDialogTarget({ mode: "create", optionGroupId: group.id })}
                onEditOption={(option) => setOptionDialogTarget({ mode: "edit", optionGroupId: group.id, option })}
                onDeleteOption={(option) => setDeleteTarget({ kind: "option", option })}
                onReorderOptions={(optionIds) => handleReorderOptions(group.id, optionIds)}
              />
            ))}
          </Accordion>
        )}

        <OptionGroupFormDialog
          open={groupDialogTarget !== null}
          group={groupDialogTarget?.mode === "edit" ? groupDialogTarget.group : undefined}
          pending={isMutating}
          onOpenChange={(open) => {
            if (!open) setGroupDialogTarget(null);
          }}
          onSubmit={handleSubmitGroup}
        />

        <OptionFormDialog
          open={optionDialogTarget !== null}
          option={optionDialogTarget?.mode === "edit" ? optionDialogTarget.option : undefined}
          pending={isMutating}
          onOpenChange={(open) => {
            if (!open) setOptionDialogTarget(null);
          }}
          onSubmit={handleSubmitOption}
        />

        <AlertDialog
          open={deleteTarget !== null}
          onOpenChange={(open) => {
            if (!open) setDeleteTarget(null);
          }}
        >
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>
                {deleteTarget?.kind === "option"
                  ? PRODUCT_OPTION_GROUP_COPY.DIALOG_OPTION_DELETE_TITLE
                  : PRODUCT_OPTION_GROUP_COPY.DIALOG_GROUP_DELETE_TITLE}
              </AlertDialogTitle>
              <AlertDialogDescription>
                {deleteTarget?.kind === "option"
                  ? PRODUCT_OPTION_GROUP_COPY.DIALOG_OPTION_DELETE_DESCRIPTION
                  : PRODUCT_OPTION_GROUP_COPY.DIALOG_GROUP_DELETE_DESCRIPTION}
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel disabled={isMutating}>{OPTION_GROUP_SCREEN_COPY.BUTTON_CANCEL}</AlertDialogCancel>
              {/* 삭제는 서버가 제약(그룹 최소 선택 수 등)으로 막을 수 있으므로 닫기를 성공 후로 미룬다. */}
              <AlertDialogAction
                disabled={isMutating}
                onClick={(event) => {
                  event.preventDefault();
                  handleConfirmDelete();
                }}
              >
                {OPTION_GROUP_SCREEN_COPY.BUTTON_DELETE}
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </CardContent>
    </Card>
  );
}
