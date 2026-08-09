"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Field, FieldError, FieldLabel } from "@/components/ui/field";
import { Textarea } from "@/components/ui/textarea";
import { rejectDeliveryAreaAdjustmentAction } from "@/feature/shop/actions";
import { REJECT_REASON_MAX } from "@/feature/shop/constants";
import type { DeliveryAreaAdjustmentListItem } from "@/feature/shop/domain";
import { DELIVERY_AREA_ADJUSTMENT_MESSAGE, SHOP_MESSAGE } from "@/feature/shop/message";
import { type DeliveryAreaAdjustmentRejectFormValues, deliveryAreaAdjustmentRejectSchema } from "@/feature/shop/schema";

interface AdjustmentRejectDialogProps {
  request: DeliveryAreaAdjustmentListItem | null;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

const EMPTY_VALUES: DeliveryAreaAdjustmentRejectFormValues = { reason: "" };

export function AdjustmentRejectDialog({ request, onOpenChange, onSuccess }: AdjustmentRejectDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  const form = useForm<DeliveryAreaAdjustmentRejectFormValues>({
    resolver: zodResolver(deliveryAreaAdjustmentRejectSchema),
    defaultValues: EMPTY_VALUES,
  });

  React.useEffect(() => {
    if (request) form.reset(EMPTY_VALUES);
  }, [request, form.reset]);

  const onSubmit = (values: DeliveryAreaAdjustmentRejectFormValues) => {
    if (!request) return;
    startTransition(async () => {
      const { success, message } = await rejectDeliveryAreaAdjustmentAction(request.id, values);
      if (success) {
        toast.success(DELIVERY_AREA_ADJUSTMENT_MESSAGE.REJECT_SUCCESS);
        onOpenChange(false);
        onSuccess();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  return (
    <Dialog open={request != null} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>배달지역 조정 신청 반려</DialogTitle>
          <DialogDescription>반려 사유를 입력해 주세요. 점주에게 그대로 노출됩니다.</DialogDescription>
        </DialogHeader>
        <form id="delivery-area-adjustment-reject-form" noValidate onSubmit={form.handleSubmit(onSubmit)}>
          <Controller
            control={form.control}
            name="reason"
            render={({ field, fieldState }) => (
              <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                <FieldLabel htmlFor="delivery-area-adjustment-reject-reason">반려 사유</FieldLabel>
                <Textarea
                  {...field}
                  id="delivery-area-adjustment-reject-reason"
                  placeholder="반려 사유를 입력하세요"
                  maxLength={REJECT_REASON_MAX}
                  aria-invalid={fieldState.invalid}
                  disabled={isPending}
                  rows={4}
                />
                {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
              </Field>
            )}
          />
        </form>
        <DialogFooter>
          <Button type="submit" form="delivery-area-adjustment-reject-form" disabled={isPending}>
            {isPending ? "처리 중..." : "반려"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
