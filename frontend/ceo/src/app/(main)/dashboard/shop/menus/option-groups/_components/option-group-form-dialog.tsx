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
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import {
  CUP_DEPOSIT_FIXED_MAX_SELECT,
  CUP_DEPOSIT_FIXED_MIN_SELECT,
  OPTION_GROUP_TYPE_OPTIONS,
  OPTION_GROUP_TYPES,
} from "@/feature/product/constants";
import type { MenuOptionGroup, ProductOptionGroupType } from "@/feature/product/domain";
import { OPTION_GROUP_SCREEN_COPY, PRODUCT_OPTION_GROUP_COPY } from "@/feature/product/message";
import { type OptionGroupFormValues, optionGroupFormSchema } from "@/feature/product/schema";

/** 등록 시 연결할 메뉴 선택지 — 메뉴판(카테고리별 목록) 조회 결과에서 id·name만 추린 것 */
export interface LinkableProductOption {
  id: number;
  name: string;
}

/** 그룹 저장 페이로드. 최소/최대는 서버가 `null` 을 "미지정"으로 받는다 */
export interface OptionGroupSubmitValues {
  /** 등록에서만 의미가 있다 — 서버가 등록 시 최초 연결 메뉴로 요구한다. 수정에서는 보내지 않는다 */
  productId: number | null;
  name: string;
  description: string;
  required: boolean;
  multipleSelect: boolean;
  minSelect: number | null;
  maxSelect: number | null;
  /** 등록에서만 의미가 있다 — 수정 폼은 유형 선택을 노출하지 않으므로 항상 `NORMAL` 이 남는다 */
  groupType: ProductOptionGroupType;
}

interface OptionGroupFormDialogProps {
  open: boolean;
  /** 지정하면 수정, 없으면 추가 */
  group?: MenuOptionGroup;
  /** 등록 시 연결할 메뉴 후보. 메뉴가 하나도 없으면 옵션그룹을 만들 수 없다 */
  linkableProducts: LinkableProductOption[];
  /**
   * 가게가 일회용컵 보증금제 대상사업자인지 여부.
   *
   * `false` 면 유형 선택 UI 자체를 숨긴다 — 서버가 `SHOP_CUP_DEPOSIT_NOT_ENABLED` 로 거부하므로
   * 고를 수 있게 두면 저장 실패만 남는다.
   */
  cupDepositEnabled?: boolean;
  pending?: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (values: OptionGroupSubmitValues) => void;
}

const FORM_ID = "option-group-form";

const DEFAULT_VALUES: OptionGroupFormValues = {
  productId: null,
  name: "",
  description: "",
  groupType: OPTION_GROUP_TYPES.NORMAL,
  required: false,
  multipleSelect: false,
  minSelect: "",
  maxSelect: "",
  isCreate: true,
};

/** 스키마가 숫자 필드를 문자열로 다루므로(빈 문자열 = 미지정) 경계에서만 숫자로 바꾼다 */
function toNullableNumber(value: string): number | null {
  const trimmed = value.trim();
  return trimmed === "" ? null : Number(trimmed);
}

export function OptionGroupFormDialog({
  open,
  group,
  linkableProducts,
  cupDepositEnabled,
  pending,
  onOpenChange,
  onSubmit,
}: OptionGroupFormDialogProps) {
  const form = useForm<OptionGroupFormValues>({
    resolver: zodResolver(optionGroupFormSchema),
    defaultValues: DEFAULT_VALUES,
  });

  const { reset, setValue, watch } = form;

  // 같은 다이얼로그 인스턴스를 추가/수정에 재사용하므로 열릴 때마다 대상 값으로 되돌린다.
  // 이걸 빼면 그룹 A 를 수정하다 닫고 [옵션그룹 추가] 를 눌렀을 때 A 의 값이 남는다.
  React.useEffect(() => {
    if (!open) return;

    reset(
      group
        ? {
            productId: null,
            name: group.name,
            description: group.description ?? "",
            // 수정에서는 유형을 바꿀 수 없지만(서버가 `groupType` 을 받지 않는다) 스키마의
            // 보증금 제약 검사가 현재 유형을 알아야 하므로 원래 값을 그대로 채운다.
            groupType: group.groupType,
            required: group.required,
            multipleSelect: group.multipleSelect,
            minSelect: group.minSelect === null ? "" : String(group.minSelect),
            maxSelect: group.maxSelect === null ? "" : String(group.maxSelect),
            isCreate: false,
          }
        : DEFAULT_VALUES,
    );
  }, [open, group, reset]);

  const groupType = watch("groupType");
  const isCupDeposit = groupType === OPTION_GROUP_TYPES.CUP_DEPOSIT;

  // `pending` 은 optional prop 이라 `boolean | undefined` 다 — 아래 `||` 조합에 그대로 쓰면
  // 린터가 nullish 병합으로 오인하므로 한 번만 boolean 으로 좁혀 둔다.
  const isPending = pending === true;

  // 유형은 등록에서만 고른다 — 수정 경로가 서버에 없어 노출하면 "바꿨는데 반영 안 됨"이 된다.
  const isTypeSelectable = group === undefined && cupDepositEnabled === true;

  /**
   * 보증금 유형을 고르면 서버가 강제하는 값으로 폼을 즉시 맞춘다.
   *
   * 입력을 비활성화만 하고 값을 그대로 두면, 일반 그룹에서 입력하던 값이 남아
   * `PRODUCT_OPTION_GROUP_DEPOSIT_SELECT_FIXED` 로 거절된다.
   */
  function handleGroupTypeChange(next: ProductOptionGroupType) {
    setValue("groupType", next, { shouldValidate: false });

    if (next !== OPTION_GROUP_TYPES.CUP_DEPOSIT) return;

    setValue("required", false, { shouldValidate: false });
    setValue("multipleSelect", false, { shouldValidate: false });
    setValue("minSelect", CUP_DEPOSIT_FIXED_MIN_SELECT, { shouldValidate: false });
    setValue("maxSelect", CUP_DEPOSIT_FIXED_MAX_SELECT, { shouldValidate: false });
  }

  const handleSubmit = (values: OptionGroupFormValues) => {
    onSubmit({
      // 수정에서는 서버가 받지 않으므로 항상 null로 보낸다 — 값이 남아 있어도 액션이 무시한다.
      productId: values.isCreate ? values.productId : null,
      name: values.name,
      description: values.description,
      required: values.required,
      multipleSelect: values.multipleSelect,
      minSelect: toNullableNumber(values.minSelect),
      maxSelect: toNullableNumber(values.maxSelect),
      groupType: values.groupType,
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>
            {group
              ? PRODUCT_OPTION_GROUP_COPY.DIALOG_GROUP_EDIT_TITLE
              : PRODUCT_OPTION_GROUP_COPY.DIALOG_GROUP_CREATE_TITLE}
          </DialogTitle>
          <DialogDescription>{PRODUCT_OPTION_GROUP_COPY.PAGE_DESCRIPTION}</DialogDescription>
        </DialogHeader>

        <form id={FORM_ID} noValidate onSubmit={form.handleSubmit(handleSubmit)}>
          <FieldGroup className="gap-4">
            {/* 메뉴 선택은 등록에서만 필요하다 — 수정 대상 그룹은 이미 최소 1개 메뉴에 연결돼 있다. */}
            {group === undefined && (
              <Controller
                control={form.control}
                name="productId"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="option-group-product">
                      {PRODUCT_OPTION_GROUP_COPY.FIELD_LINK_PRODUCT}
                    </FieldLabel>
                    <Select
                      value={field.value === null ? "" : String(field.value)}
                      onValueChange={(value) => field.onChange(Number(value))}
                      disabled={isPending || linkableProducts.length === 0}
                    >
                      <SelectTrigger id="option-group-product" aria-invalid={fieldState.invalid}>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent position="popper" align="start">
                        {linkableProducts.map((product) => (
                          <SelectItem key={product.id} value={String(product.id)}>
                            {product.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FieldDescription>{PRODUCT_OPTION_GROUP_COPY.HELP_LINK_PRODUCT}</FieldDescription>
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />
            )}

            {isTypeSelectable && (
              <Controller
                control={form.control}
                name="groupType"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="option-group-type">{PRODUCT_OPTION_GROUP_COPY.FIELD_GROUP_TYPE}</FieldLabel>
                    <Select
                      value={field.value}
                      onValueChange={(value) => handleGroupTypeChange(value as ProductOptionGroupType)}
                      disabled={isPending}
                    >
                      <SelectTrigger id="option-group-type" aria-invalid={fieldState.invalid}>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent position="popper" align="start">
                        {OPTION_GROUP_TYPE_OPTIONS.map((option) => (
                          <SelectItem key={option.value} value={option.value}>
                            {option.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FieldDescription>{PRODUCT_OPTION_GROUP_COPY.HELP_GROUP_TYPE}</FieldDescription>
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />
            )}

            {isCupDeposit && (
              <p className="rounded-md border border-dashed p-3 text-muted-foreground text-sm">
                {PRODUCT_OPTION_GROUP_COPY.NOTICE_CUP_DEPOSIT_GROUP}
              </p>
            )}

            <Controller
              control={form.control}
              name="name"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="option-group-name">{PRODUCT_OPTION_GROUP_COPY.FIELD_GROUP_NAME}</FieldLabel>
                  <Input
                    id="option-group-name"
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
              name="description"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="option-group-description">
                    {PRODUCT_OPTION_GROUP_COPY.FIELD_GROUP_DESCRIPTION}
                  </FieldLabel>
                  <Textarea
                    id="option-group-description"
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
              name="required"
              render={({ field }) => (
                <Field orientation="horizontal" className="gap-3">
                  <FieldLabel htmlFor="option-group-required" className="flex-1">
                    {PRODUCT_OPTION_GROUP_COPY.FIELD_REQUIRED}
                    <FieldDescription>{PRODUCT_OPTION_GROUP_COPY.HELP_REQUIRED}</FieldDescription>
                  </FieldLabel>
                  {/* 보증금 그룹은 필수 선택이 불가능하다(강제하면 개인컵 손님이 주문할 수 없다). */}
                  <Switch
                    id="option-group-required"
                    checked={field.value}
                    onCheckedChange={field.onChange}
                    disabled={isPending || isCupDeposit}
                  />
                </Field>
              )}
            />

            <Controller
              control={form.control}
              name="multipleSelect"
              render={({ field }) => (
                <Field orientation="horizontal" className="gap-3">
                  <FieldLabel htmlFor="option-group-multiple-select" className="flex-1">
                    {PRODUCT_OPTION_GROUP_COPY.FIELD_MULTIPLE_SELECT}
                    <FieldDescription>{PRODUCT_OPTION_GROUP_COPY.HELP_MULTIPLE_SELECT}</FieldDescription>
                  </FieldLabel>
                  <Switch
                    id="option-group-multiple-select"
                    checked={field.value}
                    onCheckedChange={field.onChange}
                    disabled={isPending || isCupDeposit}
                  />
                </Field>
              )}
            />

            <div className="grid grid-cols-2 gap-4">
              <Controller
                control={form.control}
                name="minSelect"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="option-group-min-select">
                      {PRODUCT_OPTION_GROUP_COPY.FIELD_MIN_SELECT}
                    </FieldLabel>
                    {/* 빈 문자열이 "미지정"이라 number 로 받지 않는다 — 스키마가 문자열로 검증한다. */}
                    <Input
                      id="option-group-min-select"
                      type="number"
                      min={0}
                      inputMode="numeric"
                      placeholder={OPTION_GROUP_SCREEN_COPY.SELECT_RANGE_PLACEHOLDER}
                      value={field.value}
                      onChange={field.onChange}
                      aria-invalid={fieldState.invalid}
                      disabled={isPending || isCupDeposit}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="maxSelect"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="option-group-max-select">
                      {PRODUCT_OPTION_GROUP_COPY.FIELD_MAX_SELECT}
                    </FieldLabel>
                    <Input
                      id="option-group-max-select"
                      type="number"
                      min={0}
                      inputMode="numeric"
                      placeholder={OPTION_GROUP_SCREEN_COPY.SELECT_RANGE_PLACEHOLDER}
                      value={field.value}
                      onChange={field.onChange}
                      aria-invalid={fieldState.invalid}
                      disabled={isPending || isCupDeposit}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />
            </div>

            <FieldDescription>{PRODUCT_OPTION_GROUP_COPY.HELP_SELECT_RANGE}</FieldDescription>
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
