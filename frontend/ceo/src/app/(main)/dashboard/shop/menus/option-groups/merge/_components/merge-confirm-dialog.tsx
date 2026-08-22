"use client";

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
import type { OptionGroupMergePreview } from "@/feature/product/domain";
import { OPTION_GROUP_MERGE_COPY, OPTION_GROUP_SCREEN_COPY } from "@/feature/product/message";

interface MergeConfirmDialogProps {
  open: boolean;
  preview?: OptionGroupMergePreview;
  pending?: boolean;
  onOpenChange: (open: boolean) => void;
  onConfirm: () => void;
}

/**
 * 합치기 최종 확인.
 *
 * **비가역성을 반드시 명시한다** — 서버에 분리(unmerge) 경로가 없어서 되돌릴 방법이 아예 없다
 * (PDF FAQ: "합친 이후에 다시 분리는 불가해요."). 흡수 그룹에만 있던 옵션이 사라지는 것도 함께
 * 알린다 — 그 손실이 이 조작의 유일한 비가역 부작용이다.
 */
export function MergeConfirmDialog({ open, preview, pending, onOpenChange, onConfirm }: MergeConfirmDialogProps) {
  // 영향받는 메뉴 수는 기준·후보의 연결 메뉴를 합친 뒤 중복을 제거해야 실제 수가 된다 —
  // 같은 메뉴가 여러 그룹에 걸려 있으면 단순 합산이 과대 집계된다.
  const affectedProductCount =
    preview === undefined
      ? 0
      : new Set([preview.base, ...preview.candidates].flatMap((item) => item.linkedProductNames)).size;

  return (
    <AlertDialog open={open && preview !== undefined} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{OPTION_GROUP_MERGE_COPY.DIALOG_MERGE_TITLE}</AlertDialogTitle>
          <AlertDialogDescription className="flex flex-col gap-1">
            {preview !== undefined && (
              <>
                <span>{OPTION_GROUP_MERGE_COPY.DIALOG_MERGE_BASE(preview.base.name)}</span>
                <span>{OPTION_GROUP_MERGE_COPY.DIALOG_MERGE_ABSORBED(preview.candidates.length)}</span>
                <span>{OPTION_GROUP_MERGE_COPY.DIALOG_MERGE_AFFECTED(affectedProductCount)}</span>
                <span className="font-medium text-destructive">
                  {OPTION_GROUP_MERGE_COPY.DIALOG_MERGE_IRREVERSIBLE}
                </span>
                <span>{OPTION_GROUP_MERGE_COPY.DIALOG_MERGE_VANISHING}</span>
              </>
            )}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={pending}>{OPTION_GROUP_SCREEN_COPY.BUTTON_CANCEL}</AlertDialogCancel>
          {/* 서버가 검증으로 막을 수 있으므로 닫기를 성공 후로 미룬다(옵션 삭제 다이얼로그 선례). */}
          <AlertDialogAction
            disabled={pending}
            onClick={(event) => {
              event.preventDefault();
              onConfirm();
            }}
          >
            {OPTION_GROUP_MERGE_COPY.BUTTON_MERGE}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
