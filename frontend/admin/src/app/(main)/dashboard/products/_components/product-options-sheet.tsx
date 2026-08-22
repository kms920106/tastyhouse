"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
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
import { Skeleton } from "@/components/ui/skeleton";
import { Switch } from "@/components/ui/switch";
import { createOptionAction, createOptionGroupAction, fetchProductOptionsAction } from "@/feature/product/actions";
import type { OptionGroup, ProductListItem } from "@/feature/product/domain";
import { formatPrice } from "@/feature/product/format";
import { OPTION_GROUP_TYPE_LABEL, PRODUCT_MESSAGE } from "@/feature/product/message";
import {
  type OptionFormValues,
  type OptionGroupFormValues,
  optionGroupSchema,
  optionSchema,
} from "@/feature/product/schema";

interface ProductOptionsSheetProps {
  /** 옵션 관리 대상 상품. null 이면 닫힌 상태. */
  product: Pick<ProductListItem, "id" | "name"> | null;
  onOpenChange: (open: boolean) => void;
}

const EMPTY_GROUP: OptionGroupFormValues = {
  name: "",
  description: undefined,
  required: false,
  multipleSelect: false,
  minSelect: 0,
  maxSelect: 1,
  sort: 0,
  visible: true,
};

const EMPTY_OPTION: OptionFormValues = {
  name: "",
  additionalPrice: 0,
  sort: 0,
  soldOut: false,
  visible: true,
};

function OptionCreateForm({ groupId, onCreated }: { groupId: number; onCreated: () => void }) {
  const [isPending, startTransition] = React.useTransition();
  const form = useForm<OptionFormValues>({
    resolver: zodResolver(optionSchema),
    defaultValues: EMPTY_OPTION,
  });

  const onSubmit = (values: OptionFormValues) => {
    startTransition(async () => {
      const { success, message } = await createOptionAction(groupId, values);
      if (success) {
        toast.success(PRODUCT_MESSAGE.OPTION_CREATE_SUCCESS);
        form.reset(EMPTY_OPTION);
        onCreated();
      } else {
        toast.error(message ?? PRODUCT_MESSAGE.OPTION_CREATE_FAILED);
      }
    });
  };

  return (
    <form noValidate onSubmit={form.handleSubmit(onSubmit)} className="flex flex-wrap items-end gap-2">
      <Controller
        control={form.control}
        name="name"
        render={({ field, fieldState }) => (
          <Field className="min-w-32 flex-1 gap-1.5" data-invalid={fieldState.invalid}>
            <FieldLabel htmlFor={`option-name-${groupId}`}>옵션 이름</FieldLabel>
            <Input
              {...field}
              id={`option-name-${groupId}`}
              placeholder="옵션 이름"
              aria-invalid={fieldState.invalid}
              disabled={isPending}
            />
            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
          </Field>
        )}
      />
      <Controller
        control={form.control}
        name="additionalPrice"
        render={({ field, fieldState }) => (
          <Field className="w-24 gap-1.5" data-invalid={fieldState.invalid}>
            <FieldLabel htmlFor={`option-price-${groupId}`}>추가 금액</FieldLabel>
            <Input
              id={`option-price-${groupId}`}
              type="number"
              min={0}
              value={field.value}
              onChange={(e) => field.onChange(Number(e.target.value))}
              aria-invalid={fieldState.invalid}
              disabled={isPending}
            />
            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
          </Field>
        )}
      />
      <Controller
        control={form.control}
        name="sort"
        render={({ field, fieldState }) => (
          <Field className="w-20 gap-1.5" data-invalid={fieldState.invalid}>
            <FieldLabel htmlFor={`option-sort-${groupId}`}>정렬</FieldLabel>
            <Input
              id={`option-sort-${groupId}`}
              type="number"
              value={field.value}
              onChange={(e) => field.onChange(Number(e.target.value))}
              aria-invalid={fieldState.invalid}
              disabled={isPending}
            />
            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
          </Field>
        )}
      />
      <Controller
        control={form.control}
        name="soldOut"
        render={({ field }) => (
          <Field orientation="horizontal">
            <FieldLabel htmlFor={`option-sold-out-${groupId}`}>품절</FieldLabel>
            <Switch
              id={`option-sold-out-${groupId}`}
              checked={field.value}
              onCheckedChange={field.onChange}
              disabled={isPending}
            />
          </Field>
        )}
      />
      <Controller
        control={form.control}
        name="visible"
        render={({ field }) => (
          <Field orientation="horizontal">
            <FieldLabel htmlFor={`option-visible-${groupId}`}>노출</FieldLabel>
            <Switch
              id={`option-visible-${groupId}`}
              checked={field.value}
              onCheckedChange={field.onChange}
              disabled={isPending}
            />
          </Field>
        )}
      />
      <Button type="submit" size="sm" disabled={isPending}>
        {isPending ? "추가 중..." : "옵션 추가"}
      </Button>
    </form>
  );
}

export function ProductOptionsSheet({ product, onOpenChange }: ProductOptionsSheetProps) {
  const [groups, setGroups] = React.useState<OptionGroup[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const productId = product?.id ?? null;

  const groupForm = useForm<OptionGroupFormValues>({
    resolver: zodResolver(optionGroupSchema),
    defaultValues: EMPTY_GROUP,
  });

  const loadOptions = React.useCallback(() => {
    if (productId == null) return;

    let active = true;
    setIsLoading(true);
    setError(null);

    void fetchProductOptionsAction(productId).then((result) => {
      if (!active) return;
      if (result.success && result.data) {
        setGroups(result.data.optionGroups);
      } else {
        setError(result.message ?? PRODUCT_MESSAGE.OPTIONS_LOAD_FAILED);
      }
      setIsLoading(false);
    });

    return () => {
      active = false;
    };
  }, [productId]);

  React.useEffect(() => {
    if (productId == null) return;
    groupForm.reset(EMPTY_GROUP);
    setGroups([]);
    setError(null);
    return loadOptions();
  }, [productId, groupForm.reset, loadOptions]);

  const onCreateGroup = (values: OptionGroupFormValues) => {
    if (productId == null) return;
    startTransition(async () => {
      const { success, message } = await createOptionGroupAction(productId, values);
      if (success) {
        toast.success(PRODUCT_MESSAGE.OPTION_GROUP_CREATE_SUCCESS);
        groupForm.reset(EMPTY_GROUP);
        loadOptions();
      } else {
        toast.error(message ?? PRODUCT_MESSAGE.OPTION_GROUP_CREATE_FAILED);
      }
    });
  };

  return (
    <Sheet open={product != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-xl">
        <SheetHeader>
          <SheetTitle>옵션 관리</SheetTitle>
          <SheetDescription>
            {product ? `"${product.name}" 상품의 옵션 그룹과 옵션을 관리합니다.` : ""}
          </SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-6 overflow-y-auto px-4">
          {/* 옵션 그룹/옵션 목록 */}
          <div className="space-y-4">
            {error ? (
              <p className="text-destructive text-sm">{error}</p>
            ) : isLoading ? (
              <div className="space-y-3">
                <Skeleton className="h-24 w-full" />
                <Skeleton className="h-24 w-full" />
              </div>
            ) : groups.length ? (
              groups.map((group) => (
                <div key={group.id} className="space-y-3 rounded-md border p-3">
                  <div className="flex items-center justify-between gap-2">
                    <div className="flex items-center gap-2">
                      <span className="font-medium text-sm">{group.name}</span>
                      {group.common ? <Badge variant="outline">공통</Badge> : null}
                      {group.groupType === "CUP_DEPOSIT" ? (
                        <Badge variant="outline">{OPTION_GROUP_TYPE_LABEL.CUP_DEPOSIT}</Badge>
                      ) : null}
                      {group.required ? <Badge variant="secondary">필수</Badge> : null}
                      {group.multipleSelect ? <Badge variant="secondary">복수 선택</Badge> : null}
                    </div>
                    <span className="text-muted-foreground text-xs">
                      {group.minSelect}~{group.maxSelect}개 선택
                    </span>
                  </div>

                  {group.description ? <p className="text-muted-foreground text-xs">{group.description}</p> : null}

                  {group.options.length ? (
                    <ul className="space-y-1">
                      {group.options.map((option) => (
                        <li key={option.id} className="flex items-center justify-between text-muted-foreground text-sm">
                          <span>
                            {option.name}
                            {option.soldOut ? <span className="ml-1 text-destructive">(품절)</span> : null}
                            {option.cupCount != null ? (
                              <span className="ml-1">
                                (컵 {option.cupCount}개 · 보증금 {formatPrice(option.depositAmount)})
                              </span>
                            ) : null}
                            {option.personalCupDiscountAmount != null ? (
                              <span className="ml-1">(개인컵 할인 {formatPrice(option.personalCupDiscountAmount)})</span>
                            ) : null}
                          </span>
                          <span className="tabular-nums">{formatPrice(option.additionalPrice)}</span>
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <p className="text-muted-foreground text-xs">등록된 옵션이 없습니다.</p>
                  )}

                  {/* 공통 그룹(common=true)은 관리자에서 옵션을 추가할 수 없다. */}
                  {group.common ? (
                    <p className="text-muted-foreground text-xs">공통 옵션 그룹에는 옵션을 추가할 수 없습니다.</p>
                  ) : (
                    <OptionCreateForm groupId={group.id} onCreated={loadOptions} />
                  )}
                </div>
              ))
            ) : (
              <p className="text-muted-foreground text-sm">등록된 옵션 그룹이 없습니다.</p>
            )}
          </div>

          <Separator />

          {/* 옵션 그룹 생성 폼 */}
          <div className="space-y-3">
            <h4 className="font-medium text-sm">옵션 그룹 추가</h4>
            <form
              id="option-group-form"
              noValidate
              onSubmit={groupForm.handleSubmit(onCreateGroup)}
              className="space-y-3"
            >
              <Controller
                control={groupForm.control}
                name="name"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="option-group-name">그룹 이름</FieldLabel>
                    <Input
                      {...field}
                      id="option-group-name"
                      placeholder="예: 사이즈, 토핑"
                      aria-invalid={fieldState.invalid}
                      disabled={isPending}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />
              <div className="flex flex-wrap gap-2">
                <Controller
                  control={groupForm.control}
                  name="minSelect"
                  render={({ field, fieldState }) => (
                    <Field className="w-24 gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor="option-group-min">최소 선택</FieldLabel>
                      <Input
                        id="option-group-min"
                        type="number"
                        min={0}
                        value={field.value}
                        onChange={(e) => field.onChange(Number(e.target.value))}
                        aria-invalid={fieldState.invalid}
                        disabled={isPending}
                      />
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                  )}
                />
                <Controller
                  control={groupForm.control}
                  name="maxSelect"
                  render={({ field, fieldState }) => (
                    <Field className="w-24 gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor="option-group-max">최대 선택</FieldLabel>
                      <Input
                        id="option-group-max"
                        type="number"
                        min={1}
                        value={field.value}
                        onChange={(e) => field.onChange(Number(e.target.value))}
                        aria-invalid={fieldState.invalid}
                        disabled={isPending}
                      />
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                  )}
                />
                <Controller
                  control={groupForm.control}
                  name="sort"
                  render={({ field, fieldState }) => (
                    <Field className="w-20 gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor="option-group-sort">정렬</FieldLabel>
                      <Input
                        id="option-group-sort"
                        type="number"
                        value={field.value}
                        onChange={(e) => field.onChange(Number(e.target.value))}
                        aria-invalid={fieldState.invalid}
                        disabled={isPending}
                      />
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                  )}
                />
              </div>
              <Controller
                control={groupForm.control}
                name="required"
                render={({ field }) => (
                  <Field orientation="horizontal">
                    <FieldLabel htmlFor="option-group-required">필수 선택</FieldLabel>
                    <Switch
                      id="option-group-required"
                      checked={field.value}
                      onCheckedChange={field.onChange}
                      disabled={isPending}
                    />
                  </Field>
                )}
              />
              <Controller
                control={groupForm.control}
                name="multipleSelect"
                render={({ field }) => (
                  <Field orientation="horizontal">
                    <FieldLabel htmlFor="option-group-multiple-select">복수 선택 가능</FieldLabel>
                    <Switch
                      id="option-group-multiple-select"
                      checked={field.value}
                      onCheckedChange={field.onChange}
                      disabled={isPending}
                    />
                  </Field>
                )}
              />
              <Controller
                control={groupForm.control}
                name="visible"
                render={({ field }) => (
                  <Field orientation="horizontal">
                    <FieldLabel htmlFor="option-group-visible">노출 여부</FieldLabel>
                    <Switch
                      id="option-group-visible"
                      checked={field.value}
                      onCheckedChange={field.onChange}
                      disabled={isPending}
                    />
                  </Field>
                )}
              />
              <Button type="submit" size="sm" disabled={isPending}>
                {isPending ? "추가 중..." : "옵션 그룹 추가"}
              </Button>
            </form>
          </div>
        </div>

        <SheetFooter>
          <SheetClose asChild>
            <Button variant="outline">닫기</Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
