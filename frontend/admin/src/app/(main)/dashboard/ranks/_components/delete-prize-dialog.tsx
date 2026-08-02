"use client";

import * as React from "react";

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
import { deletePrizeAction } from "@/feature/rank/actions";
import type { RankPrize } from "@/feature/rank/domain";
import { RANK_MESSAGE } from "@/feature/rank/message";

interface DeletePrizeDialogProps {
  prize: RankPrize | null;
  onOpenChange: (open: boolean) => void;
  onDeleted: () => void;
}

export function DeletePrizeDialog({ prize, onOpenChange, onDeleted }: DeletePrizeDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleDelete() {
    if (!prize) return;
    startTransition(async () => {
      const { success, message } = await deletePrizeAction(prize.id);
      if (success) {
        toast.success(RANK_MESSAGE.PRIZE_DELETE_SUCCESS);
        onOpenChange(false);
        onDeleted();
      } else {
        toast.error(message ?? RANK_MESSAGE.PRIZE_DELETE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={prize != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>경품을 삭제하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {prize ? `"${prize.name}" 경품이 삭제됩니다. 이 작업은 되돌릴 수 없습니다.` : ""}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isPending}>취소</AlertDialogCancel>
          <AlertDialogAction
            onClick={(event) => {
              event.preventDefault();
              handleDelete();
            }}
            disabled={isPending}
          >
            {isPending ? "삭제 중..." : "삭제"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
