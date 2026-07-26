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
import { deleteContentBoardAction } from "@/feature/shop/actions";
import type { ContentBoard } from "@/feature/shop/domain";
import { SHOP_MESSAGE } from "@/feature/shop/message";

interface DeleteContentBoardDialogProps {
  contentBoard: ContentBoard | null;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

export function DeleteContentBoardDialog({ contentBoard, onOpenChange, onSuccess }: DeleteContentBoardDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleDelete() {
    if (!contentBoard) return;
    startTransition(async () => {
      const { success, message } = await deleteContentBoardAction(contentBoard.id);
      if (success) {
        toast.success(SHOP_MESSAGE.CONTENT_BOARD_DELETE_SUCCESS);
        onOpenChange(false);
        onSuccess();
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={contentBoard != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>콘텐츠보드를 삭제하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {contentBoard ? `콘텐츠보드 ID ${contentBoard.id}가 삭제되며 되돌릴 수 없습니다.` : ""}
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
            {isPending ? "처리 중..." : "삭제"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
