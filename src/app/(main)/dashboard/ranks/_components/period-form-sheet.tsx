"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { Skeleton } from "@/components/ui/skeleton";
import { Switch } from "@/components/ui/switch";
import { createPeriodAction, fetchPeriodAction, updatePeriodAction } from "@/feature/rank/actions";
import type { RankPeriod } from "@/feature/rank/domain";
import { RANK_MESSAGE } from "@/feature/rank/message";
import { type PeriodFormValues, periodSchema, toInputDateTime } from "@/feature/rank/schema";

interface PeriodFormSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  period?: Pick<RankPeriod, "id"> | null;
}

const EMPTY_VALUES: PeriodFormValues = {
  startAt: "",
  endAt: "",
  visible: true,
};

export function PeriodFormSheet({ open, onOpenChange, period }: PeriodFormSheetProps) {
  const isEdit = Boolean(period);
  const [isPending, startTransition] = React.useTransition();
  const [isLoadingDetail, setIsLoadingDetail] = React.useState(false);

  const form = useForm<PeriodFormValues>({
    resolver: zodResolver(periodSchema),
    defaultValues: EMPTY_VALUES,
  });

  React.useEffect(() => {
    if (!open) return;

    if (!period) {
      form.reset(EMPTY_VALUES);
      return;
    }

    let active = true;
    setIsLoadingDetail(true);

    void fetchPeriodAction(period.id).then((result) => {
      if (!active) return;
      setIsLoadingDetail(false);

      if (!result.success || !result.data) {
        toast.error(result.message ?? RANK_MESSAGE.PERIOD_DETAIL_LOAD_FAILED);
        onOpenChange(false);
        return;
      }

      const detail = result.data;
      form.reset({
        startAt: toInputDateTime(detail.startAt),
        endAt: toInputDateTime(detail.endAt),
        visible: detail.visible,
      });
    });

    return () => {
      active = false;
    };
  }, [open, period, form.reset, onOpenChange]);

  const onSubmit = (values: PeriodFormValues) => {
    startTransition(async () => {
      const { success, message } = period
        ? await updatePeriodAction(period.id, values)
        : await createPeriodAction(values);

      if (success) {
        toast.success(isEdit ? RANK_MESSAGE.PERIOD_UPDATE_SUCCESS : RANK_MESSAGE.PERIOD_CREATE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? RANK_MESSAGE.PERIOD_CREATE_UPDATE_FAILED);
      }
    });
  };

  const busy = isPending || isLoadingDetail;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{isEdit ? "랭킹 기간 수정" : "랭킹 기간 등록"}</SheetTitle>
          <SheetDescription>
            {isEdit ? "랭킹 기간 정보를 수정합니다." : "새로운 랭킹 기간을 등록합니다."}
          </SheetDescription>
        </SheetHeader>

        {isLoadingDetail ? (
          <div className="flex-1 space-y-3 px-4">
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
          </div>
        ) : (
          <form
            id="period-form"
            noValidate
            onSubmit={form.handleSubmit(onSubmit)}
            className="flex-1 overflow-y-auto px-4"
          >
            <FieldGroup className="gap-4">
              <Controller
                control={form.control}
                name="startAt"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="period-start-at">시작 일시</FieldLabel>
                    <Input
                      {...field}
                      id="period-start-at"
                      type="datetime-local"
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="endAt"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="period-end-at">종료 일시</FieldLabel>
                    <Input
                      {...field}
                      id="period-end-at"
                      type="datetime-local"
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="visible"
                render={({ field }) => (
                  <Field orientation="horizontal">
                    <FieldLabel htmlFor="period-visible">노출 여부</FieldLabel>
                    <Switch
                      id="period-visible"
                      checked={field.value}
                      onCheckedChange={field.onChange}
                      disabled={busy}
                    />
                  </Field>
                )}
              />
            </FieldGroup>
          </form>
        )}

        <SheetFooter>
          <Button type="submit" form="period-form" disabled={busy}>
            {isPending ? "저장 중..." : isEdit ? "수정" : "등록"}
          </Button>
          <SheetClose asChild>
            <Button variant="outline" disabled={busy}>
              취소
            </Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
