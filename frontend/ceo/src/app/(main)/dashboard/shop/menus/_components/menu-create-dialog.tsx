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
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel, FieldSeparator } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import { SPICINESS_OPTIONS } from "@/feature/product/constants";
import type { MenuCategory } from "@/feature/product/domain";
import { PRODUCT_MENU_COPY } from "@/feature/product/message";
import { type MenuFormValues, menuFormSchema } from "@/feature/product/schema";

/**
 * 미분류 센티넬.
 *
 * Radix `Select` 는 빈 문자열을 항목 값으로 쓸 수 없다(빈 문자열은 "선택 해제"의 내부 표현이라
 * `SelectItem value=""` 이 런타임 에러를 낸다). 미분류를 고를 수 있어야 하므로 전용 토큰을 쓰고
 * 제출 시 `null` 로 되돌린다.
 */
const NO_CATEGORY_VALUE = "NONE";

const FORM_ID = "menu-create-form";

const DEFAULT_VALUES: MenuFormValues = {
  name: "",
  productCategoryId: NO_CATEGORY_VALUE,
  composition: "",
  description: "",
  originalPrice: "",
  discountPrice: "",
  singleServing: false,
  representative: false,
  spiciness: "",
  ratingExcluded: false,
};

export interface MenuCreateSubmitValues {
  name: string;
  productCategoryId: number | null;
  composition: string;
  description: string;
  originalPrice: number;
  discountPrice: number | null;
  singleServing: boolean;
  representative: boolean;
  spiciness: number | null;
  ratingExcluded: boolean;
}

interface MenuCreateDialogProps {
  open: boolean;
  pending?: boolean;
  categories: MenuCategory[];
  /** 미분류 그룹에 메뉴가 있으면 Select 에 미분류 항목을 노출한다 */
  onOpenChange: (open: boolean) => void;
  onSubmit: (values: MenuCreateSubmitValues) => void;
}

/**
 * 메뉴 등록 폼.
 *
 * 원문 PDF 는 4단계 위저드지만 이 앱에는 위저드 선례가 없고 `Dialog` 단일 폼이 관례이므로
 * **한 화면의 섹션 구분**으로 구현한다(`docs/tasks/frontend.md` §3-4).
 *
 * 중복 메뉴명·금칙어는 DB 를 봐야 알 수 있어 클라이언트가 판정할 수 없다 —
 * 서버가 내려준 한국어 문구를 호출부가 토스트로 그대로 노출한다.
 */
export function MenuCreateDialog({ open, pending, categories, onOpenChange, onSubmit }: MenuCreateDialogProps) {
  const form = useForm<MenuFormValues>({
    resolver: zodResolver(menuFormSchema),
    defaultValues: DEFAULT_VALUES,
  });

  // 닫았다 다시 열면 이전 입력이 남지 않도록 초기화한다.
  React.useEffect(() => {
    if (open) form.reset(DEFAULT_VALUES);
  }, [open, form]);

  const handleSubmit = (values: MenuFormValues) => {
    onSubmit({
      name: values.name.trim(),
      productCategoryId: values.productCategoryId === NO_CATEGORY_VALUE ? null : Number(values.productCategoryId),
      composition: values.composition.trim(),
      description: values.description.trim(),
      originalPrice: Number(values.originalPrice),
      // 빈 문자열은 "할인 없음"이다 — 0 으로 보내면 전액 할인이 된다.
      discountPrice: values.discountPrice.trim() === "" ? null : Number(values.discountPrice),
      singleServing: values.singleServing,
      representative: values.representative,
      spiciness: values.spiciness === "" ? null : Number(values.spiciness),
      ratingExcluded: values.ratingExcluded,
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90dvh] overflow-y-auto sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>{PRODUCT_MENU_COPY.DIALOG_MENU_CREATE_TITLE}</DialogTitle>
          <DialogDescription>{PRODUCT_MENU_COPY.DIALOG_MENU_CREATE_DESCRIPTION}</DialogDescription>
        </DialogHeader>

        <form id={FORM_ID} noValidate onSubmit={form.handleSubmit(handleSubmit)}>
          <FieldGroup className="gap-4">
            <Controller
              control={form.control}
              name="name"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="menu-create-name">{PRODUCT_MENU_COPY.FIELD_NAME}</FieldLabel>
                  <Input
                    id="menu-create-name"
                    value={field.value}
                    onChange={field.onChange}
                    aria-invalid={fieldState.invalid}
                    disabled={pending}
                  />
                  <FieldDescription>{PRODUCT_MENU_COPY.HELP_NAME}</FieldDescription>
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />

            <Controller
              control={form.control}
              name="productCategoryId"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="menu-create-category">{PRODUCT_MENU_COPY.FIELD_CATEGORY}</FieldLabel>
                  {/* Radix Select 의 value 는 lifetime 내내 문자열이어야 한다 — undefined 로
                      뒤집히면 uncontrolled → controlled 경고가 난다. */}
                  <Select value={field.value ?? ""} onValueChange={field.onChange} disabled={pending}>
                    <SelectTrigger id="menu-create-category" aria-invalid={fieldState.invalid}>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent position="popper">
                      <SelectGroup>
                        <SelectItem value={NO_CATEGORY_VALUE}>{PRODUCT_MENU_COPY.PLACEHOLDER_CATEGORY_NONE}</SelectItem>
                        {categories.map((category) => (
                          <SelectItem key={category.id} value={String(category.id)}>
                            {category.name}
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
              name="composition"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="menu-create-composition">{PRODUCT_MENU_COPY.FIELD_COMPOSITION}</FieldLabel>
                  <Textarea
                    id="menu-create-composition"
                    rows={2}
                    value={field.value}
                    onChange={field.onChange}
                    aria-invalid={fieldState.invalid}
                    disabled={pending}
                  />
                  <FieldDescription>{PRODUCT_MENU_COPY.HELP_COMPOSITION}</FieldDescription>
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />

            <Controller
              control={form.control}
              name="description"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="menu-create-description">{PRODUCT_MENU_COPY.FIELD_DESCRIPTION}</FieldLabel>
                  <Textarea
                    id="menu-create-description"
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

            <FieldSeparator />

            <div className="grid gap-4 sm:grid-cols-2">
              <Controller
                control={form.control}
                name="originalPrice"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="menu-create-original-price">
                      {PRODUCT_MENU_COPY.FIELD_ORIGINAL_PRICE}
                    </FieldLabel>
                    <Input
                      id="menu-create-original-price"
                      type="number"
                      min={0}
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
                name="discountPrice"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="menu-create-discount-price">
                      {PRODUCT_MENU_COPY.FIELD_DISCOUNT_PRICE}
                    </FieldLabel>
                    <Input
                      id="menu-create-discount-price"
                      type="number"
                      min={0}
                      value={field.value}
                      onChange={field.onChange}
                      aria-invalid={fieldState.invalid}
                      disabled={pending}
                    />
                    <FieldDescription>{PRODUCT_MENU_COPY.HELP_DISCOUNT_PRICE}</FieldDescription>
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />
            </div>

            <FieldSeparator />

            <Controller
              control={form.control}
              name="spiciness"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="menu-create-spiciness">{PRODUCT_MENU_COPY.FIELD_SPICINESS}</FieldLabel>
                  <Select value={field.value ?? ""} onValueChange={field.onChange} disabled={pending}>
                    <SelectTrigger id="menu-create-spiciness" aria-invalid={fieldState.invalid}>
                      <SelectValue placeholder={PRODUCT_MENU_COPY.PLACEHOLDER_SELECT} />
                    </SelectTrigger>
                    <SelectContent position="popper">
                      <SelectGroup>
                        {SPICINESS_OPTIONS.map((option) => (
                          <SelectItem key={option.value} value={String(option.value)}>
                            {option.label}
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
              name="singleServing"
              render={({ field }) => (
                <Field orientation="horizontal" className="gap-3">
                  <Switch
                    id="menu-create-single-serving"
                    checked={field.value}
                    onCheckedChange={field.onChange}
                    disabled={pending}
                  />
                  <div className="flex flex-col gap-0.5">
                    <FieldLabel htmlFor="menu-create-single-serving">
                      {PRODUCT_MENU_COPY.FIELD_SINGLE_SERVING}
                    </FieldLabel>
                    <FieldDescription>{PRODUCT_MENU_COPY.HELP_SINGLE_SERVING}</FieldDescription>
                  </div>
                </Field>
              )}
            />

            <Controller
              control={form.control}
              name="representative"
              render={({ field }) => (
                <Field orientation="horizontal" className="gap-3">
                  <Switch
                    id="menu-create-representative"
                    checked={field.value}
                    onCheckedChange={field.onChange}
                    disabled={pending}
                  />
                  <div className="flex flex-col gap-0.5">
                    <FieldLabel htmlFor="menu-create-representative">
                      {PRODUCT_MENU_COPY.FIELD_REPRESENTATIVE}
                    </FieldLabel>
                    <FieldDescription>{PRODUCT_MENU_COPY.HELP_REPRESENTATIVE}</FieldDescription>
                  </div>
                </Field>
              )}
            />

            <Controller
              control={form.control}
              name="ratingExcluded"
              render={({ field }) => (
                <Field orientation="horizontal" className="gap-3">
                  <Switch
                    id="menu-create-rating-excluded"
                    checked={field.value}
                    onCheckedChange={field.onChange}
                    disabled={pending}
                  />
                  <div className="flex flex-col gap-0.5">
                    <FieldLabel htmlFor="menu-create-rating-excluded">
                      {PRODUCT_MENU_COPY.FIELD_RATING_EXCLUDED}
                    </FieldLabel>
                    <FieldDescription>{PRODUCT_MENU_COPY.HELP_RATING_EXCLUDED}</FieldDescription>
                  </div>
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
