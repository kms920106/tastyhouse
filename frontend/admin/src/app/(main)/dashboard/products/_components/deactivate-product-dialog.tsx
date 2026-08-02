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
import { deactivateProductAction } from "@/feature/product/actions";
import type { ProductListItem } from "@/feature/product/domain";
import { PRODUCT_MESSAGE } from "@/feature/product/message";

interface DeactivateProductDialogProps {
  product: ProductListItem | null;
  onOpenChange: (open: boolean) => void;
}

export function DeactivateProductDialog({ product, onOpenChange }: DeactivateProductDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleDeactivate() {
    if (!product) return;
    startTransition(async () => {
      const { success, message } = await deactivateProductAction(product.id);
      if (success) {
        toast.success(PRODUCT_MESSAGE.DEACTIVATE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? PRODUCT_MESSAGE.DEACTIVATE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={product != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>상품을 비활성화하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {product
              ? `"${product.name}" 상품이 비활성화되어 노출에서 제외됩니다. 하드 삭제 API 는 없으며, 다시 노출하려면 상품 수정에서 노출 여부를 켜야 합니다.`
              : ""}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isPending}>취소</AlertDialogCancel>
          <AlertDialogAction
            onClick={(event) => {
              event.preventDefault();
              handleDeactivate();
            }}
            disabled={isPending}
          >
            {isPending ? "처리 중..." : "비활성화"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
