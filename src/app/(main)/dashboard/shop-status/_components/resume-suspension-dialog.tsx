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
import { releaseSuspensionAction } from "@/feature/shop/actions";
import { SHOP_MESSAGE } from "@/feature/shop/message";

interface ResumeSuspensionDialogProps {
  target: { shopId: number; suspensionId: number; shopName: string } | null;
  onOpenChange: (open: boolean) => void;
}

export function ResumeSuspensionDialog({ target, onOpenChange }: ResumeSuspensionDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleConfirm() {
    if (!target) return;

    startTransition(async () => {
      const { success, message } = await releaseSuspensionAction(target.shopId, target.suspensionId);
      if (success) {
        toast.success(SHOP_MESSAGE.SUSPENSION_RELEASE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={target !== null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>영업 임시중지를 해제할까요?</AlertDialogTitle>
          <AlertDialogDescription>
            {target?.shopName}의 임시중지를 해제하면 즉시 주문을 다시 받습니다.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isPending}>취소</AlertDialogCancel>
          <AlertDialogAction onClick={handleConfirm} disabled={isPending}>
            {isPending ? "해제 중..." : "해제"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
