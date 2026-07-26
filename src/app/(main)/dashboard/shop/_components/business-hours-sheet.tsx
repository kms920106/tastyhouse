"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Field, FieldError, FieldGroup, FieldLabel, FieldLegend, FieldSet } from "@/components/ui/field";
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
import { Switch } from "@/components/ui/switch";
import {
  createBreakTimeAction,
  createBusinessHourAction,
  deleteBreakTimeAction,
  updateBreakTimeAction,
  updateBusinessHourAction,
} from "@/feature/shop/actions";
import { DAY_TYPE_LABEL, WEEKDAY_OPTIONS, type WeekdayOption } from "@/feature/shop/constants";
import type { BreakTime, BusinessHour, DayType } from "@/feature/shop/domain";
import { SHOP_MESSAGE, SHOP_OPERATION_COPY } from "@/feature/shop/message";
import { type BusinessHourFormValues, businessHourFormSchema } from "@/feature/shop/schema";
import { clampRangeToBusinessHours } from "@/feature/shop/time";

import { TimeSelect } from "./time-select";

const DEFAULT_OPEN_TIME = "09:00:00";
const DEFAULT_CLOSE_TIME = "22:00:00";
const DEFAULT_BREAK_START = "15:00:00";
const DEFAULT_BREAK_END = "17:00:00";

interface BusinessHoursSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  dayType: WeekdayOption;
  businessHour?: BusinessHour;
  breakTime?: BreakTime;
  /** 요일별 기존 항목 조회용 — "다른 요일에도 동일하게 설정" 적용 시 update/create/delete 를 정확히 분기하기 위함 */
  businessHourByDay: ReadonlyMap<DayType, BusinessHour>;
  breakTimeByDay: ReadonlyMap<DayType, BreakTime>;
}

export function BusinessHoursSheet({
  open,
  onOpenChange,
  shopId,
  dayType,
  businessHour,
  breakTime,
  businessHourByDay,
  breakTimeByDay,
}: BusinessHoursSheetProps) {
  const [isPending, startTransition] = React.useTransition();

  const form = useForm<BusinessHourFormValues>({
    resolver: zodResolver(businessHourFormSchema),
    defaultValues: {
      dayType,
      isClosed: false,
      is24Hours: false,
      openTime: DEFAULT_OPEN_TIME,
      closeTime: DEFAULT_CLOSE_TIME,
      breakTimeEnabled: false,
      breakTime: { startTime: DEFAULT_BREAK_START, endTime: DEFAULT_BREAK_END },
      applyToDays: [],
    },
  });

  React.useEffect(() => {
    if (!open) return;

    form.reset({
      dayType,
      isClosed: businessHour?.isClosed ?? false,
      is24Hours: businessHour?.is24Hours ?? false,
      openTime: businessHour?.openTime ?? DEFAULT_OPEN_TIME,
      closeTime: businessHour?.closeTime ?? DEFAULT_CLOSE_TIME,
      breakTimeEnabled: Boolean(breakTime),
      breakTime: {
        startTime: breakTime?.startTime ?? DEFAULT_BREAK_START,
        endTime: breakTime?.endTime ?? DEFAULT_BREAK_END,
      },
      applyToDays: [],
    });
  }, [open, dayType, businessHour, breakTime, form]);

  const isClosed = form.watch("isClosed");
  const is24Hours = form.watch("is24Hours");
  const breakTimeEnabled = form.watch("breakTimeEnabled");
  const timeInputsDisabled = isPending || isClosed || is24Hours;

  /**
   * 영업시간을 좁히면 그 범위를 벗어난 휴게시간을 새 범위로 당겨 맞춘다.
   * PDF "영업시간을 변경하면 다른 설정에도 영향이 있어요" 안내에 대응하는 동작이다.
   */
  const clampBreakTime = React.useCallback(
    (openTime: string, closeTime: string) => {
      if (!form.getValues("breakTimeEnabled")) return;

      const current = form.getValues("breakTime");
      const clamped = clampRangeToBusinessHours(openTime, closeTime, current.startTime, current.endTime);
      if (clamped.startTime !== current.startTime || clamped.endTime !== current.endTime) {
        form.setValue("breakTime", clamped, { shouldValidate: true });
      }
    },
    [form],
  );

  const onSubmit = (values: BusinessHourFormValues) => {
    // 닫힘으로 설정하면 휴게시간도 함께 비활성화한다 — 폼 상태와 무관하게 항상 해제된 것으로 처리.
    const breakTimeEnabled = values.breakTimeEnabled && !values.isClosed;

    // 요일 다중선택 시 선택된 요일 수만큼 순차 호출하고, 실패한 요일을 토스트에 명시한다.
    // 각 요일은 자신의 기존 항목(businessHourByDay 등)을 기준으로 update/create/delete 를 독립적으로 판단한다.
    const targetDays: WeekdayOption[] = [values.dayType as WeekdayOption, ...values.applyToDays].filter(
      (day, index, all) => all.indexOf(day) === index,
    );

    startTransition(async () => {
      const failedDays: string[] = [];
      const committedDays: string[] = [];

      for (const day of targetDays) {
        const existingBusinessHour = businessHourByDay.get(day);
        const existingBreakTime = breakTimeByDay.get(day);

        const businessHourPayload = {
          dayType: day,
          openTime: values.openTime,
          closeTime: values.closeTime,
          isClosed: values.isClosed,
          is24Hours: values.is24Hours,
        };

        const businessHourResult = existingBusinessHour
          ? await updateBusinessHourAction(existingBusinessHour.id, businessHourPayload)
          : await createBusinessHourAction(shopId, businessHourPayload);

        if (!businessHourResult.success) {
          failedDays.push(DAY_TYPE_LABEL[day]);
          continue;
        }

        const breakResult = breakTimeEnabled
          ? existingBreakTime
            ? await updateBreakTimeAction(existingBreakTime.id, { dayType: day, ...values.breakTime })
            : await createBreakTimeAction(shopId, { dayType: day, ...values.breakTime })
          : existingBreakTime
            ? await deleteBreakTimeAction(existingBreakTime.id)
            : { success: true };

        if (!breakResult.success) {
          failedDays.push(DAY_TYPE_LABEL[day]);
        } else {
          committedDays.push(DAY_TYPE_LABEL[day]);
        }
      }

      if (failedDays.length === 0) {
        toast.success(SHOP_MESSAGE.BUSINESS_HOUR_SAVE_SUCCESS);
        onOpenChange(false);
      } else if (committedDays.length === 0) {
        toast.error(`${failedDays.join(", ")} 저장에 실패했습니다.`);
      } else {
        // 일부 요일은 영업시간까지는 저장됐고 휴게시간만 실패했을 수 있어, 성공/실패 요일을 모두 알린다.
        toast.error(`${committedDays.join(", ")}은(는) 저장됨, ${failedDays.join(", ")}은(는) 저장에 실패했습니다.`);
      }
    });
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{DAY_TYPE_LABEL[dayType]} 영업시간</SheetTitle>
          <SheetDescription>{SHOP_OPERATION_COPY.BUSINESS_HOURS_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <form
          id="business-hours-form"
          noValidate
          onSubmit={form.handleSubmit(onSubmit)}
          className="flex-1 overflow-y-auto px-4"
        >
          <FieldGroup className="gap-5">
            <Controller
              control={form.control}
              name="isClosed"
              render={({ field }) => (
                <Field orientation="horizontal">
                  <FieldLabel htmlFor="business-hour-closed">{SHOP_OPERATION_COPY.CLOSED}</FieldLabel>
                  <Switch
                    id="business-hour-closed"
                    checked={field.value}
                    disabled={isPending}
                    onCheckedChange={(checked) => {
                      field.onChange(checked);
                      // 휴무로 전환하면 휴게시간 토글도 화면상 함께 꺼서 실제 저장 동작과 일치시킨다.
                      if (checked) form.setValue("breakTimeEnabled", false);
                    }}
                  />
                </Field>
              )}
            />

            <Controller
              control={form.control}
              name="is24Hours"
              render={({ field }) => (
                <Field orientation="horizontal">
                  <FieldLabel htmlFor="business-hour-24-hours">{SHOP_OPERATION_COPY.ALL_DAY}</FieldLabel>
                  <Switch
                    id="business-hour-24-hours"
                    checked={field.value}
                    disabled={isPending || isClosed}
                    onCheckedChange={field.onChange}
                  />
                </Field>
              )}
            />

            <FieldSet>
              <FieldLegend variant="label">영업시간</FieldLegend>
              <div className="flex items-center gap-2">
                <Controller
                  control={form.control}
                  name="openTime"
                  render={({ field }) => (
                    <TimeSelect
                      id="business-hour-open"
                      value={field.value}
                      disabled={timeInputsDisabled}
                      onChange={(value) => {
                        field.onChange(value);
                        clampBreakTime(value, form.getValues("closeTime"));
                      }}
                    />
                  )}
                />
                <span className="text-muted-foreground text-sm">~</span>
                <Controller
                  control={form.control}
                  name="closeTime"
                  render={({ field, fieldState }) => (
                    <div className="flex flex-col gap-1">
                      <TimeSelect
                        id="business-hour-close"
                        value={field.value}
                        disabled={timeInputsDisabled}
                        onChange={(value) => {
                          field.onChange(value);
                          clampBreakTime(form.getValues("openTime"), value);
                        }}
                      />
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </div>
                  )}
                />
              </div>
            </FieldSet>

            <Separator />

            <Controller
              control={form.control}
              name="breakTimeEnabled"
              render={({ field }) => (
                <Field orientation="horizontal">
                  <FieldLabel htmlFor="break-time-enabled">{SHOP_OPERATION_COPY.BREAK_TIME_TOGGLE}</FieldLabel>
                  <Switch
                    id="break-time-enabled"
                    checked={field.value}
                    disabled={isPending || isClosed}
                    onCheckedChange={(checked) => {
                      field.onChange(checked);
                      if (checked) clampBreakTime(form.getValues("openTime"), form.getValues("closeTime"));
                    }}
                  />
                </Field>
              )}
            />

            {breakTimeEnabled && (
              <div className="flex items-center gap-2">
                <Controller
                  control={form.control}
                  name="breakTime.startTime"
                  render={({ field }) => (
                    <TimeSelect
                      id="break-time-start"
                      value={field.value}
                      disabled={isPending || isClosed}
                      onChange={field.onChange}
                    />
                  )}
                />
                <span className="text-muted-foreground text-sm">~</span>
                <Controller
                  control={form.control}
                  name="breakTime.endTime"
                  render={({ field, fieldState }) => (
                    <div className="flex flex-col gap-1">
                      <TimeSelect
                        id="break-time-end"
                        value={field.value}
                        disabled={isPending || isClosed}
                        onChange={field.onChange}
                      />
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </div>
                  )}
                />
              </div>
            )}

            <Separator />

            <Controller
              control={form.control}
              name="applyToDays"
              render={({ field }) => (
                <FieldSet>
                  <FieldLegend variant="label">{SHOP_OPERATION_COPY.APPLY_TO_OTHER_DAYS}</FieldLegend>
                  <div className="flex flex-wrap gap-3">
                    {WEEKDAY_OPTIONS.filter((option) => option !== dayType).map((option) => (
                      <Field key={option} orientation="horizontal" className="w-auto gap-2">
                        <Checkbox
                          id={`apply-to-${option}`}
                          checked={field.value.includes(option)}
                          disabled={isPending}
                          onCheckedChange={(checked) => {
                            const next = checked
                              ? [...field.value, option]
                              : field.value.filter((item) => item !== option);
                            field.onChange(next);
                          }}
                        />
                        <FieldLabel htmlFor={`apply-to-${option}`} className="font-normal">
                          {DAY_TYPE_LABEL[option]}
                        </FieldLabel>
                      </Field>
                    ))}
                  </div>
                </FieldSet>
              )}
            />

            {is24Hours && (
              <Alert>
                <AlertTitle>{SHOP_OPERATION_COPY.ALL_DAY}</AlertTitle>
                <AlertDescription>24시간 영업을 선택하면 영업시간 입력은 사용되지 않습니다.</AlertDescription>
              </Alert>
            )}
          </FieldGroup>
        </form>

        <SheetFooter>
          <Button type="submit" form="business-hours-form" disabled={isPending}>
            {isPending ? "저장 중..." : "저장"}
          </Button>
          <SheetClose asChild>
            <Button variant="outline" disabled={isPending}>
              취소
            </Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
