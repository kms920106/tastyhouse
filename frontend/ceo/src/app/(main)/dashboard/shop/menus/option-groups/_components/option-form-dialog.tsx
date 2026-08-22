"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Switch } from "@/components/ui/switch";
import { CUP_COUNT_MAX, CUP_COUNT_MIN, CUP_DEPOSIT_PER_CUP, OPTION_GROUP_TYPES } from "@/feature/product/constants";
import type { MenuOption, ProductOptionGroupType } from "@/feature/product/domain";
import { OPTION_GROUP_SCREEN_COPY, PRODUCT_OPTION_GROUP_COPY } from "@/feature/product/message";
import { type OptionFormValues, optionFormSchema } from "@/feature/product/schema";

/** 옵션 저장 페이로드. 보증금 관련 값은 일반 옵션에서 `null` 이다(서버가 값이 오면 거부한다) */
export interface OptionSubmitValues {
  name: string;
  additionalPrice: number;
  cupCount: number | null;
  personalCupDiscountAmount: number | null;
}

interface OptionFormDialogProps {
  open: boolean;
  /** 지정하면 수정, 없으면 추가 */
  option?: MenuOption;
  /**
   * 소속 옵션그룹의 유형.
   *
   * 사용자가 고르는 값이 아니라 다이얼로그가 주입하는 컨텍스트다 — 옵션은 그룹 안에서만 만들어지고
   * 유형에 따라 요구 필드가 완전히 갈린다.
   */
  groupType: ProductOptionGroupType;
  pending?: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (values: OptionSubmitValues) => void;
}

const FORM_ID = "option-form";

const DEFAULT_VALUES: OptionFormValues = {
  name: "",
  additionalPrice: "",
  groupType: OPTION_GROUP_TYPES.NORMAL,
  personalCup: false,
  cupCount: "",
  personalCupDiscountAmount: "",
};

/** 빈 문자열은 "미입력"이라 `Number("")` 가 0 이 되는 것에 기대지 않는다 */
function toNullableNumber(value: string): number | null {
  const trimmed = value.trim();
  return trimmed === "" ? null : Number(trimmed);
}

export function OptionFormDialog({ open, option, groupType, pending, onOpenChange, onSubmit }: OptionFormDialogProps) {
  const form = useForm<OptionFormValues>({
    resolver: zodResolver(optionFormSchema),
    defaultValues: DEFAULT_VALUES,
  });

  const { reset, setValue, watch } = form;

  // 그룹마다 [옵션 추가]/[옵션 수정]이 같은 다이얼로그를 공유하므로 열릴 때마다 대상 값으로 되돌린다.
  React.useEffect(() => {
    if (!open) return;

    if (!option) {
      reset({ ...DEFAULT_VALUES, groupType });
      return;
    }

    // 개인컵 옵션은 별 플래그가 없다 — 할인 금액이 채워져 있는 것으로 판별한다(서버 모델도 그렇다).
    reset({
      name: option.name,
      additionalPrice: String(option.additionalPrice),
      groupType,
      personalCup: option.personalCupDiscountAmount !== null,
      cupCount: option.cupCount === null ? "" : String(option.cupCount),
      personalCupDiscountAmount:
        option.personalCupDiscountAmount === null ? "" : String(option.personalCupDiscountAmount),
    });
  }, [open, option, groupType, reset]);

  const isCupDeposit = groupType === OPTION_GROUP_TYPES.CUP_DEPOSIT;
  // `pending` 은 optional prop 이라 `boolean | undefined` 다 — 아래 `||` 조합에 그대로 쓰면
  // 린터가 nullish 병합으로 오인하므로 한 번만 boolean 으로 좁혀 둔다.
  const isPending = pending === true;
  const isPersonalCup = watch("personalCup");
  const cupCount = watch("cupCount");

  /**
   * 개인컵 전환 시 반대쪽 필드를 비운다.
   *
   * 개인컵 옵션은 컵을 제공하지 않아 보증금이 0 이고, 보증금 옵션은 할인 대상이 아니다 —
   * 두 값이 동시에 채워진 요청은 서버가 거부하므로 화면에서 애초에 만들 수 없게 한다.
   */
  function handlePersonalCupChange(next: boolean) {
    setValue("personalCup", next, { shouldValidate: false });
    setValue(next ? "cupCount" : "personalCupDiscountAmount", "", { shouldValidate: false });
  }

  const handleSubmit = (values: OptionFormValues) => {
    // 추가 금액은 생략 가능하고, 그때는 0원으로 본다(스키마가 빈 문자열을 허용한다).
    const trimmedPrice = values.additionalPrice.trim();

    onSubmit({
      name: values.name,
      additionalPrice: trimmedPrice === "" ? 0 : Number(trimmedPrice),
      cupCount: toNullableNumber(values.cupCount),
      personalCupDiscountAmount: toNullableNumber(values.personalCupDiscountAmount),
    });
  };

  /**
   * 보증금 미리보기.
   *
   * **표시 전용 계산이다.** 실제 금액은 서버가 `cupCount × 정책 요율` 로 확정하므로
   * 요율이 바뀌면 서버가 먼저 바뀌고 이 값은 안내 문구일 뿐이다.
   */
  const parsedCupCount = toNullableNumber(cupCount);
  const depositPreview =
    parsedCupCount !== null &&
    Number.isInteger(parsedCupCount) &&
    parsedCupCount >= CUP_COUNT_MIN &&
    parsedCupCount <= CUP_COUNT_MAX
      ? parsedCupCount * CUP_DEPOSIT_PER_CUP
      : null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>
            {option
              ? PRODUCT_OPTION_GROUP_COPY.DIALOG_OPTION_EDIT_TITLE
              : PRODUCT_OPTION_GROUP_COPY.DIALOG_OPTION_CREATE_TITLE}
          </DialogTitle>
          <DialogDescription>{PRODUCT_OPTION_GROUP_COPY.HELP_SELECT_RANGE}</DialogDescription>
        </DialogHeader>

        <form id={FORM_ID} noValidate onSubmit={form.handleSubmit(handleSubmit)}>
          <FieldGroup className="gap-4">
            <Controller
              control={form.control}
              name="name"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="option-name">{PRODUCT_OPTION_GROUP_COPY.FIELD_OPTION_NAME}</FieldLabel>
                  <Input
                    id="option-name"
                    value={field.value}
                    onChange={field.onChange}
                    aria-invalid={fieldState.invalid}
                    disabled={isPending}
                  />
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />

            <Controller
              control={form.control}
              name="additionalPrice"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="option-additional-price">
                    {PRODUCT_OPTION_GROUP_COPY.FIELD_ADDITIONAL_PRICE}
                  </FieldLabel>
                  {/* 보증금과 추가금을 섞으면 비과세 분리가 무너져 서버가 0 만 받는다. */}
                  <Input
                    id="option-additional-price"
                    type="number"
                    min={0}
                    inputMode="numeric"
                    value={isCupDeposit ? "0" : field.value}
                    onChange={field.onChange}
                    aria-invalid={fieldState.invalid}
                    disabled={isPending || isCupDeposit}
                  />
                  {isCupDeposit && (
                    <FieldDescription>{PRODUCT_OPTION_GROUP_COPY.NOTICE_DEPOSIT_ADDITIONAL_PRICE}</FieldDescription>
                  )}
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />

            {/* 일반 그룹의 옵션에는 보증금 입력을 렌더링하지 않는다 — 값이 가면 서버가 거부한다. */}
            {isCupDeposit && (
              <>
                <Controller
                  control={form.control}
                  name="personalCup"
                  render={({ field, fieldState }) => (
                    <Field orientation="horizontal" className="gap-3" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor="option-personal-cup" className="flex-1">
                        {PRODUCT_OPTION_GROUP_COPY.FIELD_PERSONAL_CUP}
                        <FieldDescription>{PRODUCT_OPTION_GROUP_COPY.HELP_PERSONAL_CUP}</FieldDescription>
                      </FieldLabel>
                      <Switch
                        id="option-personal-cup"
                        checked={field.value}
                        onCheckedChange={handlePersonalCupChange}
                        disabled={isPending}
                      />
                    </Field>
                  )}
                />

                {isPersonalCup ? (
                  <Controller
                    control={form.control}
                    name="personalCupDiscountAmount"
                    render={({ field, fieldState }) => (
                      <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                        <FieldLabel htmlFor="option-personal-cup-discount">
                          {PRODUCT_OPTION_GROUP_COPY.FIELD_PERSONAL_CUP_DISCOUNT}
                        </FieldLabel>
                        <Input
                          id="option-personal-cup-discount"
                          type="number"
                          min={0}
                          inputMode="numeric"
                          value={field.value}
                          onChange={field.onChange}
                          aria-invalid={fieldState.invalid}
                          disabled={isPending}
                        />
                        {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                      </Field>
                    )}
                  />
                ) : (
                  <Controller
                    control={form.control}
                    name="cupCount"
                    render={({ field, fieldState }) => (
                      <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                        <FieldLabel htmlFor="option-cup-count">{PRODUCT_OPTION_GROUP_COPY.FIELD_CUP_COUNT}</FieldLabel>
                        <Input
                          id="option-cup-count"
                          type="number"
                          min={CUP_COUNT_MIN}
                          max={CUP_COUNT_MAX}
                          inputMode="numeric"
                          value={field.value}
                          onChange={field.onChange}
                          aria-invalid={fieldState.invalid}
                          disabled={isPending}
                        />
                        <FieldDescription>{PRODUCT_OPTION_GROUP_COPY.HELP_CUP_COUNT}</FieldDescription>
                        {depositPreview !== null && (
                          <FieldDescription>
                            {PRODUCT_OPTION_GROUP_COPY.DEPOSIT_PREVIEW(depositPreview, CUP_DEPOSIT_PER_CUP)}
                          </FieldDescription>
                        )}
                        {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                      </Field>
                    )}
                  />
                )}
              </>
            )}
          </FieldGroup>
        </form>

        <DialogFooter>
          <Button type="submit" form={FORM_ID} disabled={isPending}>
            {OPTION_GROUP_SCREEN_COPY.BUTTON_SAVE}
          </Button>
          <DialogClose asChild>
            <Button variant="outline" disabled={isPending}>
              {OPTION_GROUP_SCREEN_COPY.BUTTON_CANCEL}
            </Button>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
