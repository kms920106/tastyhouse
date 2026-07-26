"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { format } from "date-fns";
import type { DateRange } from "react-day-picker";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { DateRangePicker } from "@/components/date-range-picker";
import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldGroup, FieldLabel, FieldLegend, FieldSet } from "@/components/ui/field";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
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
  createClosedDayAction,
  createTemporaryClosureAction,
  deleteClosedDayAction,
  deleteTemporaryClosureAction,
  fetchOperationInfoAction,
  updateHolidayClosedAction,
} from "@/feature/shop/actions";
import {
  CLOSED_DAY_TYPE_LABEL,
  CLOSED_DAY_TYPE_OPTIONS,
  type ClosedDayTypeOption,
  REGULAR_CLOSED_DAY_MAX_COUNT,
  TEMPORARY_CLOSURE_MAX_DAYS,
} from "@/feature/shop/constants";
import type { ShopClosedDays } from "@/feature/shop/domain";
import { SHOP_MESSAGE, SHOP_OPERATION_COPY } from "@/feature/shop/message";
import {
  type ClosedDayFormValues,
  closedDaySchema,
  type TemporaryClosureFormValues,
  temporaryClosureSchema,
} from "@/feature/shop/schema";

interface ClosedDaysSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  closedDays: ShopClosedDays;
}

const DATE_FORMAT = "yyyy-MM-dd";

export function ClosedDaysSheet({ open, onOpenChange, shopId, closedDays }: ClosedDaysSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  // props 는 시트가 열릴 때의 스냅샷이라, 생성/삭제 후에는 로컬 상태를 다시 조회해 목록을 갱신한다.
  const [items, setItems] = React.useState<ShopClosedDays>(closedDays);
  // DateRangePicker 는 value 가 undefined 면 최근 30일을 자체 기본값으로 채우므로,
  // 빈 상태를 유지하려면 항상 객체를 넘겨 controlled 로 둔다.
  const [dateRange, setDateRange] = React.useState<DateRange>({ from: undefined, to: undefined });

  const closedDayForm = useForm<ClosedDayFormValues>({
    resolver: zodResolver(closedDaySchema),
    defaultValues: { closedDayType: CLOSED_DAY_TYPE_OPTIONS[0] },
  });

  const temporaryClosureForm = useForm<TemporaryClosureFormValues>({
    resolver: zodResolver(temporaryClosureSchema),
    defaultValues: { startDate: "", endDate: "" },
  });

  React.useEffect(() => {
    if (open) {
      setItems(closedDays);
      closedDayForm.reset({ closedDayType: CLOSED_DAY_TYPE_OPTIONS[0] });
      temporaryClosureForm.reset({ startDate: "", endDate: "" });
      setDateRange({ from: undefined, to: undefined });
    }
  }, [open, closedDays, closedDayForm, temporaryClosureForm]);

  const reload = React.useCallback(() => {
    startTransition(async () => {
      const { success, data } = await fetchOperationInfoAction(shopId);
      if (success && data) setItems(data.closedDays);
    });
  }, [shopId]);

  const isRegularClosedDayMaxReached = items.regularClosedDays.length >= REGULAR_CLOSED_DAY_MAX_COUNT;

  // DateRangePicker 는 Date 를 다루고 폼은 "YYYY-MM-DD" 를 다루므로 선택 시 변환해 동기화한다.
  function handleDateRangeChange(next: DateRange | undefined) {
    setDateRange(next ?? { from: undefined, to: undefined });
    temporaryClosureForm.setValue("startDate", next?.from ? format(next.from, DATE_FORMAT) : "", {
      shouldValidate: true,
    });
    temporaryClosureForm.setValue("endDate", next?.to ? format(next.to, DATE_FORMAT) : "", { shouldValidate: true });
  }

  function handleHolidayClosedChange(checked: boolean) {
    startTransition(async () => {
      const { success, message } = await updateHolidayClosedAction(shopId, { closedOnPublicHolidays: checked });
      if (success) {
        toast.success(SHOP_MESSAGE.HOLIDAY_CLOSED_UPDATE_SUCCESS);
        setItems((previous) => ({ ...previous, closedOnPublicHolidays: checked }));
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  }

  const onCreateClosedDay = (values: ClosedDayFormValues) => {
    startTransition(async () => {
      const { success, message } = await createClosedDayAction(shopId, values);
      if (success) {
        toast.success(SHOP_MESSAGE.CLOSED_DAY_CREATE_SUCCESS);
        reload();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  const onCreateTemporaryClosure = (values: TemporaryClosureFormValues) => {
    startTransition(async () => {
      const { success, message } = await createTemporaryClosureAction(shopId, values);
      if (success) {
        toast.success(SHOP_MESSAGE.TEMPORARY_CLOSURE_CREATE_SUCCESS);
        temporaryClosureForm.reset({ startDate: "", endDate: "" });
        setDateRange({ from: undefined, to: undefined });
        reload();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  function handleDeleteClosedDay(id: number) {
    startTransition(async () => {
      const { success, message } = await deleteClosedDayAction(id);
      if (success) {
        toast.success(SHOP_MESSAGE.CLOSED_DAY_DELETE_SUCCESS);
        reload();
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  function handleDeleteTemporaryClosure(id: number) {
    startTransition(async () => {
      const { success, message } = await deleteTemporaryClosureAction(id);
      if (success) {
        toast.success(SHOP_MESSAGE.TEMPORARY_CLOSURE_DELETE_SUCCESS);
        reload();
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{SHOP_OPERATION_COPY.CLOSED_DAYS_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_OPERATION_COPY.CLOSED_DAYS_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-5 overflow-y-auto px-4">
          <Field orientation="horizontal">
            <FieldLabel htmlFor="holiday-closed">{SHOP_OPERATION_COPY.HOLIDAY_CLOSED_TOGGLE}</FieldLabel>
            <Switch
              id="holiday-closed"
              checked={items.closedOnPublicHolidays}
              disabled={isPending}
              onCheckedChange={handleHolidayClosedChange}
            />
          </Field>

          <Separator />

          <FieldSet>
            <FieldLegend variant="label">정기휴무</FieldLegend>
            {items.regularClosedDays.length > 0 ? (
              <ul className="space-y-1">
                {items.regularClosedDays.map((item) => (
                  <li key={item.id} className="flex items-center justify-between rounded-md border px-3 py-2 text-sm">
                    <span>{item.description || CLOSED_DAY_TYPE_LABEL[item.closedDayType as ClosedDayTypeOption]}</span>
                    <Button
                      type="button"
                      size="sm"
                      variant="ghost"
                      className="text-destructive"
                      disabled={isPending}
                      onClick={() => handleDeleteClosedDay(item.id)}
                    >
                      삭제
                    </Button>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-muted-foreground text-sm">등록된 정기휴무가 없습니다.</p>
            )}

            <form
              id="closed-day-form"
              noValidate
              onSubmit={closedDayForm.handleSubmit(onCreateClosedDay)}
              className="mt-3 flex items-end gap-2"
            >
              <Controller
                control={closedDayForm.control}
                name="closedDayType"
                render={({ field }) => (
                  <Field className="flex-1 gap-1.5">
                    <FieldLabel htmlFor="closed-day-type">휴무 주기 (최대 {REGULAR_CLOSED_DAY_MAX_COUNT}건)</FieldLabel>
                    <Select value={field.value ?? ""} onValueChange={field.onChange} disabled={isPending}>
                      <SelectTrigger id="closed-day-type" className="w-full">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent position="popper">
                        <SelectGroup>
                          {CLOSED_DAY_TYPE_OPTIONS.map((option) => (
                            <SelectItem key={option} value={option}>
                              {CLOSED_DAY_TYPE_LABEL[option]}
                            </SelectItem>
                          ))}
                        </SelectGroup>
                      </SelectContent>
                    </Select>
                  </Field>
                )}
              />
              <Button type="submit" size="sm" disabled={isPending}>
                추가
              </Button>
            </form>

            {isRegularClosedDayMaxReached && (
              <p className="mt-2 text-destructive text-sm">{SHOP_MESSAGE.REGULAR_CLOSED_DAY_MAX_REACHED}</p>
            )}
          </FieldSet>

          <Separator />

          <FieldSet>
            <FieldLegend variant="label">임시휴무</FieldLegend>
            {items.temporaryClosures.length > 0 ? (
              <ul className="space-y-1">
                {items.temporaryClosures.map((item) => (
                  <li key={item.id} className="flex items-center justify-between rounded-md border px-3 py-2 text-sm">
                    <span className="tabular-nums">
                      {item.startDate} ~ {item.endDate}
                    </span>
                    <Button
                      type="button"
                      size="sm"
                      variant="ghost"
                      className="text-destructive"
                      disabled={isPending}
                      onClick={() => handleDeleteTemporaryClosure(item.id)}
                    >
                      삭제
                    </Button>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-muted-foreground text-sm">등록된 임시휴무가 없습니다.</p>
            )}

            <form
              id="temporary-closure-form"
              noValidate
              onSubmit={temporaryClosureForm.handleSubmit(onCreateTemporaryClosure)}
              className="mt-3"
            >
              <FieldGroup className="gap-2">
                <Field className="gap-1.5">
                  <FieldLabel>휴무 기간 (최대 {TEMPORARY_CLOSURE_MAX_DAYS}일)</FieldLabel>
                  <DateRangePicker value={dateRange} onChange={handleDateRangeChange} />
                  {temporaryClosureForm.formState.errors.startDate && (
                    <FieldError errors={[temporaryClosureForm.formState.errors.startDate]} />
                  )}
                  {temporaryClosureForm.formState.errors.endDate && (
                    <FieldError errors={[temporaryClosureForm.formState.errors.endDate]} />
                  )}
                </Field>
                <Button type="submit" size="sm" className="w-fit" disabled={isPending}>
                  임시휴무 추가
                </Button>
              </FieldGroup>
            </form>
          </FieldSet>
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
