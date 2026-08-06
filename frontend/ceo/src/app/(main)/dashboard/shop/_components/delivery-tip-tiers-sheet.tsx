"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Trash2 } from "lucide-react";
import { Controller, useFieldArray, useForm } from "react-hook-form";
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
import { updateDeliveryTipTiersAction } from "@/feature/shop/actions";
import { DELIVERY_TIP_TIER_MAX_COUNT, DELIVERY_TIP_UPPER_BOUND_EXCLUSIVE } from "@/feature/shop/constants";
import type { ShopDeliveryTipTier } from "@/feature/shop/domain";
import { SHOP_MESSAGE, SHOP_OPERATION_COPY } from "@/feature/shop/message";
import { type DeliveryTipTiersFormValues, deliveryTipTiersSchema } from "@/feature/shop/schema";

interface DeliveryTipTiersSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  tiers: ShopDeliveryTipTier[];
}

/** 첫 구간 기본값 — PDF 예시(5,000원 이상 주문 시 2,000원)를 초기값으로 제안한다 */
const FIRST_TIER_DEFAULT = { minOrderAmount: 5000, tipAmount: 2000 };
/** 행 추가 시 직전 행에서 증감할 폭 — 단조성(금액↑·팁↓) 위반을 애초에 줄인다 */
const NEXT_TIER_ORDER_AMOUNT_STEP = 5000;
const NEXT_TIER_TIP_STEP = 500;

export function DeliveryTipTiersSheet({ open, onOpenChange, shopId, tiers }: DeliveryTipTiersSheetProps) {
  const [isPending, startTransition] = React.useTransition();

  const form = useForm<DeliveryTipTiersFormValues>({
    resolver: zodResolver(deliveryTipTiersSchema),
    defaultValues: { tiers: [FIRST_TIER_DEFAULT] },
  });
  const { fields, append, remove } = useFieldArray({ control: form.control, name: "tiers" });

  React.useEffect(() => {
    if (!open) return;
    form.reset({
      tiers:
        tiers.length > 0
          ? tiers.map((tier) => ({ minOrderAmount: tier.minOrderAmount, tipAmount: tier.tipAmount }))
          : [FIRST_TIER_DEFAULT],
    });
  }, [open, tiers, form]);

  const canAppend = fields.length < DELIVERY_TIP_TIER_MAX_COUNT;

  // 직전 행보다 큰 주문금액·작은 배달팁을 프리필한다 (배달팁은 0 미만으로 내려가지 않게 막는다)
  const appendNextTier = () => {
    const previous = form.getValues("tiers").at(-1);
    append({
      minOrderAmount: (previous?.minOrderAmount ?? 0) + NEXT_TIER_ORDER_AMOUNT_STEP,
      tipAmount: Math.max((previous?.tipAmount ?? NEXT_TIER_TIP_STEP) - NEXT_TIER_TIP_STEP, 0),
    });
  };

  const onSubmit = (values: DeliveryTipTiersFormValues) => {
    startTransition(async () => {
      const { success, message } = await updateDeliveryTipTiersAction(shopId, values);
      if (success) {
        toast.success(SHOP_MESSAGE.DELIVERY_TIP_UPDATE_SUCCESS);
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
          <SheetTitle>{SHOP_OPERATION_COPY.DELIVERY_TIP_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_OPERATION_COPY.DELIVERY_TIP_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <form
          id="shop-delivery-tip-tiers-form"
          noValidate
          onSubmit={form.handleSubmit(onSubmit)}
          className="flex-1 overflow-y-auto px-4"
        >
          <FieldGroup className="gap-4">
            {fields.map((field, index) => (
              <div key={field.id} className="flex flex-col gap-3 rounded-md border p-3">
                <div className="flex items-center justify-between">
                  <span className="font-medium text-sm">{index === 0 ? "기본 구간" : `추가 구간 ${index}`}</span>
                  {index > 0 && (
                    <Button
                      type="button"
                      size="icon"
                      variant="ghost"
                      onClick={() => remove(index)}
                      disabled={isPending}
                      aria-label={`추가 구간 ${index} 삭제`}
                    >
                      <Trash2 className="size-4" />
                    </Button>
                  )}
                </div>

                <Controller
                  control={form.control}
                  name={`tiers.${index}.minOrderAmount`}
                  render={({ field: amountField, fieldState }) => (
                    <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor={`delivery-tip-tier-min-order-${index}`}>주문금액 (이상)</FieldLabel>
                      <Input
                        id={`delivery-tip-tier-min-order-${index}`}
                        type="number"
                        inputMode="numeric"
                        min={0}
                        value={amountField.value ?? ""}
                        onChange={(e) =>
                          amountField.onChange(e.target.value === "" ? undefined : Number(e.target.value))
                        }
                        aria-invalid={fieldState.invalid}
                        disabled={isPending}
                      />
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                  )}
                />

                <Controller
                  control={form.control}
                  name={`tiers.${index}.tipAmount`}
                  render={({ field: tipField, fieldState }) => (
                    <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor={`delivery-tip-tier-tip-${index}`}>배달팁</FieldLabel>
                      <Input
                        id={`delivery-tip-tier-tip-${index}`}
                        type="number"
                        inputMode="numeric"
                        min={0}
                        max={DELIVERY_TIP_UPPER_BOUND_EXCLUSIVE - 1}
                        value={tipField.value ?? ""}
                        onChange={(e) => tipField.onChange(e.target.value === "" ? undefined : Number(e.target.value))}
                        aria-invalid={fieldState.invalid}
                        disabled={isPending}
                      />
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                  )}
                />
              </div>
            ))}

            {/* 배열 전체 불변식(단조성·개수) 위반은 행이 아닌 배열에 붙으므로 따로 노출한다 */}
            {form.formState.errors.tiers?.root && <FieldError errors={[form.formState.errors.tiers.root]} />}
            {form.formState.errors.tiers?.message && (
              <FieldError errors={[{ message: form.formState.errors.tiers.message }]} />
            )}

            <Button type="button" variant="outline" onClick={appendNextTier} disabled={!canAppend || isPending}>
              <Plus className="size-4" />
              구간 추가
            </Button>

            <FieldDescription>{SHOP_OPERATION_COPY.DELIVERY_TIP_GUIDE}</FieldDescription>
          </FieldGroup>
        </form>

        <SheetFooter>
          <Button type="submit" form="shop-delivery-tip-tiers-form" disabled={isPending}>
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
