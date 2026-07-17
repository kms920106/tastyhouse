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
import { deleteOrderAction } from "@/feature/order/actions";
import type { OrderListItem } from "@/feature/order/domain";
import { ORDER_MESSAGE } from "@/feature/order/message";

interface DeleteOrderDialogProps {
  order: OrderListItem | null;
  onOpenChange: (open: boolean) => void;
}

export function DeleteOrderDialog({ order, onOpenChange }: DeleteOrderDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleDelete() {
    if (!order) return;
    startTransition(async () => {
      const { success, message } = await deleteOrderAction(order.id);
      if (success) {
        toast.success(ORDER_MESSAGE.DELETE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? ORDER_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={order != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>주문을 삭제하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {order ? `"${order.orderNumber}" 주문이 삭제되어 목록에서 제외됩니다. 이 작업은 되돌릴 수 없습니다.` : ""}
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
