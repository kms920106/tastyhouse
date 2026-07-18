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
import { createCategoryAction, fetchCategoriesAction } from "@/feature/product/actions";
import type { ProductCategory } from "@/feature/product/domain";
import { PRODUCT_MESSAGE } from "@/feature/product/message";
import { type CategoryFormValues, categorySchema } from "@/feature/product/schema";

interface ProductCategorySheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  initialShopId?: number;
}

export function ProductCategorySheet({ open, onOpenChange, initialShopId }: ProductCategorySheetProps) {
  const [shopIdInput, setShopIdInput] = React.useState(initialShopId === undefined ? "" : String(initialShopId));
  const [categories, setCategories] = React.useState<ProductCategory[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [loadedShopId, setLoadedShopId] = React.useState<number | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const form = useForm<CategoryFormValues>({
    resolver: zodResolver(categorySchema),
    defaultValues: {
      shopId: initialShopId ?? (undefined as unknown as number),
      name: "",
      sort: 0,
      visible: true,
    },
  });

  React.useEffect(() => {
    if (!open) return;
    setShopIdInput(initialShopId === undefined ? "" : String(initialShopId));
    setCategories([]);
    setError(null);
    setLoadedShopId(null);
    form.reset({
      shopId: initialShopId ?? (undefined as unknown as number),
      name: "",
      sort: 0,
      visible: true,
    });
  }, [open, initialShopId, form.reset]);

  const loadCategories = React.useCallback((shopId: number) => {
    setIsLoading(true);
    setError(null);
    void fetchCategoriesAction(shopId).then((result) => {
      setIsLoading(false);
      if (result.success && result.data) {
        setCategories(result.data);
        setLoadedShopId(shopId);
      } else {
        setCategories([]);
        setError(result.message ?? PRODUCT_MESSAGE.CATEGORIES_LOAD_FAILED);
      }
    });
  }, []);

  function handleLoad() {
    const parsed = Number(shopIdInput);
    if (!Number.isInteger(parsed) || parsed <= 0) {
      toast.error("매장 ID는 양수여야 합니다.");
      return;
    }
    form.setValue("shopId", parsed);
    loadCategories(parsed);
  }

  const onSubmit = (values: CategoryFormValues) => {
    startTransition(async () => {
      const { success, message } = await createCategoryAction(values);
      if (success) {
        toast.success(PRODUCT_MESSAGE.CATEGORY_CREATE_SUCCESS);
        form.reset({ shopId: values.shopId, name: "", sort: 0, visible: true });
        loadCategories(values.shopId);
      } else {
        toast.error(message ?? PRODUCT_MESSAGE.CATEGORY_CREATE_FAILED);
      }
    });
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>카테고리 관리</SheetTitle>
          <SheetDescription>매장 ID로 카테고리를 조회하고 새 카테고리를 등록합니다.</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-6 overflow-y-auto px-4">
          {/* 매장 ID 로 카테고리 조회 */}
          <div className="flex items-end gap-2">
            <Field className="flex-1 gap-1.5">
              <FieldLabel htmlFor="category-shop-id">매장 ID</FieldLabel>
              <Input
                id="category-shop-id"
                type="number"
                min={1}
                placeholder="매장 ID"
                value={shopIdInput}
                onChange={(e) => setShopIdInput(e.target.value)}
                disabled={isPending}
              />
            </Field>
            <Button type="button" size="sm" onClick={handleLoad} disabled={isLoading || isPending}>
              조회
            </Button>
          </div>

          {/* 카테고리 목록 */}
          <div className="space-y-2">
            <h4 className="font-medium text-sm">카테고리 목록</h4>
            {error ? (
              <p className="text-destructive text-sm">{error}</p>
            ) : isLoading ? (
              <div className="space-y-2">
                <Skeleton className="h-8 w-full" />
                <Skeleton className="h-8 w-full" />
              </div>
            ) : loadedShopId == null ? (
              <p className="text-muted-foreground text-sm">매장 ID를 입력하고 조회하세요.</p>
            ) : categories.length ? (
              <ul className="space-y-1">
                {categories.map((category) => (
                  <li
                    key={category.id}
                    className="flex items-center justify-between rounded-md border px-3 py-2 text-sm"
                  >
                    <span>{category.name}</span>
                    <div className="flex items-center gap-2">
                      <span className="text-muted-foreground text-xs tabular-nums">정렬 {category.sort}</span>
                      <Badge variant={category.visible ? "default" : "secondary"}>
                        {category.visible ? "노출" : "미노출"}
                      </Badge>
                    </div>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-muted-foreground text-sm">등록된 카테고리가 없습니다.</p>
            )}
          </div>

          <Separator />

          {/* 카테고리 생성 폼 */}
          <div className="space-y-3">
            <h4 className="font-medium text-sm">카테고리 추가</h4>
            <form id="category-form" noValidate onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
              <Controller
                control={form.control}
                name="name"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="category-name">카테고리 이름</FieldLabel>
                    <Input
                      {...field}
                      id="category-name"
                      placeholder="예: 메인, 사이드"
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
                  <Field className="w-24 gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="category-sort">정렬</FieldLabel>
                    <Input
                      id="category-sort"
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
                name="visible"
                render={({ field }) => (
                  <Field orientation="horizontal">
                    <FieldLabel htmlFor="category-visible">노출 여부</FieldLabel>
                    <Switch
                      id="category-visible"
                      checked={field.value}
                      onCheckedChange={field.onChange}
                      disabled={isPending}
                    />
                  </Field>
                )}
              />
              <p className="text-muted-foreground text-xs">위에서 조회한 매장 ID로 등록됩니다.</p>
              <Button type="submit" size="sm" disabled={isPending}>
                {isPending ? "추가 중..." : "카테고리 추가"}
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
