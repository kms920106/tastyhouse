"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
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
import { Skeleton } from "@/components/ui/skeleton";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import { createCouponAction, fetchCouponAction, updateCouponAction } from "@/feature/coupon/actions";
import type { CouponListItem } from "@/feature/coupon/domain";
import { COUPON_MESSAGE } from "@/feature/coupon/message";
import { COUPON_DESC_MAX, COUPON_NAME_MAX, type CouponFormValues, couponFormSchema } from "@/feature/coupon/schema";

interface CouponFormSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  coupon?: Pick<CouponListItem, "id"> | null;
}

const EMPTY_VALUES: CouponFormValues = {
  name: "",
  description: undefined,
  discountType: "AMOUNT",
  discountAmount: 1,
  maxDiscountAmount: undefined,
  minOrderAmount: 0,
  maxDiscountCount: undefined,
  issueStartAt: "",
  issueEndAt: "",
  useStartAt: "",
  useEndAt: "",
  visible: true,
};

/** "YYYY-MM-DDTHH:mm:ss" (LocalDateTime) -> "YYYY-MM-DDTHH:mm" (datetime-local) */
function toDateTimeLocal(value: string | null | undefined): string {
  if (!value) return "";
  return value.slice(0, 16);
}

/** 숫자 input onChange: 빈 값이면 undefined, 아니면 Number */
function parseOptionalNumber(value: string): number | undefined {
  return value.trim() === "" ? undefined : Number(value);
}

export function CouponFormSheet({ open, onOpenChange, coupon }: CouponFormSheetProps) {
  const isEdit = Boolean(coupon);
  const [isPending, startTransition] = React.useTransition();
  const [isLoadingDetail, setIsLoadingDetail] = React.useState(false);

  const form = useForm<CouponFormValues>({
    resolver: zodResolver(couponFormSchema),
    defaultValues: EMPTY_VALUES,
  });

  // 최대 할인 금액은 정률(RATE) 상한 개념 — 정액(AMOUNT)에서는 비활성화한다.
  const discountType = form.watch("discountType");
  const isRate = discountType === "RATE";

  // 시트가 열릴 때마다 대상 값으로 초기화한다. 수정 모드는 상세를 조회해 값을 확보한다.
  React.useEffect(() => {
    if (!open) return;

    if (!coupon) {
      form.reset(EMPTY_VALUES);
      return;
    }

    let active = true;
    setIsLoadingDetail(true);

    void fetchCouponAction(coupon.id).then((result) => {
      if (!active) return;
      setIsLoadingDetail(false);

      if (!result.success || !result.data) {
        toast.error(result.message ?? COUPON_MESSAGE.DETAIL_LOAD_FAILED);
        onOpenChange(false);
        return;
      }

      const detail = result.data;
      form.reset({
        name: detail.name,
        description: detail.description ?? undefined,
        discountType: detail.discountType,
        discountAmount: detail.discountAmount,
        maxDiscountAmount: detail.maxDiscountAmount ?? undefined,
        minOrderAmount: detail.minOrderAmount,
        maxDiscountCount: detail.maxDiscountCount ?? undefined,
        issueStartAt: toDateTimeLocal(detail.issueStartAt),
        issueEndAt: toDateTimeLocal(detail.issueEndAt),
        useStartAt: toDateTimeLocal(detail.useStartAt),
        useEndAt: toDateTimeLocal(detail.useEndAt),
        visible: detail.visible,
      });
    });

    return () => {
      active = false;
    };
  }, [open, coupon, form.reset, onOpenChange]);

  const onSubmit = (values: CouponFormValues) => {
    startTransition(async () => {
      const { success, message } = coupon
        ? await updateCouponAction(coupon.id, values)
        : await createCouponAction(values);

      if (success) {
        toast.success(isEdit ? COUPON_MESSAGE.UPDATE_SUCCESS : COUPON_MESSAGE.CREATE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? COUPON_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  const busy = isPending || isLoadingDetail;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{isEdit ? "쿠폰 수정" : "쿠폰 등록"}</SheetTitle>
          <SheetDescription>{isEdit ? "쿠폰 정보를 수정합니다." : "새로운 쿠폰을 등록합니다."}</SheetDescription>
        </SheetHeader>

        {isLoadingDetail ? (
          <div className="flex-1 space-y-3 px-4">
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
          </div>
        ) : (
          <form
            id="coupon-form"
            noValidate
            onSubmit={form.handleSubmit(onSubmit)}
            className="flex-1 overflow-y-auto px-4"
          >
            <FieldGroup className="gap-4">
              <Controller
                control={form.control}
                name="name"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="coupon-name">쿠폰 이름</FieldLabel>
                    <Input
                      {...field}
                      id="coupon-name"
                      placeholder="쿠폰 이름을 입력하세요"
                      maxLength={COUPON_NAME_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="description"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="coupon-description">설명</FieldLabel>
                    <Textarea
                      {...field}
                      value={field.value ?? ""}
                      id="coupon-description"
                      placeholder="쿠폰 설명을 입력하세요 (선택)"
                      maxLength={COUPON_DESC_MAX}
                      rows={3}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="discountType"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="coupon-discount-type">할인 유형</FieldLabel>
                    <Select
                      value={field.value}
                      onValueChange={(value) => {
                        field.onChange(value);
                        // 정액(AMOUNT) 전환 시 정률 전용 필드(최대 할인 금액)를 정리한다.
                        if (value === "AMOUNT") {
                          form.setValue("maxDiscountAmount", undefined, { shouldValidate: true });
                        }
                      }}
                      disabled={busy}
                    >
                      <SelectTrigger id="coupon-discount-type" className="w-full" aria-invalid={fieldState.invalid}>
                        <SelectValue placeholder="유형 선택" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectGroup>
                          <SelectItem value="AMOUNT">정액 (원)</SelectItem>
                          <SelectItem value="RATE">정률 (%)</SelectItem>
                        </SelectGroup>
                      </SelectContent>
                    </Select>
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="discountAmount"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="coupon-discount-amount">할인 값 (정액=원 / 정률=%)</FieldLabel>
                    <Input
                      id="coupon-discount-amount"
                      type="number"
                      min={1}
                      value={field.value}
                      onChange={(e) => field.onChange(Number(e.target.value))}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="maxDiscountAmount"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="coupon-max-discount-amount">
                      최대 할인 금액 (정률 상한, 미입력=무제한)
                    </FieldLabel>
                    <Input
                      id="coupon-max-discount-amount"
                      type="number"
                      min={0}
                      value={field.value ?? ""}
                      onChange={(e) => field.onChange(parseOptionalNumber(e.target.value))}
                      aria-invalid={fieldState.invalid}
                      disabled={busy || !isRate}
                      placeholder={isRate ? undefined : "정률 할인에만 적용됩니다"}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="minOrderAmount"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="coupon-min-order-amount">최소 주문 금액</FieldLabel>
                    <Input
                      id="coupon-min-order-amount"
                      type="number"
                      min={0}
                      value={field.value}
                      onChange={(e) => field.onChange(Number(e.target.value))}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="maxDiscountCount"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="coupon-max-discount-count">최대 발급 수량 (미입력=무제한)</FieldLabel>
                    <Input
                      id="coupon-max-discount-count"
                      type="number"
                      min={0}
                      value={field.value ?? ""}
                      onChange={(e) => field.onChange(parseOptionalNumber(e.target.value))}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="issueStartAt"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="coupon-issue-start">발급 시작 일시</FieldLabel>
                    <Input
                      {...field}
                      value={field.value ?? ""}
                      id="coupon-issue-start"
                      type="datetime-local"
                      step={1}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="issueEndAt"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="coupon-issue-end">발급 종료 일시</FieldLabel>
                    <Input
                      {...field}
                      value={field.value ?? ""}
                      id="coupon-issue-end"
                      type="datetime-local"
                      step={1}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="useStartAt"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="coupon-use-start">사용 시작 일시</FieldLabel>
                    <Input
                      {...field}
                      value={field.value ?? ""}
                      id="coupon-use-start"
                      type="datetime-local"
                      step={1}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="useEndAt"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="coupon-use-end">사용 종료 일시</FieldLabel>
                    <Input
                      {...field}
                      value={field.value ?? ""}
                      id="coupon-use-end"
                      type="datetime-local"
                      step={1}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="visible"
                render={({ field }) => (
                  <Field orientation="horizontal">
                    <FieldLabel htmlFor="coupon-visible">노출 여부</FieldLabel>
                    <Switch
                      id="coupon-visible"
                      checked={field.value}
                      onCheckedChange={field.onChange}
                      disabled={busy}
                    />
                  </Field>
                )}
              />
            </FieldGroup>
          </form>
        )}

        <SheetFooter>
          <Button type="submit" form="coupon-form" disabled={busy}>
            {isPending ? "저장 중..." : isEdit ? "수정" : "등록"}
          </Button>
          <SheetClose asChild>
            <Button variant="outline" disabled={busy}>
              취소
            </Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
