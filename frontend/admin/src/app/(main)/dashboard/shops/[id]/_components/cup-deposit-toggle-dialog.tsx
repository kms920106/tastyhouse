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
import { updateShopCupDepositAction } from "@/feature/shop/actions";
import { SHOP_CUP_DEPOSIT_COPY, SHOP_MESSAGE } from "@/feature/shop/message";

interface CupDepositToggleDialogProps {
  /** 토글 대상 가게 ID. null 이면 닫힌 상태. */
  shopId: number | null;
  /** 확인 시 반영할 다음 상태 */
  nextEnabled: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

// 외부 규제 사실(환경부 지정 일회용컵 보증금제)을 다루는 신중한 조작이라 스위치 클릭만으로 즉시 반영하지 않고 확인을 받는다.
export function CupDepositToggleDialog({
  shopId,
  nextEnabled,
  onOpenChange,
  onSuccess,
}: CupDepositToggleDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleConfirm() {
    if (shopId == null) return;
    startTransition(async () => {
      const { success, message } = await updateShopCupDepositAction(shopId, nextEnabled);
      if (success) {
        toast.success(
          nextEnabled ? SHOP_MESSAGE.CUP_DEPOSIT_ENABLE_SUCCESS : SHOP_MESSAGE.CUP_DEPOSIT_DISABLE_SUCCESS,
        );
        onOpenChange(false);
        onSuccess();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CUP_DEPOSIT_UPDATE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={shopId != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>
            {nextEnabled
              ? SHOP_CUP_DEPOSIT_COPY.ENABLE_CONFIRM_TITLE
              : SHOP_CUP_DEPOSIT_COPY.DISABLE_CONFIRM_TITLE}
          </AlertDialogTitle>
          <AlertDialogDescription>
            {nextEnabled
              ? SHOP_CUP_DEPOSIT_COPY.ENABLE_CONFIRM_DESCRIPTION
              : SHOP_CUP_DEPOSIT_COPY.DISABLE_CONFIRM_DESCRIPTION}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isPending}>취소</AlertDialogCancel>
          <AlertDialogAction
            onClick={(event) => {
              event.preventDefault();
              handleConfirm();
            }}
            disabled={isPending}
          >
            {isPending ? "처리 중..." : nextEnabled ? "대상사업자로 지정" : "지정 해제"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
