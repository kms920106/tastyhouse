"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Field,
  FieldDescription,
  FieldError,
  FieldGroup,
  FieldLabel,
  FieldLegend,
  FieldSet,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { createSuspensionAction } from "@/feature/shop/actions";
import {
  ORDER_METHOD_LABEL,
  ORDER_METHOD_OPTIONS,
  type OrderMethodOption,
  SUSPENSION_REASON_LABEL,
  SUSPENSION_REASON_OPTIONS,
} from "@/feature/shop/constants";
import { SHOP_MESSAGE, SHOP_STATUS_PAGE_COPY } from "@/feature/shop/message";
import { type SuspensionFormValues, suspensionSchema } from "@/feature/shop/schema";

interface SuspensionSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** 임시중지 대상 가게 — 단건 Switch 는 1건, [전체 영업임시중지] 는 전체 목록 */
  shopIds: number[];
  targetLabel: string;
}

/** datetime-local 입력이 요구하는 "YYYY-MM-DDTHH:mm" 형식으로 변환한다. */
function toDateTimeLocal(date: Date): string {
  const pad = (value: number) => String(value).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function SuspensionSheet({ open, onOpenChange, shopIds, targetLabel }: SuspensionSheetProps) {
  const [isPending, startTransition] = React.useTransition();

  const form = useForm<SuspensionFormValues>({
    resolver: zodResolver(suspensionSchema),
    defaultValues: {
      shopIds: [],
      reason: "SHOP_CIRCUMSTANCE",
      orderMethods: [],
      startAt: "",
      endAt: "",
    },
  });

  React.useEffect(() => {
    if (!open) return;

    const now = new Date();
    const oneHourLater = new Date(now.getTime() + 60 * 60 * 1000);
    form.reset({
      shopIds,
      reason: "SHOP_CIRCUMSTANCE",
      orderMethods: [],
      startAt: toDateTimeLocal(now),
      endAt: toDateTimeLocal(oneHourLater),
    });
  }, [open, shopIds, form]);

  const onSubmit = (values: SuspensionFormValues) => {
    if (values.shopIds.length === 0) {
      toast.error(SHOP_MESSAGE.SUSPENSION_SHOP_REQUIRED);
      return;
    }

    startTransition(async () => {
      const { success, message } = await createSuspensionAction(values);
      if (success) {
        toast.success(SHOP_MESSAGE.SUSPENSION_CREATE_SUCCESS);
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
          <SheetTitle>영업 임시중지</SheetTitle>
          <SheetDescription>{targetLabel}의 영업을 지정한 기간 동안 중지합니다.</SheetDescription>
        </SheetHeader>

        <form
          id="suspension-form"
          noValidate
          onSubmit={form.handleSubmit(onSubmit)}
          className="flex-1 overflow-y-auto px-4"
        >
          <FieldGroup className="gap-5">
            <Controller
              control={form.control}
              name="reason"
              render={({ field }) => (
                <Field className="gap-1.5">
                  <FieldLabel htmlFor="suspension-reason">중지 사유</FieldLabel>
                  <Select value={field.value ?? ""} onValueChange={field.onChange} disabled={isPending}>
                    <SelectTrigger id="suspension-reason" className="w-full">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent position="popper">
                      <SelectGroup>
                        {SUSPENSION_REASON_OPTIONS.map((option) => (
                          <SelectItem key={option} value={option}>
                            {SUSPENSION_REASON_LABEL[option]}
                          </SelectItem>
                        ))}
                      </SelectGroup>
                    </SelectContent>
                  </Select>
                </Field>
              )}
            />

            <Controller
              control={form.control}
              name="orderMethods"
              render={({ field, fieldState }) => (
                <FieldSet>
                  <FieldLegend variant="label">중지할 주문유형</FieldLegend>
                  {/* 스펙상 빈 배열은 "전체 주문유형 중지"를 의미한다. */}
                  <FieldDescription>
                    선택하지 않으면 {SHOP_STATUS_PAGE_COPY.ALL_ORDER_METHODS}을 중지합니다.
                  </FieldDescription>
                  <div className="mt-2 flex flex-col gap-3">
                    {ORDER_METHOD_OPTIONS.map((option) => (
                      <Field key={option} orientation="horizontal" className="gap-3">
                        <Checkbox
                          id={`order-method-${option}`}
                          checked={field.value.includes(option)}
                          disabled={isPending}
                          onCheckedChange={(checked) => {
                            const next: OrderMethodOption[] = checked
                              ? [...field.value, option]
                              : field.value.filter((item) => item !== option);
                            field.onChange(next);
                          }}
                        />
                        <FieldLabel htmlFor={`order-method-${option}`} className="font-normal">
                          {ORDER_METHOD_LABEL[option]}
                        </FieldLabel>
                      </Field>
                    ))}
                  </div>
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </FieldSet>
              )}
            />

            <Controller
              control={form.control}
              name="startAt"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="suspension-start-at">시작 일시</FieldLabel>
                  <Input
                    {...field}
                    id="suspension-start-at"
                    type="datetime-local"
                    disabled={isPending}
                    aria-invalid={fieldState.invalid}
                  />
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />

            <Controller
              control={form.control}
              name="endAt"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="suspension-end-at">종료 일시</FieldLabel>
                  <Input
                    {...field}
                    id="suspension-end-at"
                    type="datetime-local"
                    disabled={isPending}
                    aria-invalid={fieldState.invalid}
                  />
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />
          </FieldGroup>
        </form>

        <SheetFooter>
          <Button type="submit" form="suspension-form" disabled={isPending}>
            {isPending ? "적용 중..." : "적용"}
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
