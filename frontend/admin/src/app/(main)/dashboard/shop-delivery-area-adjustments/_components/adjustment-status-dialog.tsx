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
import { updateDeliveryAreaAdjustmentStatusAction } from "@/feature/shop/actions";
import {
  DELIVERY_AREA_ADJUSTMENT_STATUS_LABEL,
  type DeliveryAreaAdjustmentTransitionOption,
} from "@/feature/shop/constants";
import type { DeliveryAreaAdjustmentListItem } from "@/feature/shop/domain";
import { DELIVERY_AREA_ADJUSTMENT_MESSAGE, SHOP_MESSAGE } from "@/feature/shop/message";

interface AdjustmentStatusDialogProps {
  request: DeliveryAreaAdjustmentListItem | null;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

/**
 * 현재 상태에서 전이 가능한 다음 상태.
 *
 * 접수 대기 → 조정 중(가맹본부 전달), 조정 중 → 조정 완료(조정 성립 기록)만 허용된다.
 * 조정 완료는 성립 사실의 기록일 뿐이며 배달가능지역이 자동 반영되지는 않는다.
 */
function nextStatusOf(status: DeliveryAreaAdjustmentListItem["status"]): DeliveryAreaAdjustmentTransitionOption | null {
  if (status === "PENDING") return "IN_PROGRESS";
  if (status === "IN_PROGRESS") return "COMPLETED";
  return null;
}

export function AdjustmentStatusDialog({ request, onOpenChange, onSuccess }: AdjustmentStatusDialogProps) {
  const [isPending, startTransition] = React.useTransition();
  const [status, setStatus] = React.useState<DeliveryAreaAdjustmentTransitionOption>("IN_PROGRESS");

  React.useEffect(() => {
    if (!request) return;
    const next = nextStatusOf(request.status);
    if (next) setStatus(next);
  }, [request]);

  const options = request
    ? ([nextStatusOf(request.status)].filter(Boolean) as DeliveryAreaAdjustmentTransitionOption[])
    : [];

  function handleSave() {
    if (!request) return;
    startTransition(async () => {
      const { success, message } = await updateDeliveryAreaAdjustmentStatusAction(request.id, { status });
      if (success) {
        toast.success(DELIVERY_AREA_ADJUSTMENT_MESSAGE.STATUS_UPDATE_SUCCESS);
        onOpenChange(false);
        onSuccess();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={request != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>처리 상태를 변경하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {request ? `"${request.shopName}" 가게의 배달지역 조정 신청 상태를 변경합니다.` : ""}
          </AlertDialogDescription>
        </AlertDialogHeader>

        <Select
          value={status}
          onValueChange={(value) => setStatus(value as DeliveryAreaAdjustmentTransitionOption)}
          disabled={isPending}
        >
          <SelectTrigger className="w-full">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectGroup>
              {options.map((option) => (
                <SelectItem key={option} value={option}>
                  {DELIVERY_AREA_ADJUSTMENT_STATUS_LABEL[option]}
                </SelectItem>
              ))}
            </SelectGroup>
          </SelectContent>
        </Select>

        <AlertDialogFooter>
          <Button type="button" variant="outline" disabled={isPending} onClick={() => onOpenChange(false)}>
            취소
          </Button>
          <Button type="button" onClick={handleSave} disabled={isPending || options.length === 0}>
            {isPending ? "처리 중..." : "저장"}
          </Button>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
