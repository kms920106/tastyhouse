"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { updateShopStatusAction } from "@/feature/shop/actions";
import { SHOP_STATUS_LABEL, SHOP_STATUS_OPTIONS, type ShopStatusOption } from "@/feature/shop/constants";
import { SHOP_BASIC_COPY, SHOP_MESSAGE } from "@/feature/shop/message";
import { type ShopStatusFormValues, shopStatusSchema } from "@/feature/shop/schema";

interface ShopStatusSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  status: ShopStatusOption;
}

export function ShopStatusSheet({ open, onOpenChange, shopId, status }: ShopStatusSheetProps) {
  const [isPending, startTransition] = React.useTransition();

  const form = useForm<ShopStatusFormValues>({
    resolver: zodResolver(shopStatusSchema),
    defaultValues: { status: "OPEN" },
  });

  React.useEffect(() => {
    if (open) form.reset({ status });
  }, [open, status, form]);

  const selected = form.watch("status");

  const onSubmit = (values: ShopStatusFormValues) => {
    startTransition(async () => {
      const { success, message } = await updateShopStatusAction(shopId, values);
      if (success) {
        toast.success(SHOP_MESSAGE.STATUS_UPDATE_SUCCESS);
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
          <SheetTitle>{SHOP_BASIC_COPY.STATUS_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_BASIC_COPY.STATUS_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <form
          id="shop-status-form"
          noValidate
          onSubmit={form.handleSubmit(onSubmit)}
          className="flex-1 overflow-y-auto px-4"
        >
          <FieldGroup className="gap-4">
            <Controller
              control={form.control}
              name="status"
              render={({ field }) => (
                <RadioGroup value={field.value} onValueChange={field.onChange} disabled={isPending} className="gap-3">
                  {SHOP_STATUS_OPTIONS.map((option) => (
                    <Field key={option} orientation="horizontal" className="gap-3">
                      <RadioGroupItem id={`shop-status-${option}`} value={option} />
                      <FieldLabel htmlFor={`shop-status-${option}`} className="font-normal">
                        {SHOP_STATUS_LABEL[option]}
                      </FieldLabel>
                    </Field>
                  ))}
                </RadioGroup>
              )}
            />

            {selected === "HIDDEN" && (
              <Alert variant="destructive">
                <AlertTitle>노출정지는 즉시 적용됩니다</AlertTitle>
                <AlertDescription>
                  저장하면 가게가 즉시 목록에서 숨겨지고 신규 주문을 받을 수 없습니다.
                </AlertDescription>
              </Alert>
            )}
          </FieldGroup>
        </form>

        <SheetFooter>
          <Button type="submit" form="shop-status-form" disabled={isPending}>
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
