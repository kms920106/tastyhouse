"use client";

import * as React from "react";

import { ChevronRight, X } from "lucide-react";
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
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { FieldLabel } from "@/components/ui/field";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { releaseRepresentativeAction, requestRepresentativeAction } from "@/feature/product/actions";
import { PRODUCT_REPRESENTATIVE_MAX_COUNT } from "@/feature/product/constants";
import type { MenuBoardGroup, MenuBoardRow } from "@/feature/product/domain";
import { PRODUCT_REPRESENTATIVE_COPY, PRODUCT_REPRESENTATIVE_MESSAGE } from "@/feature/product/message";

import { ShopImagePreview } from "../../_components/shop-image-preview";

interface RepresentativeMenuSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  /**
   * 메뉴판이 이미 받아 둔 그룹 목록.
   *
   * 현재 지정 목록과 선택 후보를 모두 여기서 뽑는다 — 판정에 필요한 `representative` 와
   * `imageUrl` 이 이 응답에 이미 들어 있어 전용 조회 API 가 필요 없다.
   */
  groups?: MenuBoardGroup[];
}

/**
 * 사장님 추천 시트.
 *
 * PDF 등록 기준 4개 중 화면이 강제하는 것은 **2번(개수 6개)** 과 **3번(이미지 필수)** 뿐이다.
 * 1번(가게 카테고리와 일치)과 4번(메뉴명과 이미지 일치)은 **사람이 판단하는 검수 기준**이라
 * 화면이 막지 않는다 — 안내만 하고, 어긋나면 관리자 검수에서 반려된다.
 */
export function RepresentativeMenuSheet({ open, onOpenChange, shopId, groups }: RepresentativeMenuSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const [isPickerOpen, setPickerOpen] = React.useState(false);
  const [selectedIds, setSelectedIds] = React.useState<ReadonlySet<number>>(() => new Set());
  const [releaseTarget, setReleaseTarget] = React.useState<MenuBoardRow | null>(null);

  const allRows = React.useMemo(() => (groups ?? []).flatMap((group) => group.products), [groups]);
  const currentRows = React.useMemo(() => allRows.filter((row) => row.representative), [allRows]);
  const currentCount = currentRows.length;

  // 다이얼로그를 열 때마다 선택을 비운다 — 지난 선택이 남아 있으면 이미 지정된 메뉴가 다시 담긴다.
  React.useEffect(() => {
    if (isPickerOpen) setSelectedIds(new Set());
  }, [isPickerOpen]);

  const remainingSlots = PRODUCT_REPRESENTATIVE_MAX_COUNT - currentCount;

  function handleToggle(row: MenuBoardRow, checked: boolean) {
    if (!checked) {
      setSelectedIds((previous) => {
        const next = new Set(previous);
        next.delete(row.id);
        return next;
      });
      return;
    }

    // 현재 + 신규 합계가 6개를 넘으면 그 체크만 막는다 — 이미 담은 선택은 유지해
    // 무엇을 뺄지 점주가 고를 수 있게 한다.
    if (selectedIds.size >= remainingSlots) {
      toast.error(PRODUCT_REPRESENTATIVE_MESSAGE.LIMIT_EXCEEDED);
      return;
    }

    setSelectedIds((previous) => new Set(previous).add(row.id));
  }

  function handleApply() {
    const productIds = [...selectedIds];
    if (productIds.length === 0) return;

    startTransition(async () => {
      // 이미 대표거나 검수 대기 중인 메뉴는 서버가 조용히 건너뛰므로 반환 개수를 대조하지 않는다 —
      // `revalidatePath` 로 갱신된 목록이 최종 상태다.
      const { success, message } = await requestRepresentativeAction(shopId, productIds, currentCount);
      if (!success) {
        toast.error(message ?? PRODUCT_REPRESENTATIVE_COPY.REQUEST_FAILED);
        return;
      }
      toast.success(PRODUCT_REPRESENTATIVE_MESSAGE.REQUEST_SUCCESS);
      setPickerOpen(false);
    });
  }

  function handleConfirmRelease() {
    if (releaseTarget === null) return;
    const productId = releaseTarget.id;

    startTransition(async () => {
      // 지정과 달리 해제는 검수 대상이 아니라 즉시 반영된다.
      const { success, message } = await releaseRepresentativeAction(productId, shopId, currentCount);
      setReleaseTarget(null);
      if (!success) {
        toast.error(message ?? PRODUCT_REPRESENTATIVE_COPY.RELEASE_FAILED);
        return;
      }
      toast.success(PRODUCT_REPRESENTATIVE_MESSAGE.RELEASE_SUCCESS);
    });
  }

  const isFull = currentCount >= PRODUCT_REPRESENTATIVE_MAX_COUNT;
  // 마지막 1개는 해제할 수 없다 — 추천이 0개가 되면 손님 화면의 추천 그룹이 통째로 사라진다.
  const releaseDisabled = currentCount <= 1;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{PRODUCT_REPRESENTATIVE_COPY.SHEET_TITLE}</SheetTitle>
          <SheetDescription>{PRODUCT_REPRESENTATIVE_COPY.SHEET_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex flex-1 flex-col gap-4 overflow-y-auto px-4">
          <div className="flex items-center justify-between gap-2">
            <span className="font-medium text-sm">{PRODUCT_REPRESENTATIVE_COPY.CURRENT_TITLE}</span>
            <span className="text-muted-foreground text-sm">
              {currentCount} / {PRODUCT_REPRESENTATIVE_MAX_COUNT}
            </span>
          </div>

          {currentRows.length === 0 ? (
            <span className="py-4 text-muted-foreground text-sm">{PRODUCT_REPRESENTATIVE_COPY.EMPTY}</span>
          ) : (
            <ul className="flex flex-col">
              {currentRows.map((row) => (
                <li key={row.id} className="flex items-center gap-3 border-b py-3 last:border-b-0">
                  <ShopImagePreview
                    src={row.imageUrl}
                    alt={`${PRODUCT_REPRESENTATIVE_COPY.IMAGE_ALT_PREFIX}${row.name}`}
                    className="size-16 shrink-0"
                  />
                  <div className="flex min-w-0 flex-1 flex-col gap-1">
                    <span className="truncate text-sm">{row.name}</span>
                    <Badge variant="secondary" className="w-fit">
                      {PRODUCT_REPRESENTATIVE_COPY.BADGE_REPRESENTATIVE}
                    </Badge>
                  </div>
                  <Button
                    type="button"
                    size="sm"
                    variant="ghost"
                    disabled={isPending || releaseDisabled}
                    aria-label={PRODUCT_REPRESENTATIVE_COPY.ACTION_RELEASE}
                    onClick={() => setReleaseTarget(row)}
                  >
                    <X className="size-4" />
                  </Button>
                </li>
              ))}
            </ul>
          )}

          {isFull && <p className="text-muted-foreground text-xs">{PRODUCT_REPRESENTATIVE_MESSAGE.LIMIT_EXCEEDED}</p>}
          {releaseDisabled && currentCount === 1 && (
            <p className="text-muted-foreground text-xs">{PRODUCT_REPRESENTATIVE_MESSAGE.LAST_CANNOT_RELEASE}</p>
          )}

          <Button
            type="button"
            variant="outline"
            className="w-full"
            disabled={isPending || isFull}
            onClick={() => setPickerOpen(true)}
          >
            {PRODUCT_REPRESENTATIVE_COPY.BUTTON_ADD}
          </Button>

          <Collapsible className="group/criteria rounded-md border px-3 py-2">
            <CollapsibleTrigger className="flex w-full items-center justify-between gap-2 text-left font-medium text-sm">
              {PRODUCT_REPRESENTATIVE_COPY.CRITERIA_TITLE}
              <ChevronRight className="size-4 transition-transform duration-200 group-data-[state=open]/criteria:rotate-90" />
            </CollapsibleTrigger>
            <CollapsibleContent>
              {/* 1번·4번은 사람이 판단하는 검수 기준이라 화면이 막지 않는다 — 안내만 한다. */}
              <ol className="mt-2 list-decimal space-y-1 pl-4 text-muted-foreground text-xs leading-snug">
                {PRODUCT_REPRESENTATIVE_COPY.CRITERIA_ITEMS.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ol>
            </CollapsibleContent>
          </Collapsible>
        </div>

        <SheetFooter>
          <SheetClose asChild>
            <Button variant="outline" disabled={isPending}>
              {PRODUCT_REPRESENTATIVE_COPY.BUTTON_CLOSE}
            </Button>
          </SheetClose>
        </SheetFooter>

        <Dialog open={isPickerOpen} onOpenChange={setPickerOpen}>
          <DialogContent className="flex max-h-[80vh] flex-col">
            <DialogHeader>
              <DialogTitle>{PRODUCT_REPRESENTATIVE_COPY.PICKER_TITLE}</DialogTitle>
              <DialogDescription>{PRODUCT_REPRESENTATIVE_COPY.PICKER_DESCRIPTION}</DialogDescription>
            </DialogHeader>

            <div className="flex-1 overflow-y-auto">
              {(groups ?? []).length === 0 ? (
                <p className="py-4 text-muted-foreground text-sm">{PRODUCT_REPRESENTATIVE_COPY.PICKER_EMPTY}</p>
              ) : (
                (groups ?? []).map((group) => {
                  // 이미 지정된 메뉴는 후보에서 뺀다 — 다시 신청해도 서버가 건너뛰므로 담을 이유가 없다.
                  const candidates = group.products.filter((row) => !row.representative);
                  if (candidates.length === 0) return null;

                  return (
                    <div key={group.categoryId ?? "uncategorized"} className="mb-3 flex flex-col gap-1">
                      <span className="font-medium text-muted-foreground text-xs">
                        {group.categoryName ?? PRODUCT_REPRESENTATIVE_COPY.PICKER_UNCATEGORIZED}
                      </span>
                      {candidates.map((row) => {
                        // PDF 기준 3번 — 이미지가 없는 메뉴는 체크 자체를 막는다(서버까지 가지 않는다).
                        const imageMissing = row.imageUrl === null;
                        const checkboxId = `representative-candidate-${row.id}`;

                        return (
                          <div key={row.id} className="flex items-center gap-2 rounded-md px-1 py-1.5">
                            <Checkbox
                              id={checkboxId}
                              checked={selectedIds.has(row.id)}
                              disabled={isPending || imageMissing}
                              onCheckedChange={(checked) => handleToggle(row, checked === true)}
                            />
                            <FieldLabel htmlFor={checkboxId} className="min-w-0 flex-1 font-normal">
                              <span className="truncate">{row.name}</span>
                            </FieldLabel>
                            {imageMissing && (
                              <span className="shrink-0 text-muted-foreground text-xs">
                                {PRODUCT_REPRESENTATIVE_COPY.PICKER_IMAGE_MISSING_HINT}
                              </span>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  );
                })
              )}
            </div>

            <DialogFooter>
              <span className="mr-auto self-center text-muted-foreground text-sm">
                {PRODUCT_REPRESENTATIVE_COPY.PICKER_SELECTED_PREFIX}
                {selectedIds.size}
                {PRODUCT_REPRESENTATIVE_COPY.PICKER_SELECTED_SUFFIX}
              </span>
              <Button type="button" variant="outline" disabled={isPending} onClick={() => setPickerOpen(false)}>
                {PRODUCT_REPRESENTATIVE_COPY.PICKER_CANCEL}
              </Button>
              <Button type="button" disabled={isPending || selectedIds.size === 0} onClick={handleApply}>
                {PRODUCT_REPRESENTATIVE_COPY.PICKER_SUBMIT}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        <AlertDialog
          open={releaseTarget !== null}
          onOpenChange={(next) => {
            if (!next) setReleaseTarget(null);
          }}
        >
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>{PRODUCT_REPRESENTATIVE_COPY.RELEASE_CONFIRM_TITLE}</AlertDialogTitle>
              <AlertDialogDescription>{PRODUCT_REPRESENTATIVE_COPY.RELEASE_CONFIRM_DESCRIPTION}</AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>{PRODUCT_REPRESENTATIVE_COPY.RELEASE_CONFIRM_CANCEL}</AlertDialogCancel>
              <AlertDialogAction disabled={isPending} onClick={handleConfirmRelease}>
                {PRODUCT_REPRESENTATIVE_COPY.RELEASE_CONFIRM_ACTION}
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </SheetContent>
    </Sheet>
  );
}
