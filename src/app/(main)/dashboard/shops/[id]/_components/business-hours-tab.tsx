"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { Switch } from "@/components/ui/switch";
import {
  createBreakTimeAction,
  createBusinessHourAction,
  createClosedDayAction,
  deleteBreakTimeAction,
  deleteBusinessHourAction,
  deleteClosedDayAction,
  fetchBreakTimesAction,
  fetchBusinessHoursAction,
  fetchClosedDaysAction,
  updateBreakTimeAction,
  updateBusinessHourAction,
} from "@/feature/shop/actions";
import {
  CLOSED_DAY_TYPE_LABEL,
  CLOSED_DAY_TYPE_OPTIONS,
  DAY_TYPE_LABEL,
  DAY_TYPE_OPTIONS,
} from "@/feature/shop/constants";
import type { BreakTime, BusinessHour, ClosedDay } from "@/feature/shop/domain";
import { SHOP_MESSAGE } from "@/feature/shop/message";
import {
  type BreakTimeFormValues,
  type BusinessHourFormValues,
  breakTimeSchema,
  businessHourSchema,
  type ClosedDayFormValues,
  closedDaySchema,
} from "@/feature/shop/schema";

interface TabProps {
  shopId: number;
}

const EMPTY_BUSINESS_HOUR: BusinessHourFormValues = {
  dayType: DAY_TYPE_OPTIONS[0],
  openTime: "09:00:00",
  closeTime: "22:00:00",
  isClosed: false,
};

const EMPTY_BREAK_TIME: BreakTimeFormValues = {
  dayType: DAY_TYPE_OPTIONS[0],
  startTime: "15:00:00",
  endTime: "17:00:00",
};

const EMPTY_CLOSED_DAY: ClosedDayFormValues = {
  closedDayType: CLOSED_DAY_TYPE_OPTIONS[0],
};

function BusinessHoursSection({ shopId }: TabProps) {
  const [items, setItems] = React.useState<BusinessHour[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [isPending, startTransition] = React.useTransition();
  const [editingId, setEditingId] = React.useState<number | null>(null);

  const form = useForm<BusinessHourFormValues>({
    resolver: zodResolver(businessHourSchema),
    defaultValues: EMPTY_BUSINESS_HOUR,
  });

  const load = React.useCallback(() => {
    setIsLoading(true);
    setError(null);
    void fetchBusinessHoursAction(shopId).then((result) => {
      setIsLoading(false);
      if (result.success && result.data) {
        setItems(result.data);
      } else {
        setError(result.message ?? SHOP_MESSAGE.BUSINESS_HOURS_LOAD_FAILED);
      }
    });
  }, [shopId]);

  React.useEffect(() => {
    load();
  }, [load]);

  function startEdit(item: BusinessHour) {
    setEditingId(item.id);
    form.reset({
      dayType: item.dayType,
      openTime: item.openTime,
      closeTime: item.closeTime,
      isClosed: item.isClosed,
    });
  }

  function cancelEdit() {
    setEditingId(null);
    form.reset(EMPTY_BUSINESS_HOUR);
  }

  const onSubmit = (values: BusinessHourFormValues) => {
    startTransition(async () => {
      const { success, message } = editingId
        ? await updateBusinessHourAction(editingId, values)
        : await createBusinessHourAction(shopId, values);
      if (success) {
        toast.success(
          editingId ? SHOP_MESSAGE.BUSINESS_HOUR_UPDATE_SUCCESS : SHOP_MESSAGE.BUSINESS_HOUR_CREATE_SUCCESS,
        );
        cancelEdit();
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  function handleDelete(id: number) {
    startTransition(async () => {
      const { success, message } = await deleteBusinessHourAction(id);
      if (success) {
        toast.success(SHOP_MESSAGE.BUSINESS_HOUR_DELETE_SUCCESS);
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <div className="space-y-3">
      <h4 className="font-medium text-sm">운영시간</h4>
      {error ? (
        <p className="text-destructive text-sm">{error}</p>
      ) : isLoading ? (
        <Skeleton className="h-20 w-full" />
      ) : items.length ? (
        <ul className="space-y-1">
          {items.map((item) => (
            <li key={item.id} className="flex items-center justify-between rounded-md border px-3 py-2 text-sm">
              <span>
                {item.description} · {item.isClosed ? "휴무" : `${item.openTime} ~ ${item.closeTime}`}
              </span>
              <div className="flex gap-1">
                <Button type="button" size="sm" variant="outline" onClick={() => startEdit(item)}>
                  수정
                </Button>
                <Button
                  type="button"
                  size="sm"
                  variant="ghost"
                  className="text-destructive"
                  disabled={isPending}
                  onClick={() => handleDelete(item.id)}
                >
                  삭제
                </Button>
              </div>
            </li>
          ))}
        </ul>
      ) : (
        <p className="text-muted-foreground text-sm">등록된 운영시간이 없습니다.</p>
      )}

      <form noValidate onSubmit={form.handleSubmit(onSubmit)} className="flex flex-wrap items-end gap-2">
        <Controller
          control={form.control}
          name="dayType"
          render={({ field }) => (
            <Field className="w-32 gap-1.5">
              <FieldLabel htmlFor="business-hour-day-type">요일 구분</FieldLabel>
              <Select value={field.value} onValueChange={field.onChange} disabled={isPending}>
                <SelectTrigger id="business-hour-day-type" className="w-full">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    {DAY_TYPE_OPTIONS.map((option) => (
                      <SelectItem key={option} value={option}>
                        {DAY_TYPE_LABEL[option]}
                      </SelectItem>
                    ))}
                  </SelectGroup>
                </SelectContent>
              </Select>
            </Field>
          )}
        />
        <Controller
          control={form.control}
          name="openTime"
          render={({ field, fieldState }) => (
            <Field className="w-32 gap-1.5" data-invalid={fieldState.invalid}>
              <FieldLabel htmlFor="business-hour-open">시작 (HH:mm:ss)</FieldLabel>
              <Input {...field} id="business-hour-open" placeholder="09:00:00" disabled={isPending} />
              {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
            </Field>
          )}
        />
        <Controller
          control={form.control}
          name="closeTime"
          render={({ field, fieldState }) => (
            <Field className="w-32 gap-1.5" data-invalid={fieldState.invalid}>
              <FieldLabel htmlFor="business-hour-close">종료 (HH:mm:ss)</FieldLabel>
              <Input {...field} id="business-hour-close" placeholder="22:00:00" disabled={isPending} />
              {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
            </Field>
          )}
        />
        <Controller
          control={form.control}
          name="isClosed"
          render={({ field }) => (
            <Field orientation="horizontal">
              <FieldLabel htmlFor="business-hour-is-closed">휴무</FieldLabel>
              <Switch
                id="business-hour-is-closed"
                checked={field.value}
                onCheckedChange={field.onChange}
                disabled={isPending}
              />
            </Field>
          )}
        />
        <Button type="submit" size="sm" disabled={isPending}>
          {isPending ? "저장 중..." : editingId ? "수정" : "등록"}
        </Button>
        {editingId ? (
          <Button type="button" size="sm" variant="outline" onClick={cancelEdit} disabled={isPending}>
            취소
          </Button>
        ) : null}
      </form>
    </div>
  );
}

function BreakTimesSection({ shopId }: TabProps) {
  const [items, setItems] = React.useState<BreakTime[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [isPending, startTransition] = React.useTransition();
  const [editingId, setEditingId] = React.useState<number | null>(null);

  const form = useForm<BreakTimeFormValues>({
    resolver: zodResolver(breakTimeSchema),
    defaultValues: EMPTY_BREAK_TIME,
  });

  const load = React.useCallback(() => {
    setIsLoading(true);
    setError(null);
    void fetchBreakTimesAction(shopId).then((result) => {
      setIsLoading(false);
      if (result.success && result.data) {
        setItems(result.data);
      } else {
        setError(result.message ?? SHOP_MESSAGE.BREAK_TIMES_LOAD_FAILED);
      }
    });
  }, [shopId]);

  React.useEffect(() => {
    load();
  }, [load]);

  function startEdit(item: BreakTime) {
    setEditingId(item.id);
    form.reset({ dayType: item.dayType, startTime: item.startTime, endTime: item.endTime });
  }

  function cancelEdit() {
    setEditingId(null);
    form.reset(EMPTY_BREAK_TIME);
  }

  const onSubmit = (values: BreakTimeFormValues) => {
    startTransition(async () => {
      const { success, message } = editingId
        ? await updateBreakTimeAction(editingId, values)
        : await createBreakTimeAction(shopId, values);
      if (success) {
        toast.success(editingId ? SHOP_MESSAGE.BREAK_TIME_UPDATE_SUCCESS : SHOP_MESSAGE.BREAK_TIME_CREATE_SUCCESS);
        cancelEdit();
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  function handleDelete(id: number) {
    startTransition(async () => {
      const { success, message } = await deleteBreakTimeAction(id);
      if (success) {
        toast.success(SHOP_MESSAGE.BREAK_TIME_DELETE_SUCCESS);
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <div className="space-y-3">
      <h4 className="font-medium text-sm">브레이크타임</h4>
      {error ? (
        <p className="text-destructive text-sm">{error}</p>
      ) : isLoading ? (
        <Skeleton className="h-20 w-full" />
      ) : items.length ? (
        <ul className="space-y-1">
          {items.map((item) => (
            <li key={item.id} className="flex items-center justify-between rounded-md border px-3 py-2 text-sm">
              <span>
                {item.description} · {item.startTime} ~ {item.endTime}
              </span>
              <div className="flex gap-1">
                <Button type="button" size="sm" variant="outline" onClick={() => startEdit(item)}>
                  수정
                </Button>
                <Button
                  type="button"
                  size="sm"
                  variant="ghost"
                  className="text-destructive"
                  disabled={isPending}
                  onClick={() => handleDelete(item.id)}
                >
                  삭제
                </Button>
              </div>
            </li>
          ))}
        </ul>
      ) : (
        <p className="text-muted-foreground text-sm">등록된 브레이크타임이 없습니다.</p>
      )}

      <form noValidate onSubmit={form.handleSubmit(onSubmit)} className="flex flex-wrap items-end gap-2">
        <Controller
          control={form.control}
          name="dayType"
          render={({ field }) => (
            <Field className="w-32 gap-1.5">
              <FieldLabel htmlFor="break-time-day-type">요일 구분</FieldLabel>
              <Select value={field.value} onValueChange={field.onChange} disabled={isPending}>
                <SelectTrigger id="break-time-day-type" className="w-full">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    {DAY_TYPE_OPTIONS.map((option) => (
                      <SelectItem key={option} value={option}>
                        {DAY_TYPE_LABEL[option]}
                      </SelectItem>
                    ))}
                  </SelectGroup>
                </SelectContent>
              </Select>
            </Field>
          )}
        />
        <Controller
          control={form.control}
          name="startTime"
          render={({ field, fieldState }) => (
            <Field className="w-32 gap-1.5" data-invalid={fieldState.invalid}>
              <FieldLabel htmlFor="break-time-start">시작 (HH:mm:ss)</FieldLabel>
              <Input {...field} id="break-time-start" placeholder="15:00:00" disabled={isPending} />
              {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
            </Field>
          )}
        />
        <Controller
          control={form.control}
          name="endTime"
          render={({ field, fieldState }) => (
            <Field className="w-32 gap-1.5" data-invalid={fieldState.invalid}>
              <FieldLabel htmlFor="break-time-end">종료 (HH:mm:ss)</FieldLabel>
              <Input {...field} id="break-time-end" placeholder="17:00:00" disabled={isPending} />
              {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
            </Field>
          )}
        />
        <Button type="submit" size="sm" disabled={isPending}>
          {isPending ? "저장 중..." : editingId ? "수정" : "등록"}
        </Button>
        {editingId ? (
          <Button type="button" size="sm" variant="outline" onClick={cancelEdit} disabled={isPending}>
            취소
          </Button>
        ) : null}
      </form>
    </div>
  );
}

function ClosedDaysSection({ shopId }: TabProps) {
  const [items, setItems] = React.useState<ClosedDay[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const form = useForm<ClosedDayFormValues>({
    resolver: zodResolver(closedDaySchema),
    defaultValues: EMPTY_CLOSED_DAY,
  });

  const load = React.useCallback(() => {
    setIsLoading(true);
    setError(null);
    void fetchClosedDaysAction(shopId).then((result) => {
      setIsLoading(false);
      if (result.success && result.data) {
        setItems(result.data);
      } else {
        setError(result.message ?? SHOP_MESSAGE.CLOSED_DAYS_LOAD_FAILED);
      }
    });
  }, [shopId]);

  React.useEffect(() => {
    load();
  }, [load]);

  const onSubmit = (values: ClosedDayFormValues) => {
    startTransition(async () => {
      const { success, message } = await createClosedDayAction(shopId, values);
      if (success) {
        toast.success(SHOP_MESSAGE.CLOSED_DAY_CREATE_SUCCESS);
        form.reset(EMPTY_CLOSED_DAY);
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  function handleDelete(id: number) {
    startTransition(async () => {
      const { success, message } = await deleteClosedDayAction(id);
      if (success) {
        toast.success(SHOP_MESSAGE.CLOSED_DAY_DELETE_SUCCESS);
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <div className="space-y-3">
      <h4 className="font-medium text-sm">정기휴무일</h4>
      {error ? (
        <p className="text-destructive text-sm">{error}</p>
      ) : isLoading ? (
        <Skeleton className="h-20 w-full" />
      ) : items.length ? (
        <ul className="space-y-1">
          {items.map((item) => (
            <li key={item.id} className="flex items-center justify-between rounded-md border px-3 py-2 text-sm">
              <span>{item.description}</span>
              <Button
                type="button"
                size="sm"
                variant="ghost"
                className="text-destructive"
                disabled={isPending}
                onClick={() => handleDelete(item.id)}
              >
                삭제
              </Button>
            </li>
          ))}
        </ul>
      ) : (
        <p className="text-muted-foreground text-sm">등록된 정기휴무일이 없습니다.</p>
      )}

      <form noValidate onSubmit={form.handleSubmit(onSubmit)} className="flex flex-wrap items-end gap-2">
        <Controller
          control={form.control}
          name="closedDayType"
          render={({ field }) => (
            <Field className="w-48 gap-1.5">
              <FieldLabel htmlFor="closed-day-type">휴무 유형</FieldLabel>
              <Select value={field.value} onValueChange={field.onChange} disabled={isPending}>
                <SelectTrigger id="closed-day-type" className="w-full">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
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
          {isPending ? "등록 중..." : "등록"}
        </Button>
      </form>
    </div>
  );
}

export function BusinessHoursTab({ shopId }: TabProps) {
  return (
    <div className="space-y-6">
      <BusinessHoursSection shopId={shopId} />
      <Separator />
      <BreakTimesSection shopId={shopId} />
      <Separator />
      <ClosedDaysSection shopId={shopId} />
    </div>
  );
}
