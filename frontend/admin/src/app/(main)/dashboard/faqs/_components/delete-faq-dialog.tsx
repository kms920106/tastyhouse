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
import { deleteFaqAction } from "@/feature/faq/actions";
import type { FaqListItem } from "@/feature/faq/domain";
import { FAQ_MESSAGE } from "@/feature/faq/message";

interface DeleteFaqDialogProps {
  faq: FaqListItem | null;
  onOpenChange: (open: boolean) => void;
}

export function DeleteFaqDialog({ faq, onOpenChange }: DeleteFaqDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleDelete() {
    if (!faq) return;
    startTransition(async () => {
      const { success, message } = await deleteFaqAction(faq.id);
      if (success) {
        toast.success(FAQ_MESSAGE.DELETE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? FAQ_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={faq != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>FAQ를 삭제하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {faq ? `"${faq.question}" 항목이 영구적으로 삭제됩니다. 이 작업은 되돌릴 수 없습니다.` : ""}
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
