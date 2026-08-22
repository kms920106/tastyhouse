"use client";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { Lock, Store } from "lucide-react";
import { toast } from "sonner";

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
import { Empty, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  excludeOptionGroupMergeSuggestionAction,
  loadOptionGroupMergePreviewAction,
  mergeOptionGroupsAction,
} from "@/feature/product/actions";
import { OPTION_GROUP_MERGE_MIN_SELECTION, OPTION_GROUP_MERGE_MODES } from "@/feature/product/constants";
import type {
  MenuOptionGroup,
  OptionGroupMergeMode,
  OptionGroupMergePreview,
  OptionGroupMergeSuggestion,
} from "@/feature/product/domain";
import {
  OPTION_GROUP_MERGE_COPY,
  OPTION_GROUP_MERGE_MESSAGE,
  OPTION_GROUP_SCREEN_COPY,
  PRODUCT_MESSAGE,
} from "@/feature/product/message";
import type { ShopSummary } from "@/feature/shop/domain";

import { ShopSelector } from "../../../../_components/shop-selector";
import { MergeBaseSelector } from "./merge-base-selector";
import { MergeConfirmDialog } from "./merge-confirm-dialog";
import { MergeDiffSheet } from "./merge-diff-sheet";
import { MergeManualPicker } from "./merge-manual-picker";
import { MergeSuggestionList } from "./merge-suggestion-list";

interface MergeManageProps {
  shops: ShopSummary[];
  shopId?: number;
  mode: OptionGroupMergeMode;
  keyword?: string;
  /** 조회 실패 시 undefined 로 넘어와 셸만 살린다 */
  suggestions?: OptionGroupMergeSuggestion[];
  /** 검색어가 적용된 목록. 직접 선택 화면이 보여주는 후보다 */
  optionGroups?: MenuOptionGroup[];
  /**
   * 검색 이전의 전체 목록.
   *
   * 선택한 그룹이 검색어에 걸려 목록에서 사라져도 기준 선택·합치기 대상에는 남아야 하므로
   * 이름을 되짚을 원본이 따로 필요하다 — 필터된 목록만 들고 있으면 선택이 조용히 사라진다.
   */
  allOptionGroups?: MenuOptionGroup[];
  errorCode?: string;
  errorMessage?: string;
}

export function MergeManage({
  shops,
  shopId,
  mode,
  keyword,
  suggestions,
  optionGroups,
  allOptionGroups,
  errorCode,
  errorMessage,
}: MergeManageProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isNavigating, startNavigation] = React.useTransition();
  const [isMutating, startMutation] = React.useTransition();

  const [selectedIds, setSelectedIds] = React.useState<number[]>([]);
  const [baseOptionGroupId, setBaseOptionGroupId] = React.useState<number>();
  const [preview, setPreview] = React.useState<OptionGroupMergePreview>();
  const [isDiffOpen, setDiffOpen] = React.useState(false);
  const [isConfirmOpen, setConfirmOpen] = React.useState(false);
  const [excludeTarget, setExcludeTarget] = React.useState<OptionGroupMergeSuggestion | null>(null);
  /** 기준 선택 단계로 넘어간 추천 묶음. 카드에 활성 표시를 하기 위한 것이다 */
  const [selectedSignature, setSelectedSignature] = React.useState<string>();

  /**
   * 이 화면의 진입 경로. 서버 이력(`PRODUCT_OPTION_GROUP_MERGE_HISTORY`)에 append-only 로 남아
   * 잘못 기록하면 정정할 수 없다.
   *
   * **`useRef` 가 아니라 상태로 둔다.** ref 는 마운트 시 값이 굳어 URL 직접 진입·뒤로가기로
   * `mode` prop 만 바뀔 때 따라오지 않는다. 조작(추천 카드 선택 / 체크박스 토글)이 곧 진입 경로가
   * 되도록 전이의 일부로 다룬다.
   */
  const [entryType, setEntryType] = React.useState<OptionGroupMergeMode>(mode);

  const accessDeniedMessage =
    errorCode === "SHOP_ACCESS_DENIED"
      ? PRODUCT_MESSAGE.SHOP_ACCESS_DENIED
      : errorCode === "SHOP_NOT_FOUND"
        ? PRODUCT_MESSAGE.SHOP_NOT_FOUND
        : undefined;

  const isBusy = isNavigating || isMutating;

  /** URL 갱신 공통 처리 — 서버가 searchParams 로 조회하므로 재조회가 자동으로 따라온다 */
  function updateSearchParams(next: Record<string, string | undefined>) {
    const params = new URLSearchParams(searchParams.toString());

    for (const [key, value] of Object.entries(next)) {
      if (value === undefined) params.delete(key);
      else params.set(key, value);
    }

    startNavigation(() => {
      router.push(`?${params.toString()}`);
    });
  }

  /** 모드를 바꾸면 진행 중이던 선택을 버린다 — 두 경로의 선택 의미가 달라 이어 붙이면 혼동된다 */
  function handleModeChange(nextMode: string) {
    resetSelection();
    setEntryType(nextMode === OPTION_GROUP_MERGE_MODES.MANUAL ? "MANUAL" : "RECOMMENDED");
    updateSearchParams({ mode: nextMode });
  }

  function resetSelection() {
    setSelectedSignature(undefined);
    setSelectedIds([]);
    setBaseOptionGroupId(undefined);
    setPreview(undefined);
    setDiffOpen(false);
    setConfirmOpen(false);
  }

  /** 실패 문구는 서버가 내려준 한국어 `message` 를 그대로 쓴다(이 앱의 확립된 관례) */
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

  /**
   * 추천 카드에서 넘어온 선택.
   *
   * 묶음의 옵션그룹 전체를 선택 상태로 옮기고 기준 선택 단계로 보낸다 — 어느 그룹이 남을지는
   * 서버가 정할 수 없고(어떤 이름·연결을 남길지는 사장님 결정) 사용자가 골라야 한다.
   */
  function handleSelectSuggestion(suggestion: OptionGroupMergeSuggestion) {
    setEntryType("RECOMMENDED");
    setSelectedSignature(suggestion.signature);
    setSelectedIds(suggestion.groups.map((group) => group.id));
    setBaseOptionGroupId(undefined);
    setPreview(undefined);
  }

  function handleToggleSelection(optionGroupId: number, checked: boolean) {
    // 체크박스로 직접 고른 순간부터는 진입 경로가 직접 선택이다 — 추천 카드에서 넘어온 묶음을
    // 손으로 고쳤다면 그것은 더 이상 추천 그대로가 아니므로 이력에 MANUAL 로 남아야 한다.
    setEntryType("MANUAL");
    setSelectedSignature(undefined);

    setSelectedIds((prev) => (checked ? [...prev, optionGroupId] : prev.filter((id) => id !== optionGroupId)));

    // 선택이 바뀌면 이전 기준·미리보기는 더 이상 유효하지 않다.
    setBaseOptionGroupId(undefined);
    setPreview(undefined);
  }

  /**
   * 기준을 고르면 곧바로 미리보기를 받는다.
   *
   * 여기서 `mergeable`/`blockedReason` 이 함께 오므로 합치기 버튼의 활성 여부가 이 응답으로 정해진다.
   */
  function handleBaseChange(nextBaseId: number) {
    setBaseOptionGroupId(nextBaseId);
    setPreview(undefined);

    if (shopId === undefined) return;

    const targetIds = selectedIds.filter((id) => id !== nextBaseId);
    if (targetIds.length === 0) return;

    startMutation(async () => {
      const result = await loadOptionGroupMergePreviewAction(shopId, nextBaseId, targetIds);

      if (!result.success || result.data === undefined) {
        toast.error(result.message ?? OPTION_GROUP_MERGE_MESSAGE.PREVIEW_LOAD_FAILED);
        return;
      }

      setPreview(result.data);
    });
  }

  function handleConfirmExclude() {
    const target = excludeTarget;
    if (!target || shopId === undefined) return;

    runMutation(
      () =>
        excludeOptionGroupMergeSuggestionAction(
          shopId,
          target.signature,
          target.groups.map((group) => group.id),
        ),
      {
        success: OPTION_GROUP_MERGE_MESSAGE.EXCLUDE_SUCCESS,
        failure: OPTION_GROUP_MERGE_MESSAGE.EXCLUDE_FAILED,
      },
      () => setExcludeTarget(null),
    );
  }

  function handleConfirmMerge() {
    // **확인 다이얼로그가 보여준 `preview` 를 그대로 페이로드로 쓴다.** 선택 상태에서 다시 조립하면
    // 사용자가 승인한 내용과 전송되는 내용이 서로 다른 소스가 되어, 비가역 조작에서 "확인한 것과
    // 다른 것이 실행"될 여지가 남는다.
    if (shopId === undefined || preview === undefined) return;

    runMutation(
      () =>
        mergeOptionGroupsAction(shopId, {
          baseOptionGroupId: preview.base.id,
          optionGroupIds: preview.candidates.map((candidate) => candidate.id),
          entryType,
        }),
      { success: OPTION_GROUP_MERGE_MESSAGE.MERGE_SUCCESS, failure: OPTION_GROUP_MERGE_MESSAGE.MERGE_FAILED },
      () => {
        resetSelection();
        // 합치기 결과는 옵션그룹 관리 화면이 보여준다 — 이 화면에 남으면 방금 합친 묶음이 사라진
        // 빈 목록만 보인다.
        router.push(`/dashboard/shop/menus/option-groups?shopId=${shopId}`);
      },
    );
  }

  const groups = optionGroups ?? [];
  // 선택 상태는 검색과 독립이므로 필터된 목록이 아니라 전체 목록에서 뽑는다.
  const selectedGroups = (allOptionGroups ?? groups).filter((group) => selectedIds.includes(group.id));
  const hasEnoughSelection = selectedIds.length >= OPTION_GROUP_MERGE_MIN_SELECTION;

  // 합치기는 서버 사전 검증(`mergeable`)을 통과해야만 누를 수 있다 — 프론트가 다시 판정하지 않는다.
  const canMerge = preview?.mergeable === true && baseOptionGroupId !== undefined;
  const blockedMessage =
    preview !== undefined && !preview.mergeable
      ? ((preview.blockedReason !== null
          ? OPTION_GROUP_MERGE_COPY.BLOCKED_REASON_LABEL[preview.blockedReason]
          : undefined) ?? OPTION_GROUP_MERGE_COPY.BLOCKED_REASON_FALLBACK)
      : undefined;

  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{OPTION_GROUP_MERGE_COPY.PAGE_TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{OPTION_GROUP_MERGE_COPY.PAGE_DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          {accessDeniedMessage === undefined && shopId !== undefined && (
            <ShopSelector
              shops={shops}
              shopId={shopId}
              disabled={isBusy}
              onChange={(nextShopId) => {
                resetSelection();
                updateSearchParams({ shopId: String(nextShopId) });
              }}
            />
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
        ) : (
          <>
            <Tabs value={mode} onValueChange={handleModeChange}>
              <TabsList>
                <TabsTrigger value={OPTION_GROUP_MERGE_MODES.RECOMMENDED} disabled={isBusy}>
                  {OPTION_GROUP_MERGE_COPY.TAB_RECOMMENDED}
                </TabsTrigger>
                <TabsTrigger value={OPTION_GROUP_MERGE_MODES.MANUAL} disabled={isBusy}>
                  {OPTION_GROUP_MERGE_COPY.TAB_MANUAL}
                </TabsTrigger>
              </TabsList>
            </Tabs>

            {/* 조회 실패는 화면을 무너뜨리지 않는다 — 가게를 바꾸거나 새로고침해 재시도할 수 있어야 한다. */}
            {mode === OPTION_GROUP_MERGE_MODES.RECOMMENDED ? (
              suggestions === undefined ? (
                <p className="text-destructive text-sm">
                  {errorMessage ?? OPTION_GROUP_MERGE_MESSAGE.SUGGESTIONS_LOAD_FAILED}
                </p>
              ) : (
                <MergeSuggestionList
                  suggestions={suggestions}
                  selectedSignature={selectedSignature}
                  disabled={isBusy}
                  onExclude={setExcludeTarget}
                  onSelect={handleSelectSuggestion}
                />
              )
            ) : optionGroups === undefined ? (
              <p className="text-destructive text-sm">{errorMessage ?? PRODUCT_MESSAGE.LOAD_FAILED}</p>
            ) : (
              <MergeManualPicker
                optionGroups={groups}
                keyword={keyword}
                selectedIds={selectedIds}
                disabled={isBusy}
                onKeywordSubmit={(nextKeyword) => updateSearchParams({ keyword: nextKeyword })}
                onToggle={handleToggleSelection}
              />
            )}

            {/* 기준 선택은 두 모드가 공유한다 — 추천에서 넘어온 선택도 여기서 기준을 고른다. */}
            {hasEnoughSelection && (
              <div className="flex flex-col gap-4 rounded-md border p-4">
                <MergeBaseSelector
                  candidates={selectedGroups}
                  baseOptionGroupId={baseOptionGroupId}
                  disabled={isBusy}
                  onChange={handleBaseChange}
                />

                {blockedMessage !== undefined && <p className="text-destructive text-sm">{blockedMessage}</p>}

                <div className="flex flex-wrap justify-end gap-2">
                  <Button
                    type="button"
                    variant="outline"
                    disabled={isBusy || preview === undefined}
                    onClick={() => setDiffOpen(true)}
                  >
                    {OPTION_GROUP_MERGE_COPY.BUTTON_VIEW_DIFF}
                  </Button>
                  <Button type="button" disabled={isBusy || !canMerge} onClick={() => setConfirmOpen(true)}>
                    {OPTION_GROUP_MERGE_COPY.BUTTON_MERGE}
                  </Button>
                </div>
              </div>
            )}
          </>
        )}

        <MergeDiffSheet open={isDiffOpen} preview={preview} onOpenChange={setDiffOpen} />

        <MergeConfirmDialog
          open={isConfirmOpen}
          preview={preview}
          pending={isMutating}
          onOpenChange={setConfirmOpen}
          onConfirm={handleConfirmMerge}
        />

        <AlertDialog
          open={excludeTarget !== null}
          onOpenChange={(open) => {
            if (!open) setExcludeTarget(null);
          }}
        >
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>{OPTION_GROUP_MERGE_COPY.DIALOG_EXCLUDE_TITLE}</AlertDialogTitle>
              <AlertDialogDescription>{OPTION_GROUP_MERGE_COPY.DIALOG_EXCLUDE_DESCRIPTION}</AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel disabled={isMutating}>{OPTION_GROUP_SCREEN_COPY.BUTTON_CANCEL}</AlertDialogCancel>
              {/* 서버가 서명 불일치로 막을 수 있으므로 닫기를 성공 후로 미룬다. */}
              <AlertDialogAction
                disabled={isMutating}
                onClick={(event) => {
                  event.preventDefault();
                  handleConfirmExclude();
                }}
              >
                {OPTION_GROUP_MERGE_COPY.BUTTON_EXCLUDE}
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </CardContent>
    </Card>
  );
}
