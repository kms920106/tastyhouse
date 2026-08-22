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
import { hideOrderNoticeAction } from "@/feature/shop/actions";
import { SHOP_MESSAGE } from "@/feature/shop/message";
import { type OrderNoticeHideFormValues, orderNoticeHideSchema } from "@/feature/shop/schema";

interface OrderNoticeHideDialogProps {
  /** 게시중단 대상 가게 ID. null 이면 다이얼로그를 닫는다 */
  shopId: number | null;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

const EMPTY_VALUES: OrderNoticeHideFormValues = { reason: "" };

export function OrderNoticeHideDialog({ shopId, onOpenChange, onSuccess }: OrderNoticeHideDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  const form = useForm<OrderNoticeHideFormValues>({
    resolver: zodResolver(orderNoticeHideSchema),
    defaultValues: EMPTY_VALUES,
  });

  React.useEffect(() => {
    if (shopId != null) form.reset(EMPTY_VALUES);
  }, [shopId, form.reset]);

  const onSubmit = (values: OrderNoticeHideFormValues) => {
    if (shopId == null) return;
    startTransition(async () => {
      const { success, message } = await hideOrderNoticeAction(shopId, values);
      if (success) {
        toast.success(SHOP_MESSAGE.ORDER_NOTICE_HIDE_SUCCESS);
        onOpenChange(false);
        onSuccess();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  return (
    <Dialog open={shopId != null} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>주문안내를 게시중단하시겠습니까?</DialogTitle>
          <DialogDescription>사유는 점주 조회 화면에 그대로 전달됩니다.</DialogDescription>
        </DialogHeader>
        <form noValidate onSubmit={form.handleSubmit(onSubmit)}>
          <Controller
            control={form.control}
            name="reason"
            render={({ field, fieldState }) => (
              <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                <FieldLabel htmlFor="order-notice-hide-reason">게시중단 사유</FieldLabel>
                <Textarea
                  {...field}
                  id="order-notice-hide-reason"
                  rows={4}
                  disabled={isPending}
                  aria-invalid={fieldState.invalid}
                />
                {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
              </Field>
            )}
          />
          <DialogFooter className="mt-4">
            <Button type="button" variant="outline" disabled={isPending} onClick={() => onOpenChange(false)}>
              취소
            </Button>
            <Button type="submit" variant="destructive" disabled={isPending}>
              {isPending ? "처리 중..." : "게시중단"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
