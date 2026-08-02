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
import { soldOutProductAction } from "@/feature/product/actions";
import type { ProductListItem } from "@/feature/product/domain";
import { PRODUCT_MESSAGE } from "@/feature/product/message";

interface SoldOutProductDialogProps {
  product: ProductListItem | null;
  onOpenChange: (open: boolean) => void;
}

export function SoldOutProductDialog({ product, onOpenChange }: SoldOutProductDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleSoldOut() {
    if (!product) return;
    startTransition(async () => {
      const { success, message } = await soldOutProductAction(product.id);
      if (success) {
        toast.success(PRODUCT_MESSAGE.SOLD_OUT_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? PRODUCT_MESSAGE.SOLD_OUT_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={product != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>상품을 품절 처리하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {product
              ? `"${product.name}" 상품이 품절 상태로 변경됩니다. 품절 해제 전용 API 는 없으며, 되돌리려면 상품 수정에서 품절 여부를 꺼야 합니다.`
              : ""}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isPending}>취소</AlertDialogCancel>
          <AlertDialogAction
            onClick={(event) => {
              event.preventDefault();
              handleSoldOut();
            }}
            disabled={isPending}
          >
            {isPending ? "처리 중..." : "품절 처리"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
