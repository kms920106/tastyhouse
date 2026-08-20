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
import { Textarea } from "@/components/ui/textarea";
import type { MenuOptionGroup } from "@/feature/product/domain";
import { OPTION_GROUP_SCREEN_COPY, PRODUCT_OPTION_GROUP_COPY } from "@/feature/product/message";
import { type OptionGroupFormValues, optionGroupFormSchema } from "@/feature/product/schema";

/** 그룹 저장 페이로드. 최소/최대는 서버가 `null` 을 "미지정"으로 받는다 */
export interface OptionGroupSubmitValues {
  name: string;
  description: string;
  required: boolean;
  multipleSelect: boolean;
  minSelect: number | null;
  maxSelect: number | null;
}

interface OptionGroupFormDialogProps {
  open: boolean;
  /** 지정하면 수정, 없으면 추가 */
  group?: MenuOptionGroup;
  pending?: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (values: OptionGroupSubmitValues) => void;
}

const FORM_ID = "option-group-form";

const DEFAULT_VALUES: OptionGroupFormValues = {
  name: "",
  description: "",
  required: false,
  multipleSelect: false,
  minSelect: "",
  maxSelect: "",
};

/** 스키마가 숫자 필드를 문자열로 다루므로(빈 문자열 = 미지정) 경계에서만 숫자로 바꾼다 */
function toNullableNumber(value: string): number | null {
  const trimmed = value.trim();
  return trimmed === "" ? null : Number(trimmed);
}

export function OptionGroupFormDialog({ open, group, pending, onOpenChange, onSubmit }: OptionGroupFormDialogProps) {
  const form = useForm<OptionGroupFormValues>({
    resolver: zodResolver(optionGroupFormSchema),
    defaultValues: DEFAULT_VALUES,
  });

  const { reset } = form;

  // 같은 다이얼로그 인스턴스를 추가/수정에 재사용하므로 열릴 때마다 대상 값으로 되돌린다.
  // 이걸 빼면 그룹 A 를 수정하다 닫고 [옵션그룹 추가] 를 눌렀을 때 A 의 값이 남는다.
  React.useEffect(() => {
    if (!open) return;

    reset(
      group
        ? {
            name: group.name,
            description: group.description ?? "",
            required: group.required,
            multipleSelect: group.multipleSelect,
            minSelect: group.minSelect === null ? "" : String(group.minSelect),
            maxSelect: group.maxSelect === null ? "" : String(group.maxSelect),
          }
        : DEFAULT_VALUES,
    );
  }, [open, group, reset]);

  const handleSubmit = (values: OptionGroupFormValues) => {
    onSubmit({
      name: values.name,
      description: values.description,
      required: values.required,
      multipleSelect: values.multipleSelect,
      minSelect: toNullableNumber(values.minSelect),
      maxSelect: toNullableNumber(values.maxSelect),
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
                    disabled={pending}
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
                    disabled={pending}
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
                  <Switch
                    id="option-group-required"
                    checked={field.value}
                    onCheckedChange={field.onChange}
                    disabled={pending}
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
                    disabled={pending}
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
                      disabled={pending}
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
                      disabled={pending}
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
          <Button type="submit" form={FORM_ID} disabled={pending}>
            {OPTION_GROUP_SCREEN_COPY.BUTTON_SAVE}
          </Button>
          <DialogClose asChild>
            <Button variant="outline" disabled={pending}>
              {OPTION_GROUP_SCREEN_COPY.BUTTON_CANCEL}
            </Button>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
