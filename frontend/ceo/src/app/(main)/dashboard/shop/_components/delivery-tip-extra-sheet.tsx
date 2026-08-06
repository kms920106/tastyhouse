"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Trash2 } from "lucide-react";
import { Controller, useFieldArray, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group";
import {
  deleteDeliveryTipDistanceAction,
  deleteDeliveryTipRegionsAction,
  updateDeliveryTipDistanceAction,
  updateDeliveryTipHolidayAction,
  updateDeliveryTipRegionsAction,
  updateDeliveryTipSchedulesAction,
} from "@/feature/shop/actions";
import {
  DAY_TYPE_LABEL,
  DELIVERY_TIP_BASE_DISTANCE_OPTIONS,
  DELIVERY_TIP_EXTRA_UPPER_BOUND,
  DELIVERY_TIP_SCHEDULE_DAY_TYPE_OPTIONS,
  DELIVERY_TIP_SCHEDULE_DISALLOWED_DAY_TYPES,
  DELIVERY_TIP_SURCHARGE_RULES,
  DELIVERY_TIP_SURCHARGE_UNIT_LABEL,
  DELIVERY_TIP_UNSET,
  EXTRA_DELIVERY_TIP_TYPE_LABEL,
  EXTRA_DELIVERY_TIP_TYPES,
} from "@/feature/shop/constants";
import type {
  DeliveryTipSurchargeUnit,
  ExtraDeliveryTipType,
  ShopDeliveryArea,
  ShopDeliveryTipSetting,
} from "@/feature/shop/domain";
import { SHOP_MESSAGE, SHOP_OPERATION_COPY } from "@/feature/shop/message";
import {
  type DeliveryTipDistanceFormValues,
  type DeliveryTipHolidayFormValues,
  type DeliveryTipRegionsFormValues,
  type DeliveryTipSchedulesFormValues,
  deliveryTipDistanceSchema,
  deliveryTipHolidaySchema,
  deliveryTipRegionsSchema,
  deliveryTipSchedulesSchema,
} from "@/feature/shop/schema";
import { formatTimeLabel } from "@/feature/shop/time";

interface DeliveryTipExtraSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  deliveryTip: ShopDeliveryTipSetting;
  deliveryAreas: ShopDeliveryArea[];
}

const DEFAULT_BASE_DISTANCE_METERS = DELIVERY_TIP_BASE_DISTANCE_OPTIONS[1];
const DEFAULT_SURCHARGE_UNIT: DeliveryTipSurchargeUnit = "PER_500M";
/** 거리별 미설정 상태에서 시작할 기본 배달팁·할증액 — PDF 예시(1.5km 2,500원 + 500m당 500원) 기준 */
const DEFAULT_BASE_TIP_AMOUNT = 2500;
const DEFAULT_SURCHARGE_AMOUNT = 500;
const DEFAULT_SCHEDULE_ROW = {
  dayType: "MONDAY" as const,
  startTime: "00:00:00",
  endTime: "02:00:00",
  tipAmount: 1000,
};

/** 시간별 배달팁 요일 칩 — 일요일·공휴일은 선택할 수 없다(서버가 거부) */
const SCHEDULE_DAY_TYPE_CHIPS = DELIVERY_TIP_SCHEDULE_DAY_TYPE_OPTIONS.map((dayType) => ({
  dayType,
  disabled: (DELIVERY_TIP_SCHEDULE_DISALLOWED_DAY_TYPES as readonly string[]).includes(dayType),
}));

export function DeliveryTipExtraSheet({
  open,
  onOpenChange,
  shopId,
  deliveryTip,
  deliveryAreas,
}: DeliveryTipExtraSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const [extraTipType, setExtraTipType] = React.useState<ExtraDeliveryTipType>(deliveryTip.extraTipType);

  const distanceForm = useForm<DeliveryTipDistanceFormValues>({
    resolver: zodResolver(deliveryTipDistanceSchema),
    defaultValues: {
      baseDistanceMeters: DEFAULT_BASE_DISTANCE_METERS,
      baseTipAmount: 2500,
      surchargeUnit: DEFAULT_SURCHARGE_UNIT,
      surchargeAmount: 500,
    },
  });

  const regionsForm = useForm<DeliveryTipRegionsFormValues>({
    resolver: zodResolver(deliveryTipRegionsSchema),
    defaultValues: { regions: [] },
  });
  const regionFields = useFieldArray({ control: regionsForm.control, name: "regions" });

  const schedulesForm = useForm<DeliveryTipSchedulesFormValues>({
    resolver: zodResolver(deliveryTipSchedulesSchema),
    defaultValues: { schedules: [DEFAULT_SCHEDULE_ROW] },
  });
  const scheduleFields = useFieldArray({ control: schedulesForm.control, name: "schedules" });

  const holidayForm = useForm<DeliveryTipHolidayFormValues>({
    resolver: zodResolver(deliveryTipHolidaySchema),
    defaultValues: { tipAmount: DELIVERY_TIP_UNSET },
  });

  React.useEffect(() => {
    if (!open) return;

    setExtraTipType(deliveryTip.extraTipType);
    // 거리별 미설정(distance === null)이면 편집 기본값으로 시작한다.
    // 기본배달거리 구간의 기본 배달팁은 조회 응답에 없으므로 첫 구간 배달팁을 초기값으로 제안한다.
    const { distance, tiers } = deliveryTip;
    distanceForm.reset(
      distance
        ? {
            baseDistanceMeters: distance.baseDistanceMeters,
            baseTipAmount: tiers.length > 0 ? tiers[0].tipAmount : DEFAULT_BASE_TIP_AMOUNT,
            surchargeUnit: distance.surchargeUnit,
            surchargeAmount: distance.surchargeAmount,
          }
        : {
            baseDistanceMeters: DEFAULT_BASE_DISTANCE_METERS,
            baseTipAmount: tiers.length > 0 ? tiers[0].tipAmount : DEFAULT_BASE_TIP_AMOUNT,
            surchargeUnit: DEFAULT_SURCHARGE_UNIT,
            surchargeAmount: DEFAULT_SURCHARGE_AMOUNT,
          },
    );
    regionsForm.reset({
      regions: deliveryTip.regions.map((region) => ({ adminDongId: region.adminDongId, tipAmount: region.tipAmount })),
    });
    schedulesForm.reset({
      schedules:
        deliveryTip.schedules.length > 0
          ? deliveryTip.schedules.map((schedule) => ({
              dayType: schedule.dayType,
              startTime: schedule.startTime,
              endTime: schedule.endTime,
              tipAmount: schedule.tipAmount,
            }))
          : [DEFAULT_SCHEDULE_ROW],
    });
    holidayForm.reset({ tipAmount: deliveryTip.holidayTipAmount });
  }, [open, deliveryTip, distanceForm, regionsForm, schedulesForm, holidayForm]);

  const runAction = (action: () => Promise<{ success: boolean; message?: string }>, successMessage: string) => {
    startTransition(async () => {
      const { success, message } = await action();
      if (success) toast.success(successMessage);
      else toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
    });
  };

  // 세그먼트를 바꿔 저장하면 서버가 반대편을 자동 해제한다.
  // '사용 안 함' 을 고른 경우에만 현재 설정된 쪽의 DELETE 를 호출한다.
  const saveExtraTipType = () => {
    if (extraTipType === "DISTANCE") {
      void distanceForm.handleSubmit((values) =>
        runAction(
          () => updateDeliveryTipDistanceAction(shopId, values),
          SHOP_MESSAGE.EXTRA_DELIVERY_TIP_UPDATE_SUCCESS,
        ),
      )();
      return;
    }
    if (extraTipType === "REGION") {
      void regionsForm.handleSubmit((values) =>
        runAction(() => updateDeliveryTipRegionsAction(shopId, values), SHOP_MESSAGE.EXTRA_DELIVERY_TIP_UPDATE_SUCCESS),
      )();
      return;
    }

    if (deliveryTip.extraTipType === "NONE") {
      toast.success(SHOP_MESSAGE.DELIVERY_TIP_REMOVE_SUCCESS);
      return;
    }
    const removeAction =
      deliveryTip.extraTipType === "DISTANCE"
        ? () => deleteDeliveryTipDistanceAction(shopId)
        : () => deleteDeliveryTipRegionsAction(shopId);
    runAction(removeAction, SHOP_MESSAGE.DELIVERY_TIP_REMOVE_SUCCESS);
  };

  const surchargeUnit = distanceForm.watch("surchargeUnit");
  const surchargeRule = DELIVERY_TIP_SURCHARGE_RULES[surchargeUnit];
  const hasDeliveryAreas = deliveryAreas.length > 0;
  const selectedAdminDongIds = regionsForm.watch("regions").map((region) => region.adminDongId);

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-lg">
        <SheetHeader>
          <SheetTitle>{SHOP_OPERATION_COPY.EXTRA_DELIVERY_TIP_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_OPERATION_COPY.EXTRA_DELIVERY_TIP_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-6 overflow-y-auto px-4">
          {/* ===== 1. 거리별 / 지역별 (택1) ===== */}
          <section className="space-y-3">
            <div className="flex flex-col gap-1">
              <span className="font-medium text-sm">거리별 · 지역별</span>
              <span className="text-muted-foreground text-xs leading-snug">
                {SHOP_OPERATION_COPY.DISTANCE_REGION_EXCLUSIVE_GUIDE}
              </span>
            </div>

            <ToggleGroup
              type="single"
              variant="outline"
              value={extraTipType}
              onValueChange={(value) => value && setExtraTipType(value as ExtraDeliveryTipType)}
              className="w-full"
              disabled={isPending}
            >
              {EXTRA_DELIVERY_TIP_TYPES.map((type) => (
                <ToggleGroupItem key={type} value={type} className="flex-1">
                  {EXTRA_DELIVERY_TIP_TYPE_LABEL[type]}
                </ToggleGroupItem>
              ))}
            </ToggleGroup>

            {extraTipType === "DISTANCE" && (
              <FieldGroup className="gap-4">
                <Controller
                  control={distanceForm.control}
                  name="baseDistanceMeters"
                  render={({ field, fieldState }) => (
                    <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor="delivery-tip-base-distance">기본배달거리</FieldLabel>
                      <Select
                        value={String(field.value ?? "")}
                        onValueChange={(value) => field.onChange(Number(value))}
                        disabled={isPending}
                      >
                        <SelectTrigger id="delivery-tip-base-distance">
                          <SelectValue placeholder="기본배달거리를 선택해 주세요." />
                        </SelectTrigger>
                        <SelectContent>
                          {DELIVERY_TIP_BASE_DISTANCE_OPTIONS.map((meters) => (
                            <SelectItem key={meters} value={String(meters)}>
                              {meters / 1000}km
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <FieldDescription>기본배달거리까지는 할증 배달팁이 붙지 않습니다.</FieldDescription>
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                  )}
                />

                <Controller
                  control={distanceForm.control}
                  name="baseTipAmount"
                  render={({ field, fieldState }) => (
                    <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor="delivery-tip-base-amount">기본 배달팁</FieldLabel>
                      <Input
                        id="delivery-tip-base-amount"
                        type="number"
                        inputMode="numeric"
                        min={0}
                        max={DELIVERY_TIP_EXTRA_UPPER_BOUND}
                        value={field.value ?? ""}
                        onChange={(e) => field.onChange(e.target.value === "" ? undefined : Number(e.target.value))}
                        aria-invalid={fieldState.invalid}
                        disabled={isPending}
                      />
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                  )}
                />

                <Controller
                  control={distanceForm.control}
                  name="surchargeUnit"
                  render={({ field, fieldState }) => (
                    <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor="delivery-tip-surcharge-unit">추가 거리 할증 단위</FieldLabel>
                      <Select
                        value={field.value ?? ""}
                        onValueChange={(value) => {
                          field.onChange(value as DeliveryTipSurchargeUnit);
                          // 단위가 바뀌면 허용 범위도 바뀌므로 금액을 즉시 재검증한다
                          void distanceForm.trigger("surchargeAmount");
                        }}
                        disabled={isPending}
                      >
                        <SelectTrigger id="delivery-tip-surcharge-unit">
                          <SelectValue placeholder="할증 단위를 선택해 주세요." />
                        </SelectTrigger>
                        <SelectContent>
                          {(Object.keys(DELIVERY_TIP_SURCHARGE_RULES) as DeliveryTipSurchargeUnit[]).map((unit) => (
                            <SelectItem key={unit} value={unit}>
                              {DELIVERY_TIP_SURCHARGE_UNIT_LABEL[unit]}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                  )}
                />

                <Controller
                  control={distanceForm.control}
                  name="surchargeAmount"
                  render={({ field, fieldState }) => (
                    <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor="delivery-tip-surcharge-amount">
                        {DELIVERY_TIP_SURCHARGE_UNIT_LABEL[surchargeUnit]} 할증 금액
                      </FieldLabel>
                      <Input
                        id="delivery-tip-surcharge-amount"
                        type="number"
                        inputMode="numeric"
                        min={surchargeRule.min}
                        max={surchargeRule.max}
                        value={field.value ?? ""}
                        onChange={(e) => field.onChange(e.target.value === "" ? undefined : Number(e.target.value))}
                        aria-invalid={fieldState.invalid}
                        disabled={isPending}
                      />
                      <FieldDescription>
                        {`${DELIVERY_TIP_SURCHARGE_UNIT_LABEL[surchargeUnit]} ${surchargeRule.min.toLocaleString("ko-KR")}원 ~ ${surchargeRule.max.toLocaleString("ko-KR")}원`}
                      </FieldDescription>
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                  )}
                />
              </FieldGroup>
            )}

            {extraTipType === "REGION" &&
              (hasDeliveryAreas ? (
                <FieldGroup className="gap-4">
                  {regionFields.fields.map((field, index) => (
                    <div key={field.id} className="flex flex-col gap-3 rounded-md border p-3">
                      <div className="flex items-center justify-between">
                        <span className="font-medium text-sm">지역 {index + 1}</span>
                        <Button
                          type="button"
                          size="icon"
                          variant="ghost"
                          onClick={() => regionFields.remove(index)}
                          disabled={isPending}
                          aria-label={`지역 ${index + 1} 삭제`}
                        >
                          <Trash2 className="size-4" />
                        </Button>
                      </div>

                      <Controller
                        control={regionsForm.control}
                        name={`regions.${index}.adminDongId`}
                        render={({ field: regionField, fieldState }) => (
                          <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor={`delivery-tip-region-${index}`}>배달가능지역</FieldLabel>
                            <Select
                              value={regionField.value ? String(regionField.value) : ""}
                              onValueChange={(value) => regionField.onChange(Number(value))}
                              disabled={isPending}
                            >
                              <SelectTrigger id={`delivery-tip-region-${index}`}>
                                <SelectValue placeholder="지역을 선택해 주세요." />
                              </SelectTrigger>
                              <SelectContent>
                                {deliveryAreas.map((area) => (
                                  <SelectItem
                                    key={area.id}
                                    value={String(area.adminDongId)}
                                    // 이미 다른 행에서 고른 행정동은 중복 선택할 수 없다
                                    disabled={
                                      area.adminDongId !== regionField.value &&
                                      selectedAdminDongIds.includes(area.adminDongId)
                                    }
                                  >
                                    {area.regionName}
                                  </SelectItem>
                                ))}
                              </SelectContent>
                            </Select>
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                          </Field>
                        )}
                      />

                      <Controller
                        control={regionsForm.control}
                        name={`regions.${index}.tipAmount`}
                        render={({ field: tipField, fieldState }) => (
                          <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor={`delivery-tip-region-amount-${index}`}>추가 배달팁</FieldLabel>
                            <Input
                              id={`delivery-tip-region-amount-${index}`}
                              type="number"
                              inputMode="numeric"
                              min={0}
                              max={DELIVERY_TIP_EXTRA_UPPER_BOUND}
                              value={tipField.value ?? ""}
                              onChange={(e) =>
                                tipField.onChange(e.target.value === "" ? undefined : Number(e.target.value))
                              }
                              aria-invalid={fieldState.invalid}
                              disabled={isPending}
                            />
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                          </Field>
                        )}
                      />
                    </div>
                  ))}

                  {regionsForm.formState.errors.regions?.message && (
                    <FieldError errors={[{ message: regionsForm.formState.errors.regions.message }]} />
                  )}

                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => regionFields.append({ adminDongId: 0, tipAmount: 1000 })}
                    disabled={isPending || regionFields.fields.length >= deliveryAreas.length}
                  >
                    <Plus className="size-4" />
                    지역 추가
                  </Button>
                </FieldGroup>
              ) : (
                <p className="rounded-md border border-dashed p-4 text-center text-muted-foreground text-sm">
                  {SHOP_OPERATION_COPY.DELIVERY_AREA_EMPTY_GUIDE}
                </p>
              ))}

            <Button
              type="button"
              onClick={saveExtraTipType}
              disabled={isPending || (extraTipType === "REGION" && !hasDeliveryAreas)}
            >
              {isPending ? "저장 중..." : "거리별 · 지역별 저장"}
            </Button>
          </section>

          <Separator />

          {/* ===== 2. 시간별 ===== */}
          <section className="space-y-3">
            <div className="flex flex-col gap-1">
              <span className="font-medium text-sm">시간별</span>
              <span className="text-muted-foreground text-xs leading-snug">
                요일과 시간대별로 추가 배달팁을 받을 수 있습니다. 일요일은 선택할 수 없습니다.
              </span>
            </div>

            <FieldGroup className="gap-4">
              {scheduleFields.fields.map((field, index) => (
                <div key={field.id} className="flex flex-col gap-3 rounded-md border p-3">
                  <div className="flex items-center justify-between">
                    <span className="font-medium text-sm">시간대 {index + 1}</span>
                    <Button
                      type="button"
                      size="icon"
                      variant="ghost"
                      onClick={() => scheduleFields.remove(index)}
                      disabled={isPending || scheduleFields.fields.length === 1}
                      aria-label={`시간대 ${index + 1} 삭제`}
                    >
                      <Trash2 className="size-4" />
                    </Button>
                  </div>

                  <Controller
                    control={schedulesForm.control}
                    name={`schedules.${index}.dayType`}
                    render={({ field: dayField, fieldState }) => (
                      <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                        <FieldLabel>요일</FieldLabel>
                        <ToggleGroup
                          type="single"
                          variant="outline"
                          value={dayField.value ?? ""}
                          onValueChange={(value) => value && dayField.onChange(value)}
                          className="flex-wrap justify-start"
                          disabled={isPending}
                        >
                          {SCHEDULE_DAY_TYPE_CHIPS.map(({ dayType, disabled }) => (
                            <ToggleGroupItem key={dayType} value={dayType} disabled={disabled}>
                              {DAY_TYPE_LABEL[dayType]}
                            </ToggleGroupItem>
                          ))}
                        </ToggleGroup>
                        {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                      </Field>
                    )}
                  />

                  <div className="flex gap-3">
                    <Controller
                      control={schedulesForm.control}
                      name={`schedules.${index}.startTime`}
                      render={({ field: startField, fieldState }) => (
                        <Field className="flex-1 gap-1.5" data-invalid={fieldState.invalid}>
                          <FieldLabel htmlFor={`delivery-tip-schedule-start-${index}`}>시작</FieldLabel>
                          <Input
                            id={`delivery-tip-schedule-start-${index}`}
                            type="time"
                            step={300}
                            value={formatTimeLabel(startField.value ?? "")}
                            onChange={(e) => startField.onChange(`${e.target.value}:00`)}
                            aria-invalid={fieldState.invalid}
                            disabled={isPending}
                          />
                          {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                        </Field>
                      )}
                    />

                    <Controller
                      control={schedulesForm.control}
                      name={`schedules.${index}.endTime`}
                      render={({ field: endField, fieldState }) => (
                        <Field className="flex-1 gap-1.5" data-invalid={fieldState.invalid}>
                          <FieldLabel htmlFor={`delivery-tip-schedule-end-${index}`}>종료</FieldLabel>
                          <Input
                            id={`delivery-tip-schedule-end-${index}`}
                            type="time"
                            step={300}
                            value={formatTimeLabel(endField.value ?? "")}
                            onChange={(e) => endField.onChange(`${e.target.value}:00`)}
                            aria-invalid={fieldState.invalid}
                            disabled={isPending}
                          />
                          {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                        </Field>
                      )}
                    />
                  </div>

                  <Controller
                    control={schedulesForm.control}
                    name={`schedules.${index}.tipAmount`}
                    render={({ field: tipField, fieldState }) => (
                      <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                        <FieldLabel htmlFor={`delivery-tip-schedule-amount-${index}`}>추가 배달팁</FieldLabel>
                        <Input
                          id={`delivery-tip-schedule-amount-${index}`}
                          type="number"
                          inputMode="numeric"
                          min={0}
                          max={DELIVERY_TIP_EXTRA_UPPER_BOUND}
                          value={tipField.value ?? ""}
                          onChange={(e) =>
                            tipField.onChange(e.target.value === "" ? undefined : Number(e.target.value))
                          }
                          aria-invalid={fieldState.invalid}
                          disabled={isPending}
                        />
                        {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                      </Field>
                    )}
                  />
                </div>
              ))}

              {schedulesForm.formState.errors.schedules?.message && (
                <FieldError errors={[{ message: schedulesForm.formState.errors.schedules.message }]} />
              )}

              <Button
                type="button"
                variant="outline"
                onClick={() => scheduleFields.append(DEFAULT_SCHEDULE_ROW)}
                disabled={isPending}
              >
                <Plus className="size-4" />
                시간대 추가
              </Button>

              <Button
                type="button"
                onClick={schedulesForm.handleSubmit((values) =>
                  runAction(
                    () => updateDeliveryTipSchedulesAction(shopId, values),
                    SHOP_MESSAGE.EXTRA_DELIVERY_TIP_UPDATE_SUCCESS,
                  ),
                )}
                disabled={isPending}
              >
                {isPending ? "저장 중..." : "시간별 저장"}
              </Button>
            </FieldGroup>
          </section>

          <Separator />

          {/* ===== 3. 공휴일 ===== */}
          <section className="space-y-3">
            <div className="flex flex-col gap-1">
              <span className="font-medium text-sm">공휴일</span>
              <span className="text-muted-foreground text-xs leading-snug">
                {SHOP_OPERATION_COPY.HOLIDAY_TIP_SUNDAY_GUIDE}
              </span>
            </div>

            <FieldGroup className="gap-4">
              <Controller
                control={holidayForm.control}
                name="tipAmount"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="delivery-tip-holiday-amount">추가 배달팁</FieldLabel>
                    <Input
                      id="delivery-tip-holiday-amount"
                      type="number"
                      inputMode="numeric"
                      min={0}
                      max={DELIVERY_TIP_EXTRA_UPPER_BOUND}
                      value={field.value ?? ""}
                      onChange={(e) => field.onChange(e.target.value === "" ? undefined : Number(e.target.value))}
                      aria-invalid={fieldState.invalid}
                      disabled={isPending}
                    />
                    <FieldDescription>법정 공휴일에 일괄 부과됩니다. 0을 입력하면 해제됩니다.</FieldDescription>
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Button
                type="button"
                onClick={holidayForm.handleSubmit((values) =>
                  runAction(
                    () => updateDeliveryTipHolidayAction(shopId, values),
                    values.tipAmount === DELIVERY_TIP_UNSET
                      ? SHOP_MESSAGE.DELIVERY_TIP_REMOVE_SUCCESS
                      : SHOP_MESSAGE.EXTRA_DELIVERY_TIP_UPDATE_SUCCESS,
                  ),
                )}
                disabled={isPending}
              >
                {isPending ? "저장 중..." : "공휴일 저장"}
              </Button>
            </FieldGroup>
          </section>
        </div>

        <SheetFooter>
          <SheetClose asChild>
            <Button variant="outline" disabled={isPending}>
              닫기
            </Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
