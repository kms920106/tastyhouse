"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { Switch } from "@/components/ui/switch";
import { updateShopScheduledOrderAction } from "@/feature/shop/actions";
import { SHOP_MESSAGE, SHOP_OPERATION_COPY } from "@/feature/shop/message";
import { type ShopScheduledOrderFormValues, shopScheduledOrderSchema } from "@/feature/shop/schema";

interface ScheduledOrderSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  enabled: boolean;
}

export function ScheduledOrderSheet({ open, onOpenChange, shopId, enabled }: ScheduledOrderSheetProps) {
  const [isPending, startTransition] = React.useTransition();

  const form = useForm<ShopScheduledOrderFormValues>({
    resolver: zodResolver(shopScheduledOrderSchema),
    defaultValues: { enabled: false },
  });

  React.useEffect(() => {
    if (open) form.reset({ enabled });
  }, [open, enabled, form]);

  const onSubmit = (values: ShopScheduledOrderFormValues) => {
    startTransition(async () => {
      const { success, message } = await updateShopScheduledOrderAction(shopId, values);
      if (success) {
        toast.success(SHOP_MESSAGE.SCHEDULED_ORDER_UPDATE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{SHOP_OPERATION_COPY.SCHEDULED_ORDER_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_OPERATION_COPY.SCHEDULED_ORDER_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <form
          id="shop-scheduled-order-form"
          noValidate
          onSubmit={form.handleSubmit(onSubmit)}
          className="flex-1 overflow-y-auto px-4"
        >
          <FieldGroup className="gap-4">
            <Controller
              control={form.control}
              name="enabled"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <div className="flex items-center justify-between gap-4">
                    <FieldLabel htmlFor="shop-scheduled-order">
                      {field.value
                        ? SHOP_OPERATION_COPY.SCHEDULED_ORDER_ON_LABEL
                        : SHOP_OPERATION_COPY.SCHEDULED_ORDER_OFF_LABEL}
                    </FieldLabel>
                    <Switch
                      id="shop-scheduled-order"
                      checked={field.value}
                      onCheckedChange={field.onChange}
                      aria-invalid={fieldState.invalid}
                      disabled={isPending}
                    />
                  </div>
                  <FieldDescription>{SHOP_OPERATION_COPY.SCHEDULED_ORDER_GUIDE}</FieldDescription>
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />
          </FieldGroup>
        </form>

        <SheetFooter>
          <Button type="submit" form="shop-scheduled-order-form" disabled={isPending}>
            {isPending ? "저장 중..." : "적용"}
          </Button>
          <SheetClose asChild>
            <Button variant="outline" disabled={isPending}>
              취소
            </Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
