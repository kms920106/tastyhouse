"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldLabel } from "@/components/ui/field";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { Textarea } from "@/components/ui/textarea";
import { createBlindRequestAction } from "@/feature/shop-review/actions";
import { BLIND_DETAIL_REASON_MAX_LENGTH, BLIND_REASON_ETC } from "@/feature/shop-review/constants";
import type { ReviewBlindReasonOption } from "@/feature/shop-review/domain";
import { SHOP_REVIEW_COPY } from "@/feature/shop-review/message";
import { type BlindRequestFormValues, blindRequestSchema } from "@/feature/shop-review/schema";

interface BlindRequestSheetProps {
  shopId: number;
  reviewId: number;
  /** 서버 카탈로그. 비어 있으면 사유를 고를 수 없어 안내만 띄운다 */
  blindReasons: ReviewBlindReasonOption[];
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

/**
 * 게시중단 요청 폼.
 *
 * 사유가 `ETC` 면 상세 사유가 필수다(`blindRequestSchema` 의 `superRefine`). 서버도 같은 규칙으로
 * `REVIEW_BLIND_DETAIL_REASON_REQUIRED` 를 내므로, 여기서 막는 것은 왕복을 아끼기 위한 1차 방어다.
 */
export function BlindRequestSheet({ shopId, reviewId, blindReasons, open, onOpenChange }: BlindRequestSheetProps) {
  const [isPending, startTransition] = React.useTransition();

  const form = useForm<BlindRequestFormValues>({
    resolver: zodResolver(blindRequestSchema),
    defaultValues: { reason: undefined, detailReason: "" },
  });

  // 시트를 다시 열 때 이전 입력이 남아 있지 않게 한다.
  React.useEffect(() => {
    if (open) form.reset({ reason: undefined, detailReason: "" });
  }, [open, form.reset]);

  const selectedReason = form.watch("reason");
  const isDetailRequired = selectedReason === BLIND_REASON_ETC;

  const onSubmit = (values: BlindRequestFormValues) => {
    startTransition(async () => {
      const { success, message } = await createBlindRequestAction(shopId, reviewId, values);
      if (success) {
        toast.success(SHOP_REVIEW_COPY.BLIND_REQUEST_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? SHOP_REVIEW_COPY.BLIND_REQUEST_FAILED);
      }
    });
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex flex-col gap-0 overflow-y-auto">
        <SheetHeader>
          <SheetTitle>{SHOP_REVIEW_COPY.BLIND_REQUEST_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_REVIEW_COPY.BLIND_REQUEST_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex flex-col gap-4 px-4 pb-6">
          {blindReasons.length === 0 ? (
            <p className="text-destructive text-sm">{SHOP_REVIEW_COPY.BLIND_REASON_LOAD_FAILED}</p>
          ) : (
            <form className="flex flex-col gap-4" noValidate onSubmit={form.handleSubmit(onSubmit)}>
              <Controller
                control={form.control}
                name="reason"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="blind-request-reason">
                      {SHOP_REVIEW_COPY.BLIND_REQUEST_REASON_LABEL}
                    </FieldLabel>
                    {/* Radix Select 의 value 는 항상 안정 문자열이어야 한다(undefined 금지) */}
                    <Select value={field.value ?? ""} onValueChange={field.onChange} disabled={isPending}>
                      <SelectTrigger id="blind-request-reason" className="w-full" aria-invalid={fieldState.invalid}>
                        <SelectValue placeholder={SHOP_REVIEW_COPY.BLIND_REQUEST_REASON_PLACEHOLDER} />
                      </SelectTrigger>
                      <SelectContent position="popper" align="start">
                        <SelectGroup>
                          {blindReasons.map((reason) => (
                            <SelectItem key={reason.code} value={reason.code}>
                              {reason.description}
                            </SelectItem>
                          ))}
                        </SelectGroup>
                      </SelectContent>
                    </Select>
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="detailReason"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="blind-request-detail-reason">
                      {SHOP_REVIEW_COPY.BLIND_REQUEST_DETAIL_LABEL}
                    </FieldLabel>
                    <Textarea
                      {...field}
                      value={field.value ?? ""}
                      id="blind-request-detail-reason"
                      placeholder={SHOP_REVIEW_COPY.BLIND_REQUEST_DETAIL_PLACEHOLDER}
                      maxLength={BLIND_DETAIL_REASON_MAX_LENGTH}
                      aria-invalid={fieldState.invalid}
                      aria-required={isDetailRequired}
                      disabled={isPending}
                      rows={4}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Button type="submit" className="self-end" disabled={isPending}>
                {SHOP_REVIEW_COPY.BLIND_REQUEST_SUBMIT}
              </Button>
            </form>
          )}
        </div>
      </SheetContent>
    </Sheet>
  );
}
