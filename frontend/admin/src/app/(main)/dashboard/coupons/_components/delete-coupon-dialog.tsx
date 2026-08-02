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
import { deleteCouponAction } from "@/feature/coupon/actions";
import type { CouponListItem } from "@/feature/coupon/domain";
import { COUPON_MESSAGE } from "@/feature/coupon/message";

interface DeleteCouponDialogProps {
  coupon: CouponListItem | null;
  onOpenChange: (open: boolean) => void;
}

export function DeleteCouponDialog({ coupon, onOpenChange }: DeleteCouponDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleDelete() {
    if (!coupon) return;
    startTransition(async () => {
      const { success, message } = await deleteCouponAction(coupon.id);
      if (success) {
        toast.success(COUPON_MESSAGE.DELETE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? COUPON_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={coupon != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>쿠폰을 삭제하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {coupon ? `"${coupon.name}" 쿠폰이 삭제되어 목록에서 제외됩니다. 이 작업은 되돌릴 수 없습니다.` : ""}
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
