"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import { createHygieneBadgeAction, deleteHygieneBadgeAction, fetchHygieneBadgesAction } from "@/feature/shop/actions";
import { HYGIENE_BADGE_TYPE_LABEL, HYGIENE_BADGE_TYPE_OPTIONS } from "@/feature/shop/constants";
import type { ShopHygieneBadge } from "@/feature/shop/domain";
import { SHOP_MESSAGE } from "@/feature/shop/message";
import { type HygieneBadgeFormValues, hygieneBadgeSchema } from "@/feature/shop/schema";

interface HygieneBadgesTabProps {
  shopId: number;
}

const EMPTY_HYGIENE_BADGE: HygieneBadgeFormValues = {
  badgeType: HYGIENE_BADGE_TYPE_OPTIONS[0],
  certifiedDate: "",
  lastInspectionMonth: "",
};

const CESCO_BADGE_TYPES = new Set(["CESCO_BLUE", "CESCO_WHITE"]);

export function HygieneBadgesTab({ shopId }: HygieneBadgesTabProps) {
  const [items, setItems] = React.useState<ShopHygieneBadge[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const form = useForm<HygieneBadgeFormValues>({
    resolver: zodResolver(hygieneBadgeSchema),
    defaultValues: EMPTY_HYGIENE_BADGE,
  });

  const load = React.useCallback(() => {
    setIsLoading(true);
    setError(null);
    void fetchHygieneBadgesAction(shopId).then((result) => {
      setIsLoading(false);
      if (result.success && result.data) {
        setItems(result.data);
      } else {
        setError(result.message ?? SHOP_MESSAGE.HYGIENE_BADGES_LOAD_FAILED);
      }
    });
  }, [shopId]);

  React.useEffect(() => {
    load();
  }, [load]);

  const badgeTypeWatch = form.watch("badgeType");
  const isCescoType = CESCO_BADGE_TYPES.has(badgeTypeWatch);

  const onSubmit = (values: HygieneBadgeFormValues) => {
    startTransition(async () => {
      const { success, message } = await createHygieneBadgeAction(shopId, values);
      if (success) {
        toast.success(SHOP_MESSAGE.HYGIENE_BADGE_CREATE_SUCCESS);
        form.reset(EMPTY_HYGIENE_BADGE);
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  function handleDelete(id: number) {
    startTransition(async () => {
      const { success, message } = await deleteHygieneBadgeAction(id);
      if (success) {
        toast.success(SHOP_MESSAGE.HYGIENE_BADGE_DELETE_SUCCESS);
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <div className="space-y-3">
      <h4 className="font-medium text-sm">위생 인증</h4>
      {error ? (
        <p className="text-destructive text-sm">{error}</p>
      ) : isLoading ? (
        <Skeleton className="h-20 w-full" />
      ) : items.length ? (
        <ul className="space-y-1">
          {items.map((item) => (
            <li key={item.id} className="flex items-center justify-between rounded-md border px-3 py-2 text-sm">
              <span>
                {HYGIENE_BADGE_TYPE_LABEL[item.badgeType]} · 인증일 {item.certifiedDate}
                {item.lastInspectionMonth ? ` · 최근 점검월 ${item.lastInspectionMonth}` : ""}
              </span>
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
        <p className="text-muted-foreground text-sm">등록된 위생 인증이 없습니다.</p>
      )}

      <form noValidate onSubmit={form.handleSubmit(onSubmit)} className="flex flex-wrap items-end gap-2">
        <Controller
          control={form.control}
          name="badgeType"
          render={({ field }) => (
            <Field className="w-48 gap-1.5">
              <FieldLabel htmlFor="hygiene-badge-type">인증 유형</FieldLabel>
              <Select value={field.value} onValueChange={field.onChange} disabled={isPending}>
                <SelectTrigger id="hygiene-badge-type" className="w-full">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    {HYGIENE_BADGE_TYPE_OPTIONS.map((option) => (
                      <SelectItem key={option} value={option}>
                        {HYGIENE_BADGE_TYPE_LABEL[option]}
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
          name="certifiedDate"
          render={({ field, fieldState }) => (
            <Field className="w-40 gap-1.5" data-invalid={fieldState.invalid}>
              <FieldLabel htmlFor="hygiene-badge-certified-date">인증일</FieldLabel>
              <Input {...field} id="hygiene-badge-certified-date" type="date" disabled={isPending} />
              {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
            </Field>
          )}
        />
        <Controller
          control={form.control}
          name="lastInspectionMonth"
          render={({ field, fieldState }) => (
            <Field className="w-40 gap-1.5" data-invalid={fieldState.invalid}>
              <FieldLabel htmlFor="hygiene-badge-last-inspection-month">최근 점검월 (세스코)</FieldLabel>
              <Input
                {...field}
                id="hygiene-badge-last-inspection-month"
                type="month"
                disabled={isPending || !isCescoType}
              />
              {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
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
