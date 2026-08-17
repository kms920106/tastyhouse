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
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { HOUR_OPTIONS, MINUTE_OPTIONS, SOLD_OUT_UNTIL_MAX_DAYS } from "@/feature/product/constants";
import { formatHourLabel, formatMinuteLabel } from "@/feature/product/format";
import { PRODUCT_AVAILABILITY_COPY } from "@/feature/product/message";
import {
  type SoldOutUntilFormValues,
  soldOutUntilSchema,
  toLocalDateTimeString,
  toSoldOutUntilDate,
} from "@/feature/product/schema";
import { formatDate } from "@/lib/date";

interface SoldOutPeriodDialogProps {
  open: boolean;
  pending?: boolean;
  onOpenChange: (open: boolean) => void;
  /** 조립된 `yyyy-MM-ddTHH:mm:ss` 문자열을 넘긴다 */
  onApply: (soldOutUntil: string) => void;
}

const DEFAULT_VALUES: SoldOutUntilFormValues = { date: "", hour: "", minute: "" };

export function SoldOutPeriodDialog({ open, pending, onOpenChange, onApply }: SoldOutPeriodDialogProps) {
  const form = useForm<SoldOutUntilFormValues>({
    resolver: zodResolver(soldOutUntilSchema),
    defaultValues: DEFAULT_VALUES,
  });

  // 열 때마다 오늘 날짜로 초기화한다. `min`/`max` 도 열린 시점 기준으로 계산해야
  // 자정을 넘겨 열어둔 다이얼로그가 어제 날짜를 허용하지 않는다.
  const [dateBounds, setDateBounds] = React.useState(() => ({ min: "", max: "" }));

  React.useEffect(() => {
    if (!open) return;

    const today = new Date();
    const maxDate = new Date(today);
    maxDate.setDate(maxDate.getDate() + SOLD_OUT_UNTIL_MAX_DAYS);

    setDateBounds({ min: formatDate(today), max: formatDate(maxDate) });
    form.reset({ ...DEFAULT_VALUES, date: formatDate(today) });
  }, [open, form]);

  const onSubmit = (values: SoldOutUntilFormValues) => {
    const target = toSoldOutUntilDate(values.date, values.hour, values.minute);
    // 스키마가 이미 실존 날짜를 보장하지만, 타입을 좁히기 위해 한 번 더 확인한다.
    if (target === null) return;

    onApply(toLocalDateTimeString(target));
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{PRODUCT_AVAILABILITY_COPY.DIALOG_PERIOD_TITLE}</DialogTitle>
          <DialogDescription>{PRODUCT_AVAILABILITY_COPY.DIALOG_PERIOD_DESCRIPTION}</DialogDescription>
        </DialogHeader>

        <form id="sold-out-period-form" noValidate onSubmit={form.handleSubmit(onSubmit)}>
          <FieldGroup className="gap-4">
            <Controller
              control={form.control}
              name="date"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="sold-out-period-date">
                    {PRODUCT_AVAILABILITY_COPY.DIALOG_PERIOD_DATE_LABEL}
                  </FieldLabel>
                  <Input
                    id="sold-out-period-date"
                    type="date"
                    min={dateBounds.min}
                    max={dateBounds.max}
                    value={field.value}
                    onChange={field.onChange}
                    aria-invalid={fieldState.invalid}
                    disabled={pending}
                  />
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />

            <div className="grid grid-cols-2 gap-4">
              <Controller
                control={form.control}
                name="hour"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="sold-out-period-hour">
                      {PRODUCT_AVAILABILITY_COPY.DIALOG_PERIOD_HOUR_LABEL}
                    </FieldLabel>
                    {/* Radix Select 의 value 는 lifetime 내내 문자열이어야 한다 — undefined 로
                        뒤집히면 uncontrolled → controlled 경고가 난다. */}
                    <Select value={field.value ?? ""} onValueChange={field.onChange} disabled={pending}>
                      <SelectTrigger id="sold-out-period-hour" aria-invalid={fieldState.invalid}>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent position="popper">
                        <SelectGroup>
                          {HOUR_OPTIONS.map((hour) => (
                            <SelectItem key={hour} value={String(hour)}>
                              {formatHourLabel(hour)}
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
                name="minute"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="sold-out-period-minute">
                      {PRODUCT_AVAILABILITY_COPY.DIALOG_PERIOD_MINUTE_LABEL}
                    </FieldLabel>
                    <Select value={field.value ?? ""} onValueChange={field.onChange} disabled={pending}>
                      <SelectTrigger id="sold-out-period-minute" aria-invalid={fieldState.invalid}>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent position="popper">
                        <SelectGroup>
                          {MINUTE_OPTIONS.map((minute) => (
                            <SelectItem key={minute} value={String(minute)}>
                              {formatMinuteLabel(minute)}
                            </SelectItem>
                          ))}
                        </SelectGroup>
                      </SelectContent>
                    </Select>
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />
            </div>
          </FieldGroup>
        </form>

        <DialogFooter>
          <Button type="submit" form="sold-out-period-form" disabled={pending}>
            {PRODUCT_AVAILABILITY_COPY.DIALOG_PERIOD_APPLY}
          </Button>
          <DialogClose asChild>
            <Button variant="outline" disabled={pending}>
              {PRODUCT_AVAILABILITY_COPY.DIALOG_PERIOD_CANCEL}
            </Button>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
