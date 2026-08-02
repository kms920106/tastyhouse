"use client";

import * as React from "react";

import { toast } from "sonner";

import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { updateOrderStatusAction } from "@/feature/order/actions";
import { ORDER_STATUS_OPTIONS } from "@/feature/order/constants";
import type { OrderListItem, OrderStatus } from "@/feature/order/domain";
import { ORDER_MESSAGE } from "@/feature/order/message";

interface OrderStatusDialogProps {
  order: OrderListItem | null;
  onOpenChange: (open: boolean) => void;
}

export function OrderStatusDialog({ order, onOpenChange }: OrderStatusDialogProps) {
  const [isPending, startTransition] = React.useTransition();
  const [status, setStatus] = React.useState<OrderStatus>(order?.orderStatus ?? "PENDING");

  React.useEffect(() => {
    if (order) {
      setStatus(order.orderStatus);
    }
  }, [order]);

  function handleSave() {
    if (!order) return;
    startTransition(async () => {
      const { success, message } = await updateOrderStatusAction(order.id, { status });
      if (success) {
        toast.success(ORDER_MESSAGE.STATUS_UPDATE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? ORDER_MESSAGE.STATUS_UPDATE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={order != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>주문 상태를 변경하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {order ? `"${order.orderNumber}" 주문의 상태를 변경합니다. 상태 전이 검증 없이 즉시 반영됩니다.` : ""}
          </AlertDialogDescription>
        </AlertDialogHeader>

        <Select value={status} onValueChange={(value) => setStatus(value as OrderStatus)} disabled={isPending}>
          <SelectTrigger className="w-full">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectGroup>
              {ORDER_STATUS_OPTIONS.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectGroup>
          </SelectContent>
        </Select>

        <AlertDialogFooter>
          <Button type="button" variant="outline" disabled={isPending} onClick={() => onOpenChange(false)}>
            취소
          </Button>
          <Button type="button" onClick={handleSave} disabled={isPending}>
            {isPending ? "처리 중..." : "저장"}
          </Button>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
