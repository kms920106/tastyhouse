"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldLabel } from "@/components/ui/field";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { updateShopReviewSortTypeAction } from "@/feature/shop-review/actions";
import { DEFAULT_REVIEW_SORT_TYPE, REVIEW_SORT_TYPE_OPTIONS } from "@/feature/shop-review/constants";
import type { ShopReviewSortTypeSetting } from "@/feature/shop-review/domain";
import { SHOP_REVIEW_COPY } from "@/feature/shop-review/message";
import { type SortTypeFormValues, sortTypeSchema } from "@/feature/shop-review/schema";
import { formatDateTime } from "@/lib/date";

interface ShopReviewSortTypeFormProps {
  shopId: number;
  /** 조회 실패 시 undefined — 폼은 기본값(최신순)으로 열고 안내 문구를 띄운다 */
  setting?: ShopReviewSortTypeSetting;
  disabled?: boolean;
}

/**
 * 앱 노출 정렬 설정 — 고객 앱 리뷰 탭의 **기본** 정렬을 저장한다.
 *
 * 필터의 `sortType`(URL, 이 화면 조회용)과 완전히 별개다. 두 컨트롤이 나란히 보이므로
 * 안내 문구로 역할 차이를 분명히 밝힌다.
 */
export function ShopReviewSortTypeForm({ shopId, setting, disabled }: ShopReviewSortTypeFormProps) {
  const [isPending, startTransition] = React.useTransition();

  // 서버가 미설정 가게에도 기본값(LATEST)을 내려주지만, 조회 자체가 실패했을 때를 위해 한 번 더 방어한다.
  const savedSortType = (setting?.sortType ?? DEFAULT_REVIEW_SORT_TYPE) as SortTypeFormValues["sortType"];

  const form = useForm<SortTypeFormValues>({
    resolver: zodResolver(sortTypeSchema),
    defaultValues: { sortType: savedSortType },
  });

  // 저장 후 revalidate 로 새 값이 내려오면 폼도 그 값을 기준으로 다시 잡는다.
  React.useEffect(() => {
    form.reset({ sortType: savedSortType });
  }, [savedSortType, form.reset]);

  const onSubmit = (values: SortTypeFormValues) => {
    startTransition(async () => {
      const { success, message } = await updateShopReviewSortTypeAction(shopId, values);
      if (success) {
        toast.success(SHOP_REVIEW_COPY.SORT_TYPE_SAVED);
      } else {
        toast.error(message ?? SHOP_REVIEW_COPY.SORT_TYPE_SAVE_FAILED);
      }
    });
  };

  const isBusy = isPending || disabled;

  return (
    <section className="flex flex-col gap-2">
      <div className="flex flex-col gap-1">
        <h2 className="font-medium text-sm">{SHOP_REVIEW_COPY.SORT_TYPE_SECTION_TITLE}</h2>
        <p className="text-muted-foreground text-xs leading-snug">{SHOP_REVIEW_COPY.SORT_TYPE_GUIDE}</p>
      </div>

      {setting === undefined && <p className="text-destructive text-sm">{SHOP_REVIEW_COPY.SORT_TYPE_LOAD_FAILED}</p>}

      <form className="flex flex-wrap items-end gap-2" noValidate onSubmit={form.handleSubmit(onSubmit)}>
        <Controller
          control={form.control}
          name="sortType"
          render={({ field, fieldState }) => (
            <Field className="w-full gap-1.5 md:w-48" data-invalid={fieldState.invalid}>
              <FieldLabel htmlFor="shop-review-app-sort-type">{SHOP_REVIEW_COPY.SORT_TYPE_LABEL}</FieldLabel>
              {/* Radix Select 의 value 는 항상 안정 문자열이어야 한다(undefined 금지) */}
              <Select value={field.value ?? ""} onValueChange={field.onChange} disabled={isBusy}>
                <SelectTrigger id="shop-review-app-sort-type" className="w-full" aria-invalid={fieldState.invalid}>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent position="popper" align="start">
                  <SelectGroup>
                    {REVIEW_SORT_TYPE_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
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

        <Button type="submit" disabled={isBusy}>
          {SHOP_REVIEW_COPY.SORT_TYPE_SAVE}
        </Button>
      </form>

      {/* 미설정 가게는 updatedAt 이 null 이라 "아직 설정하지 않음"을 분명히 알린다 */}
      <p className="text-muted-foreground text-xs">
        {setting?.updatedAt
          ? `${SHOP_REVIEW_COPY.SORT_TYPE_UPDATED_AT} ${formatDateTime(setting.updatedAt)}`
          : SHOP_REVIEW_COPY.SORT_TYPE_NOT_CONFIGURED}
      </p>
    </section>
  );
}
