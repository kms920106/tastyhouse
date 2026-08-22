"use client";

import * as React from "react";

import { Trash2Icon } from "lucide-react";
import { type Control, Controller, type FieldErrors, type UseFieldArrayReturn } from "react-hook-form";

import { Button } from "@/components/ui/button";
import { Field, FieldDescription, FieldError, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { PRODUCT_PRICE_COPY, PRODUCT_PRICE_MESSAGE } from "@/feature/product/message";
import type { ProductPricesFormValues } from "@/feature/product/schema";

interface MenuPriceRowsProps {
  control: Control<ProductPricesFormValues>;
  fieldArray: UseFieldArrayReturn<ProductPricesFormValues, "prices">;
  errors: FieldErrors<ProductPricesFormValues>;
  /** 저장 중 */
  pending: boolean;
  /**
   * 매장가격 인증 ON 여부.
   *
   * OFF 면 매장가·픽업가 입력란을 비활성한다 — 서버도 `PRODUCT_PRICE_STORE_NOT_VERIFIED` 로
   * 거절하므로 입력을 받아두면 저장 시점에야 실패한다.
   */
  storePriceVerified: boolean;
  /** 할인 진행·대기 중이면 가격 영역 전체를 비활성한다 */
  discountInProgress: boolean;
  /** 매장 가격 인증 화면으로 보내는 진입점. 인증 OFF 안내에 함께 노출한다 */
  onNavigateVerification?: () => void;
}

/**
 * 메뉴 가격 행 목록 편집.
 *
 * 한 행이 `(가격명, 배달가, 매장가, 픽업가)` 다. 행이 1개면 가격명을 비워둘 수 있고,
 * **2개 이상이면 전 행에 가격명이 필수**가 된다(스키마가 판정하므로 이 컴포넌트는 표시만 한다).
 *
 * 금액을 `type="text"` + `inputMode="numeric"` 으로 다루는 이유는 `type="number"` 가 빈 값을
 * `NaN` 으로 만들어 **"미설정"과 "0"을 구분할 수 없게** 되기 때문이다. 매장가·픽업가는 미설정이
 * 유효한 상태(`null`)라 이 구분이 필요하다.
 */
export function MenuPriceRows({
  control,
  fieldArray,
  errors,
  pending,
  storePriceVerified,
  discountInProgress,
  onNavigateVerification,
}: MenuPriceRowsProps) {
  const { fields, append, remove } = fieldArray;

  // 할인 중이면 가격을 아예 못 만진다. 인증 OFF 는 매장가·픽업가만 막는다 — 배달가는 상시 가능하다.
  const rowDisabled = pending || discountInProgress;
  const channelDisabled = rowDisabled || !storePriceVerified;

  // 최소 1개는 남겨야 한다 — 가격이 없는 메뉴는 주문될 수 없다
  const canRemove = fields.length > 1;

  // 가격명은 행이 2개 이상일 때만 필수다. 라벨의 (선택) 표기를 그때 뗀다.
  const nameRequired = fields.length > 1;

  return (
    <div className="flex flex-col gap-4">
      {discountInProgress && (
        <p className="rounded-md bg-muted px-3 py-2 text-muted-foreground text-sm">
          {PRODUCT_PRICE_MESSAGE.DISCOUNT_IN_PROGRESS}
        </p>
      )}

      {!storePriceVerified && !discountInProgress && (
        <div className="flex flex-col items-start gap-1 rounded-md bg-muted px-3 py-2">
          <p className="text-muted-foreground text-sm">{PRODUCT_PRICE_MESSAGE.STORE_NOT_VERIFIED}</p>
          {onNavigateVerification !== undefined && (
            <Button type="button" variant="link" className="h-auto p-0" onClick={onNavigateVerification}>
              {PRODUCT_PRICE_COPY.VERIFICATION_LINK}
            </Button>
          )}
        </div>
      )}

      {fields.map((row, index) => (
        <div key={row.id} className="flex flex-col gap-2 rounded-md border p-3">
          <div className="flex items-start justify-between gap-2">
            <Controller
              control={control}
              name={`prices.${index}.priceName`}
              render={({ field, fieldState }) => (
                <Field className="flex-1 gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor={`menu-price-name-${index}`}>
                    {nameRequired ? PRODUCT_PRICE_COPY.FIELD_PRICE_NAME : PRODUCT_PRICE_COPY.FIELD_PRICE_NAME_OPTIONAL}
                  </FieldLabel>
                  <Input
                    id={`menu-price-name-${index}`}
                    value={field.value}
                    onChange={field.onChange}
                    aria-invalid={fieldState.invalid}
                    disabled={rowDisabled}
                  />
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />
            <Button
              type="button"
              variant="ghost"
              size="icon"
              className="mt-6"
              aria-label={PRODUCT_PRICE_COPY.ACTION_REMOVE_ROW}
              disabled={rowDisabled || !canRemove}
              onClick={() => remove(index)}
            >
              <Trash2Icon className="size-4" />
            </Button>
          </div>

          <div className="grid grid-cols-1 gap-2 sm:grid-cols-3">
            <Controller
              control={control}
              name={`prices.${index}.deliveryPrice`}
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor={`menu-price-delivery-${index}`}>
                    {PRODUCT_PRICE_COPY.FIELD_DELIVERY_PRICE}
                  </FieldLabel>
                  <Input
                    id={`menu-price-delivery-${index}`}
                    inputMode="numeric"
                    value={field.value}
                    onChange={field.onChange}
                    aria-invalid={fieldState.invalid}
                    disabled={rowDisabled}
                  />
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />
            <Controller
              control={control}
              name={`prices.${index}.storePrice`}
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor={`menu-price-store-${index}`}>{PRODUCT_PRICE_COPY.FIELD_STORE_PRICE}</FieldLabel>
                  <Input
                    id={`menu-price-store-${index}`}
                    inputMode="numeric"
                    value={field.value}
                    onChange={field.onChange}
                    aria-invalid={fieldState.invalid}
                    disabled={channelDisabled}
                  />
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />
            <Controller
              control={control}
              name={`prices.${index}.pickupPrice`}
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor={`menu-price-pickup-${index}`}>
                    {PRODUCT_PRICE_COPY.FIELD_PICKUP_PRICE}
                  </FieldLabel>
                  <Input
                    id={`menu-price-pickup-${index}`}
                    inputMode="numeric"
                    value={field.value}
                    onChange={field.onChange}
                    aria-invalid={fieldState.invalid}
                    disabled={channelDisabled}
                  />
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />
          </div>
        </div>
      ))}

      {/* 배열 레벨 오류(min(1)) 는 행이 아니라 목록에 붙는다 */}
      {errors.prices?.message !== undefined && <p className="text-destructive text-sm">{errors.prices.message}</p>}

      <Button
        type="button"
        variant="outline"
        disabled={rowDisabled}
        onClick={() => append({ priceName: "", deliveryPrice: "", storePrice: "", pickupPrice: "" })}
      >
        {PRODUCT_PRICE_COPY.ACTION_ADD_ROW}
      </Button>

      <FieldDescription>{PRODUCT_PRICE_COPY.HELP_PRICE_NAME}</FieldDescription>

      <ChannelPriceGuide />
    </div>
  );
}

/**
 * 채널별 가격 안내표 (접이식).
 *
 * PDF 표를 그대로 옮긴다 — 어떤 가격이 어느 탭에 노출되는지는 점주가 가장 자주 묻는 부분이라
 * 화면 안에 둔다. 기본은 접어두어 편집을 방해하지 않는다.
 */
function ChannelPriceGuide() {
  const [open, setOpen] = React.useState(false);

  return (
    <div className="rounded-md border">
      <Button
        type="button"
        variant="ghost"
        className="w-full justify-between"
        aria-expanded={open}
        onClick={() => setOpen((prev) => !prev)}
      >
        {PRODUCT_PRICE_COPY.CHANNEL_GUIDE_TITLE}
        <span aria-hidden>{open ? "−" : "+"}</span>
      </Button>

      {open && (
        <div className="px-3 pb-3">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-muted-foreground">
                <th className="py-1 text-left font-normal">{PRODUCT_PRICE_COPY.CHANNEL_GUIDE_COLUMN_KIND}</th>
                <th className="py-1 text-left font-normal">{PRODUCT_PRICE_COPY.CHANNEL_GUIDE_COLUMN_CONDITION}</th>
                <th className="py-1 text-left font-normal">{PRODUCT_PRICE_COPY.CHANNEL_GUIDE_COLUMN_EXPOSURE}</th>
              </tr>
            </thead>
            <tbody>
              {PRODUCT_PRICE_COPY.CHANNEL_GUIDE_ROWS.map((row) => (
                <tr key={row.kind} className="border-t">
                  <td className="py-1.5 pr-2">{row.kind}</td>
                  <td className="py-1.5 pr-2">{row.condition}</td>
                  <td className="py-1.5">{row.exposure}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <p className="mt-2 text-muted-foreground text-xs">{PRODUCT_PRICE_COPY.CHANNEL_GUIDE_FOOTNOTE}</p>
        </div>
      )}
    </div>
  );
}
