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
import { Field, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import type { MenuOption } from "@/feature/product/domain";
import { OPTION_GROUP_SCREEN_COPY, PRODUCT_OPTION_GROUP_COPY } from "@/feature/product/message";
import { type OptionFormValues, optionFormSchema } from "@/feature/product/schema";

interface OptionFormDialogProps {
  open: boolean;
  /** 지정하면 수정, 없으면 추가 */
  option?: MenuOption;
  pending?: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (values: { name: string; additionalPrice: number }) => void;
}

const FORM_ID = "option-form";

const DEFAULT_VALUES: OptionFormValues = { name: "", additionalPrice: "" };

export function OptionFormDialog({ open, option, pending, onOpenChange, onSubmit }: OptionFormDialogProps) {
  const form = useForm<OptionFormValues>({
    resolver: zodResolver(optionFormSchema),
    defaultValues: DEFAULT_VALUES,
  });

  const { reset } = form;

  // 그룹마다 [옵션 추가]/[옵션 수정]이 같은 다이얼로그를 공유하므로 열릴 때마다 대상 값으로 되돌린다.
  React.useEffect(() => {
    if (!open) return;

    reset(option ? { name: option.name, additionalPrice: String(option.additionalPrice) } : DEFAULT_VALUES);
  }, [open, option, reset]);

  const handleSubmit = (values: OptionFormValues) => {
    // 추가 금액은 생략 가능하고, 그때는 0원으로 본다(스키마가 빈 문자열을 허용한다).
    const trimmed = values.additionalPrice.trim();
    onSubmit({ name: values.name, additionalPrice: trimmed === "" ? 0 : Number(trimmed) });
  };

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
                    disabled={pending}
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
                  <Input
                    id="option-additional-price"
                    type="number"
                    min={0}
                    inputMode="numeric"
                    value={field.value}
                    onChange={field.onChange}
                    aria-invalid={fieldState.invalid}
                    disabled={pending}
                  />
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />
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
