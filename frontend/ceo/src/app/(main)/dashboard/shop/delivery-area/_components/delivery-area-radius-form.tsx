"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";

import { Button } from "@/components/ui/button";
import { Field, FieldDescription, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Slider } from "@/components/ui/slider";
import {
  DELIVERY_AREA_DEFAULT_RADIUS_KM,
  DELIVERY_AREA_MAX_RADIUS_KM,
  DELIVERY_AREA_MIN_RADIUS_KM,
  DELIVERY_AREA_RADIUS_STEP_KM,
} from "@/feature/shop/constants";
import type { DeliveryAreaRadiusPreview } from "@/feature/shop/domain";
import { SHOP_OPERATION_COPY } from "@/feature/shop/message";
import { type DeliveryAreaRadiusFormValues, deliveryAreaRadiusSchema } from "@/feature/shop/schema";

interface DeliveryAreaRadiusFormProps {
  /** 반경이 바뀔 때마다 지도에 미리보기 원을 그리게 한다 */
  onRadiusChange: (radiusKm: number) => void;
  /** "이 반경으로 추가" — draft 에 커밋한다(되돌리기 1단위) */
  onApply: (values: DeliveryAreaRadiusFormValues) => void;
  /** 서버가 판정한 미리보기 결과. 아직 없으면 null */
  preview: DeliveryAreaRadiusPreview | null;
  isPending: boolean;
}

/**
 * 반경으로 추가 폼.
 *
 * 이 화면에서 RHF 을 쓰는 곳은 여기뿐이다. 브러시·트리·검색은 폼이 아니라 직접 조작이라
 * `useState`/`useReducer` 로 간다 — 기존 `delivery-area-sheet.tsx` 가 RHF 없이 간 것과 같은 기준이다.
 */
export function DeliveryAreaRadiusForm({ onRadiusChange, onApply, preview, isPending }: DeliveryAreaRadiusFormProps) {
  const form = useForm<DeliveryAreaRadiusFormValues>({
    resolver: zodResolver(deliveryAreaRadiusSchema),
    defaultValues: { radiusKm: DELIVERY_AREA_DEFAULT_RADIUS_KM, replace: false },
    mode: "onChange",
  });

  const radiusKm = form.watch("radiusKm");

  // 슬라이더를 끄는 동안 지도에 원을 즉시 그린다. 서버 미리보기는 셸이 debounce 해서 부른다.
  React.useEffect(() => {
    onRadiusChange(radiusKm);
  }, [radiusKm, onRadiusChange]);

  return (
    <form className="flex flex-col gap-4" onSubmit={form.handleSubmit(onApply)}>
      <Field className="gap-1.5">
        <FieldLabel htmlFor="delivery-area-radius">{SHOP_OPERATION_COPY.DELIVERY_AREA_RADIUS_LABEL}</FieldLabel>
        <FieldDescription>{SHOP_OPERATION_COPY.DELIVERY_AREA_RADIUS_HELP}</FieldDescription>

        <Controller
          control={form.control}
          name="radiusKm"
          render={({ field }) => (
            <div className="flex items-center gap-3">
              <Slider
                id="delivery-area-radius"
                className="flex-1"
                min={DELIVERY_AREA_MIN_RADIUS_KM}
                max={DELIVERY_AREA_MAX_RADIUS_KM}
                step={DELIVERY_AREA_RADIUS_STEP_KM}
                value={[field.value]}
                onValueChange={([next]) => field.onChange(next)}
                disabled={isPending}
                aria-label={SHOP_OPERATION_COPY.DELIVERY_AREA_RADIUS_LABEL}
              />
              <div className="flex shrink-0 items-center gap-1">
                <Input
                  type="number"
                  className="w-20"
                  min={DELIVERY_AREA_MIN_RADIUS_KM}
                  max={DELIVERY_AREA_MAX_RADIUS_KM}
                  step={DELIVERY_AREA_RADIUS_STEP_KM}
                  value={field.value}
                  onChange={(event) => {
                    // 빈 입력은 NaN 이 되어 슬라이더를 깨뜨리므로 하한으로 붙잡는다.
                    const next = Number(event.target.value);
                    field.onChange(Number.isFinite(next) ? next : DELIVERY_AREA_MIN_RADIUS_KM);
                  }}
                  disabled={isPending}
                  aria-label={`${SHOP_OPERATION_COPY.DELIVERY_AREA_RADIUS_LABEL} 직접 입력`}
                />
                <span className="text-muted-foreground text-sm">km</span>
              </div>
            </div>
          )}
        />

        {form.formState.errors.radiusKm ? (
          <p className="text-destructive text-sm">{form.formState.errors.radiusKm.message}</p>
        ) : (
          <FieldDescription>{SHOP_OPERATION_COPY.DELIVERY_AREA_RADIUS_RULE}</FieldDescription>
        )}
      </Field>

      <Controller
        control={form.control}
        name="replace"
        render={({ field }) => (
          <Field className="gap-1.5">
            <FieldLabel>적용 방식</FieldLabel>
            {/* 기본은 더하기 — 가이드의 "반경으로 골격 → 그리기로 다듬기" 흐름을 따른다 */}
            <RadioGroup
              className="flex gap-4"
              value={field.value ? "replace" : "add"}
              onValueChange={(value) => field.onChange(value === "replace")}
              disabled={isPending}
            >
              <div className="flex items-center gap-2">
                <RadioGroupItem id="delivery-area-radius-add" value="add" />
                <Label htmlFor="delivery-area-radius-add" className="font-normal">
                  기존에 더하기
                </Label>
              </div>
              <div className="flex items-center gap-2">
                <RadioGroupItem id="delivery-area-radius-replace" value="replace" />
                <Label htmlFor="delivery-area-radius-replace" className="font-normal">
                  덮어쓰기
                </Label>
              </div>
            </RadioGroup>
          </Field>
        )}
      />

      {preview && (
        <p className="text-muted-foreground text-sm">
          반경 안 행정동 <span className="font-medium text-foreground">{preview.adminDongCount}곳</span>
          {preview.unresolvedCount > 0 && ` · 판정 불가 ${preview.unresolvedCount}곳`}
        </p>
      )}

      {/*
        검증 메시지만 띄우고 버튼을 열어 두면 상한을 넘긴 값으로도 제출을 시도할 수 있다.
        `mode: "onChange"` 라 `isValid` 가 입력 즉시 갱신되므로 메시지와 같은 조건으로 잠근다.
      */}
      <Button type="submit" disabled={isPending || !form.formState.isValid}>
        이 반경으로 추가
      </Button>
    </form>
  );
}
