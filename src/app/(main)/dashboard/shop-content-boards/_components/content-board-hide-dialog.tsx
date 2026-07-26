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
import { hideContentBoardAction } from "@/feature/shop/actions";
import type { ContentBoard } from "@/feature/shop/domain";
import { SHOP_MESSAGE } from "@/feature/shop/message";

interface ContentBoardHideDialogProps {
  contentBoard: ContentBoard | null;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

export function ContentBoardHideDialog({ contentBoard, onOpenChange, onSuccess }: ContentBoardHideDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  const nextHidden = contentBoard ? !contentBoard.hidden : false;

  function handleToggle() {
    if (!contentBoard) return;
    startTransition(async () => {
      const { success, message } = await hideContentBoardAction(contentBoard.id, { hidden: nextHidden });
      if (success) {
        toast.success(nextHidden ? SHOP_MESSAGE.CONTENT_BOARD_HIDE_SUCCESS : SHOP_MESSAGE.CONTENT_BOARD_SHOW_SUCCESS);
        onOpenChange(false);
        onSuccess();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={contentBoard != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>
            {nextHidden ? "콘텐츠보드를 숨김 처리하시겠습니까?" : "콘텐츠보드를 노출 처리하시겠습니까?"}
          </AlertDialogTitle>
          <AlertDialogDescription>
            {contentBoard
              ? nextHidden
                ? `콘텐츠보드 ID ${contentBoard.id}가 숨김 처리되어 사용자에게 노출되지 않습니다.`
                : `콘텐츠보드 ID ${contentBoard.id}가 다시 노출됩니다.`
              : ""}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isPending}>취소</AlertDialogCancel>
          <AlertDialogAction
            onClick={(event) => {
              event.preventDefault();
              handleToggle();
            }}
            disabled={isPending}
          >
            {isPending ? "처리 중..." : nextHidden ? "숨김 처리" : "노출 처리"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
