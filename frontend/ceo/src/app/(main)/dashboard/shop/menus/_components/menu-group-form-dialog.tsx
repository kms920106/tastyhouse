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
import { Textarea } from "@/components/ui/textarea";
import { PRODUCT_MENU_COPY } from "@/feature/product/message";
import { type MenuCategoryFormValues, menuCategoryFormSchema } from "@/feature/product/schema";

const FORM_ID = "menu-group-form";

const DEFAULT_VALUES: MenuCategoryFormValues = { name: "", description: "" };

/** 수정 대상. null 이면 추가 모드다 — 두 모드가 필드 구성이 같아 한 다이얼로그로 겸한다 */
export interface MenuGroupFormTarget {
  categoryId: number;
  name: string;
  description: string;
}

interface MenuGroupFormDialogProps {
  open: boolean;
  pending?: boolean;
  /** null 이면 추가, 값이 있으면 그 그룹의 수정 */
  target: MenuGroupFormTarget | null;
  onOpenChange: (open: boolean) => void;
  onSubmit: (values: MenuCategoryFormValues) => void;
}

export function MenuGroupFormDialog({ open, pending, target, onOpenChange, onSubmit }: MenuGroupFormDialogProps) {
  const form = useForm<MenuCategoryFormValues>({
    resolver: zodResolver(menuCategoryFormSchema),
    defaultValues: DEFAULT_VALUES,
  });

  // 열 때마다 대상 값으로 다시 채운다 — 추가 후 곧바로 수정을 열면 이전 입력이 남는다.
  React.useEffect(() => {
    if (!open) return;
    form.reset(target === null ? DEFAULT_VALUES : { name: target.name, description: target.description });
  }, [open, target, form]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>
            {target === null ? PRODUCT_MENU_COPY.DIALOG_GROUP_CREATE_TITLE : PRODUCT_MENU_COPY.DIALOG_GROUP_EDIT_TITLE}
          </DialogTitle>
          <DialogDescription>{PRODUCT_MENU_COPY.DIALOG_GROUP_DESCRIPTION}</DialogDescription>
        </DialogHeader>

        <form id={FORM_ID} noValidate onSubmit={form.handleSubmit(onSubmit)}>
          <FieldGroup className="gap-4">
            <Controller
              control={form.control}
              name="name"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="menu-group-name">{PRODUCT_MENU_COPY.FIELD_GROUP_NAME}</FieldLabel>
                  <Input
                    id="menu-group-name"
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
                  <FieldLabel htmlFor="menu-group-description">{PRODUCT_MENU_COPY.FIELD_GROUP_DESCRIPTION}</FieldLabel>
                  <Textarea
                    id="menu-group-description"
                    rows={3}
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
            {PRODUCT_MENU_COPY.BUTTON_SAVE}
          </Button>
          <DialogClose asChild>
            <Button variant="outline" disabled={pending}>
              {PRODUCT_MENU_COPY.BUTTON_CANCEL}
            </Button>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
