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
import { closeShopAction } from "@/feature/shop/actions";
import type { ShopListItem } from "@/feature/shop/domain";
import { SHOP_MESSAGE } from "@/feature/shop/message";

interface CloseShopDialogProps {
  shop: ShopListItem | null;
  onOpenChange: (open: boolean) => void;
}

export function CloseShopDialog({ shop, onOpenChange }: CloseShopDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleClose() {
    if (!shop) return;
    startTransition(async () => {
      const { success, message } = await closeShopAction(shop.id);
      if (success) {
        toast.success(SHOP_MESSAGE.CLOSE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? SHOP_MESSAGE.CLOSE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={shop != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>가게를 폐업 처리하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {shop ? `"${shop.name}" 가게가 폐업 처리되어 노출에서 제외됩니다. 되돌릴 수 없습니다.` : ""}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isPending}>취소</AlertDialogCancel>
          <AlertDialogAction
            onClick={(event) => {
              event.preventDefault();
              handleClose();
            }}
            disabled={isPending}
          >
            {isPending ? "처리 중..." : "폐업 처리"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
