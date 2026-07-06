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
import { deleteNoticeAction } from "@/feature/notice/actions";
import type { NoticeListItem } from "@/feature/notice/domain";
import { NOTICE_MESSAGE } from "@/feature/notice/message";

interface DeleteNoticeDialogProps {
  notice: NoticeListItem | null;
  onOpenChange: (open: boolean) => void;
}

export function DeleteNoticeDialog({ notice, onOpenChange }: DeleteNoticeDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleDelete() {
    if (!notice) return;
    startTransition(async () => {
      const { success, message } = await deleteNoticeAction(notice.id);
      if (success) {
        toast.success(NOTICE_MESSAGE.DELETE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? NOTICE_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={notice != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>공지사항을 삭제하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {notice ? `"${notice.title}" 공지사항이 영구적으로 삭제됩니다. 이 작업은 되돌릴 수 없습니다.` : ""}
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
