"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { updateShopMinOrderAmountAction } from "@/feature/shop/actions";
import { MIN_ORDER_AMOUNT_STEP, MIN_ORDER_AMOUNT_UNSET, MIN_ORDER_AMOUNT_UPPER_BOUND } from "@/feature/shop/constants";
import { SHOP_MESSAGE, SHOP_OPERATION_COPY } from "@/feature/shop/message";
import { type ShopMinOrderAmountFormValues, shopMinOrderAmountSchema } from "@/feature/shop/schema";

interface MinOrderAmountSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  minOrderAmount: number;
}

export function MinOrderAmountSheet({ open, onOpenChange, shopId, minOrderAmount }: MinOrderAmountSheetProps) {
  const [isPending, startTransition] = React.useTransition();

  const form = useForm<ShopMinOrderAmountFormValues>({
    resolver: zodResolver(shopMinOrderAmountSchema),
    defaultValues: { minOrderAmount: MIN_ORDER_AMOUNT_UNSET },
  });

  React.useEffect(() => {
    if (open) form.reset({ minOrderAmount });
  }, [open, minOrderAmount, form]);

  const onSubmit = (values: ShopMinOrderAmountFormValues) => {
    startTransition(async () => {
      const { success, message } = await updateShopMinOrderAmountAction(shopId, values);
      if (success) {
        toast.success(SHOP_MESSAGE.MIN_ORDER_AMOUNT_UPDATE_SUCCESS);
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
          <SheetTitle>{SHOP_OPERATION_COPY.MIN_ORDER_AMOUNT_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_OPERATION_COPY.MIN_ORDER_AMOUNT_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <form
          id="shop-min-order-amount-form"
          noValidate
          onSubmit={form.handleSubmit(onSubmit)}
          className="flex-1 overflow-y-auto px-4"
        >
          <FieldGroup className="gap-4">
            <Controller
              control={form.control}
              name="minOrderAmount"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="shop-min-order-amount">{SHOP_OPERATION_COPY.MIN_ORDER_AMOUNT_TITLE}</FieldLabel>
                  <Input
                    id="shop-min-order-amount"
                    type="number"
                    inputMode="numeric"
                    min={MIN_ORDER_AMOUNT_UNSET}
                    max={MIN_ORDER_AMOUNT_UPPER_BOUND}
                    step={MIN_ORDER_AMOUNT_STEP}
                    value={field.value ?? ""}
                    onChange={(e) => field.onChange(e.target.value === "" ? undefined : Number(e.target.value))}
                    aria-invalid={fieldState.invalid}
                    disabled={isPending}
                  />
                  <FieldDescription>{SHOP_OPERATION_COPY.MIN_ORDER_AMOUNT_GUIDE}</FieldDescription>
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />
          </FieldGroup>
        </form>

        <SheetFooter>
          <Button type="submit" form="shop-min-order-amount-form" disabled={isPending}>
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
