"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";

import { Button } from "@/components/ui/button";
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
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
import { Textarea } from "@/components/ui/textarea";
import { SPICINESS_OPTIONS } from "@/feature/product/constants";
import { PRODUCT_DETAIL_COPY, PRODUCT_DETAIL_SCREEN_COPY, PRODUCT_MENU_COPY } from "@/feature/product/message";
import { type MenuFormValues, menuFormSchema } from "@/feature/product/schema";

/** 미분류를 나타내는 센티넬. Radix Select 는 빈 문자열을 항목 값으로 쓸 수 없다 */
export const CATEGORY_NONE_VALUE = "NONE";

/**
 * 상세 화면의 기본 정보 Sheet 는 전부 **같은 폼 스키마의 부분집합**을 편집한다.
 *
 * `updateMenuAction` 이 전체 필드를 요구하는 PUT(전량 치환)이라, Sheet 마다 스키마를 쪼개면
 * 나머지 필드를 어디선가 다시 조립해야 하고 그 조립이 어긋나면 **편집하지 않은 값이 조용히
 * 초기화된다.** 그래서 폼 하나(`menuFormSchema`)를 공유하고 어떤 필드를 그릴지만 나눈다.
 */
export type MenuBasicSection = "name" | "text" | "price" | "category";

const SHEET_TITLE: Record<MenuBasicSection, string> = {
  name: PRODUCT_DETAIL_COPY.SHEET_NAME_TITLE,
  text: PRODUCT_DETAIL_COPY.SHEET_TEXT_TITLE,
  price: PRODUCT_DETAIL_COPY.SHEET_PRICE_TITLE,
  category: PRODUCT_DETAIL_COPY.SHEET_CATEGORY_TITLE,
};

const FORM_ID = "menu-basic-form";

interface MenuBasicSheetProps {
  /** null 이면 닫힌 상태. 어떤 섹션을 열었는지가 곧 열림 여부다 */
  section: MenuBasicSection | null;
  onOpenChange: (open: boolean) => void;
  pending: boolean;
  defaultValues: MenuFormValues;
  categories: { id: number; name: string }[];
  onSubmit: (values: MenuFormValues) => void;
}

export function MenuBasicSheet({
  section,
  onOpenChange,
  pending,
  defaultValues,
  categories,
  onSubmit,
}: MenuBasicSheetProps) {
  const form = useForm<MenuFormValues>({
    resolver: zodResolver(menuFormSchema),
    defaultValues,
  });

  // 열 때마다 서버가 내려준 현재 값으로 되돌린다 — 취소하고 다시 연 Sheet 에 이전 편집이
  // 남아 있으면, 저장 시 전체 필드를 보내는 PUT 이라 **버린 줄 알았던 값이 되살아난다.**
  React.useEffect(() => {
    if (section !== null) form.reset(defaultValues);
  }, [section, defaultValues, form]);

  return (
    <Sheet open={section !== null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{section === null ? "" : SHEET_TITLE[section]}</SheetTitle>
          <SheetDescription>{PRODUCT_DETAIL_COPY.PAGE_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <form id={FORM_ID} noValidate onSubmit={form.handleSubmit(onSubmit)} className="flex-1 overflow-y-auto px-4">
          <FieldGroup className="gap-4">
            {section === "name" && (
              <Controller
                control={form.control}
                name="name"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="menu-basic-name">{PRODUCT_MENU_COPY.FIELD_NAME}</FieldLabel>
                    <Input
                      id="menu-basic-name"
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
            )}

            {section === "text" && (
              <>
                <Controller
                  control={form.control}
                  name="composition"
                  render={({ field, fieldState }) => (
                    <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor="menu-basic-composition">{PRODUCT_MENU_COPY.FIELD_COMPOSITION}</FieldLabel>
                      <Textarea
                        id="menu-basic-composition"
                        rows={3}
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
                      <FieldLabel htmlFor="menu-basic-description">{PRODUCT_MENU_COPY.FIELD_DESCRIPTION}</FieldLabel>
                      <Textarea
                        id="menu-basic-description"
                        rows={5}
                        value={field.value}
                        onChange={field.onChange}
                        aria-invalid={fieldState.invalid}
                        disabled={pending}
                      />
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                  )}
                />
                {/* 중량은 설명 텍스트에 섞지 않고 별도 필드로 둔다 — 설명을 고치다 법정 표시가
                    지워지지 않게 하고, 손님 화면에서 따로 강조 배치할 수 있게 한다. */}
                <Controller
                  control={form.control}
                  name="weightText"
                  render={({ field, fieldState }) => (
                    <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor="menu-basic-weight-text">{PRODUCT_MENU_COPY.FIELD_WEIGHT_TEXT}</FieldLabel>
                      <Input
                        id="menu-basic-weight-text"
                        value={field.value}
                        onChange={field.onChange}
                        placeholder={PRODUCT_MENU_COPY.PLACEHOLDER_WEIGHT_TEXT}
                        aria-invalid={fieldState.invalid}
                        disabled={pending}
                      />
                      <FieldDescription>{PRODUCT_MENU_COPY.HELP_WEIGHT_TEXT}</FieldDescription>
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                  )}
                />
              </>
            )}

            {section === "price" && (
              <>
                <Controller
                  control={form.control}
                  name="originalPrice"
                  render={({ field, fieldState }) => (
                    <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor="menu-basic-original-price">
                        {PRODUCT_MENU_COPY.FIELD_ORIGINAL_PRICE}
                      </FieldLabel>
                      <Input
                        id="menu-basic-original-price"
                        type="number"
                        inputMode="numeric"
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
                      <FieldLabel htmlFor="menu-basic-discount-price">
                        {PRODUCT_MENU_COPY.FIELD_DISCOUNT_PRICE}
                      </FieldLabel>
                      <Input
                        id="menu-basic-discount-price"
                        type="number"
                        inputMode="numeric"
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
                <Controller
                  control={form.control}
                  name="spiciness"
                  render={({ field, fieldState }) => (
                    <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor="menu-basic-spiciness">{PRODUCT_MENU_COPY.FIELD_SPICINESS}</FieldLabel>
                      {/* Radix Select 의 value 는 lifetime 내내 문자열이어야 한다 —
                          undefined 로 뒤집히면 uncontrolled → controlled 경고가 난다. */}
                      <Select value={field.value ?? ""} onValueChange={field.onChange} disabled={pending}>
                        <SelectTrigger id="menu-basic-spiciness" aria-invalid={fieldState.invalid}>
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
              </>
            )}

            {section === "category" && (
              <Controller
                control={form.control}
                name="productCategoryId"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="menu-basic-category">{PRODUCT_MENU_COPY.FIELD_CATEGORY}</FieldLabel>
                    <Select value={field.value ?? ""} onValueChange={field.onChange} disabled={pending}>
                      <SelectTrigger id="menu-basic-category" aria-invalid={fieldState.invalid}>
                        <SelectValue placeholder={PRODUCT_MENU_COPY.PLACEHOLDER_SELECT} />
                      </SelectTrigger>
                      <SelectContent position="popper">
                        <SelectGroup>
                          {/* 미분류도 명시적으로 고를 수 있어야 한다 — 서버는 null 을 "미분류로
                              이동"으로 해석하므로 "선택 안 함"과 구분할 필요가 없다. */}
                          <SelectItem value={CATEGORY_NONE_VALUE}>
                            {PRODUCT_MENU_COPY.PLACEHOLDER_CATEGORY_NONE}
                          </SelectItem>
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
            )}
          </FieldGroup>
        </form>

        <SheetFooter>
          <Button type="submit" form={FORM_ID} disabled={pending}>
            {pending ? PRODUCT_DETAIL_SCREEN_COPY.BUTTON_SAVING : PRODUCT_DETAIL_SCREEN_COPY.BUTTON_SAVE}
          </Button>
          <SheetClose asChild>
            <Button variant="outline" disabled={pending}>
              {PRODUCT_DETAIL_SCREEN_COPY.BUTTON_CANCEL}
            </Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
