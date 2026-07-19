"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE_BYTES } from "@/api/file/file.dto";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
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
import { Skeleton } from "@/components/ui/skeleton";
import { Switch } from "@/components/ui/switch";
import {
  createFoodTypeCategoryAction,
  fetchFoodTypeCategoriesAction,
  updateFoodTypeCategoryAction,
  uploadShopImageAction,
} from "@/feature/shop/actions";
import { FOOD_TYPE_DISPLAY_NAME_MAX, FOOD_TYPE_LABEL, FOOD_TYPE_OPTIONS } from "@/feature/shop/constants";
import type { FoodTypeCategory } from "@/feature/shop/domain";
import { SHOP_MESSAGE } from "@/feature/shop/message";
import { type FoodTypeCategoryFormValues, foodTypeCategorySchema } from "@/feature/shop/schema";

interface FoodTypeCategoriesSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const EMPTY_VALUES: FoodTypeCategoryFormValues = {
  foodType: FOOD_TYPE_OPTIONS[0],
  displayName: "",
  activeImageFileId: undefined as unknown as number,
  inactiveImageFileId: undefined as unknown as number,
  sort: 0,
  visible: true,
};

export function FoodTypeCategoriesSheet({ open, onOpenChange }: FoodTypeCategoriesSheetProps) {
  const [categories, setCategories] = React.useState<FoodTypeCategory[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [isPending, startTransition] = React.useTransition();
  const [editingId, setEditingId] = React.useState<number | null>(null);
  const [uploadingField, setUploadingField] = React.useState<"activeImageFileId" | "inactiveImageFileId" | null>(null);

  const form = useForm<FoodTypeCategoryFormValues>({
    resolver: zodResolver(foodTypeCategorySchema),
    defaultValues: EMPTY_VALUES,
  });

  const loadCategories = React.useCallback(() => {
    let active = true;
    setIsLoading(true);
    setError(null);

    void fetchFoodTypeCategoriesAction().then((result) => {
      if (!active) return;
      setIsLoading(false);
      if (result.success && result.data) {
        setCategories(result.data);
      } else {
        setError(result.message ?? SHOP_MESSAGE.FOOD_TYPE_CATEGORIES_LOAD_FAILED);
      }
    });

    return () => {
      active = false;
    };
  }, []);

  React.useEffect(() => {
    if (!open) return;
    form.reset(EMPTY_VALUES);
    setEditingId(null);
    const cleanup = loadCategories();
    return cleanup;
  }, [open, form.reset, loadCategories]);

  function startEdit(category: FoodTypeCategory) {
    setEditingId(category.id);
    form.reset({
      foodType: category.foodType as FoodTypeCategoryFormValues["foodType"],
      displayName: category.displayName,
      activeImageFileId: category.activeImageFileId,
      inactiveImageFileId: category.inactiveImageFileId,
      sort: category.sort,
      visible: category.visible,
    });
  }

  function cancelEdit() {
    setEditingId(null);
    form.reset(EMPTY_VALUES);
  }

  async function handleFileChange(
    event: React.ChangeEvent<HTMLInputElement>,
    field: "activeImageFileId" | "inactiveImageFileId",
  ) {
    const file = event.target.files?.[0];
    if (!file) return;

    if (!(ALLOWED_IMAGE_TYPES as readonly string[]).includes(file.type)) {
      toast.error(SHOP_MESSAGE.IMAGE_TYPE_INVALID);
      event.target.value = "";
      return;
    }
    if (file.size > MAX_IMAGE_SIZE_BYTES) {
      toast.error(SHOP_MESSAGE.IMAGE_SIZE_EXCEEDED);
      event.target.value = "";
      return;
    }

    setUploadingField(field);
    const formData = new FormData();
    formData.append("file", file);
    const result = await uploadShopImageAction(formData);
    setUploadingField(null);
    event.target.value = "";

    if (!result.success || result.fileId === undefined) {
      toast.error(result.message ?? SHOP_MESSAGE.IMAGE_UPLOAD_FAILED);
      return;
    }

    form.setValue(field, result.fileId, { shouldValidate: true });
  }

  const onSubmit = (values: FoodTypeCategoryFormValues) => {
    startTransition(async () => {
      const { success, message } = editingId
        ? await updateFoodTypeCategoryAction(editingId, values)
        : await createFoodTypeCategoryAction(values);

      if (success) {
        toast.success(
          editingId ? SHOP_MESSAGE.FOOD_TYPE_CATEGORY_UPDATE_SUCCESS : SHOP_MESSAGE.FOOD_TYPE_CATEGORY_CREATE_SUCCESS,
        );
        cancelEdit();
        loadCategories();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  const busy = isPending || uploadingField !== null;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>음식종류 카테고리 관리</SheetTitle>
          <SheetDescription>전역 음식종류 마스터 카테고리를 등록하고 수정합니다.</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-6 overflow-y-auto px-4">
          <div className="space-y-2">
            <h4 className="font-medium text-sm">카테고리 목록</h4>
            {error ? (
              <p className="text-destructive text-sm">{error}</p>
            ) : isLoading ? (
              <div className="space-y-2">
                <Skeleton className="h-8 w-full" />
                <Skeleton className="h-8 w-full" />
              </div>
            ) : categories.length ? (
              <ul className="space-y-1">
                {categories.map((category) => (
                  <li
                    key={category.id}
                    className="flex items-center justify-between rounded-md border px-3 py-2 text-sm"
                  >
                    <div className="flex flex-col">
                      <span>{category.displayName}</span>
                      <span className="text-muted-foreground text-xs">{category.foodType}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <Badge variant={category.visible ? "default" : "secondary"}>
                        {category.visible ? "노출" : "미노출"}
                      </Badge>
                      <Button type="button" size="sm" variant="outline" onClick={() => startEdit(category)}>
                        수정
                      </Button>
                    </div>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-muted-foreground text-sm">등록된 음식종류 카테고리가 없습니다.</p>
            )}
          </div>

          <Separator />

          <div className="space-y-3">
            <h4 className="font-medium text-sm">{editingId ? "카테고리 수정" : "카테고리 추가"}</h4>
            <form id="food-type-category-form" noValidate onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
              <Controller
                control={form.control}
                name="foodType"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="food-type-category-food-type">음식 종류</FieldLabel>
                    <Select value={field.value} onValueChange={field.onChange} disabled={busy}>
                      <SelectTrigger
                        id="food-type-category-food-type"
                        className="w-full"
                        aria-invalid={fieldState.invalid}
                      >
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectGroup>
                          {FOOD_TYPE_OPTIONS.map((option) => (
                            <SelectItem key={option} value={option}>
                              {FOOD_TYPE_LABEL[option]}
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
                name="displayName"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="food-type-category-display-name">노출명</FieldLabel>
                    <Input
                      {...field}
                      id="food-type-category-display-name"
                      placeholder="예: 한식"
                      maxLength={FOOD_TYPE_DISPLAY_NAME_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />
              <Controller
                control={form.control}
                name="activeImageFileId"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="food-type-category-active-image">활성 이미지</FieldLabel>
                    <Input
                      id="food-type-category-active-image"
                      type="file"
                      accept="image/*"
                      onChange={(e) => handleFileChange(e, "activeImageFileId")}
                      disabled={busy}
                    />
                    {uploadingField === "activeImageFileId" && (
                      <p className="text-muted-foreground text-xs">업로드 중...</p>
                    )}
                    {field.value ? (
                      <p className="text-muted-foreground text-xs">업로드된 파일 ID: {field.value}</p>
                    ) : null}
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />
              <Controller
                control={form.control}
                name="inactiveImageFileId"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="food-type-category-inactive-image">비활성 이미지</FieldLabel>
                    <Input
                      id="food-type-category-inactive-image"
                      type="file"
                      accept="image/*"
                      onChange={(e) => handleFileChange(e, "inactiveImageFileId")}
                      disabled={busy}
                    />
                    {uploadingField === "inactiveImageFileId" && (
                      <p className="text-muted-foreground text-xs">업로드 중...</p>
                    )}
                    {field.value ? (
                      <p className="text-muted-foreground text-xs">업로드된 파일 ID: {field.value}</p>
                    ) : null}
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />
              <Controller
                control={form.control}
                name="sort"
                render={({ field, fieldState }) => (
                  <Field className="w-24 gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="food-type-category-sort">정렬</FieldLabel>
                    <Input
                      id="food-type-category-sort"
                      type="number"
                      value={field.value}
                      onChange={(e) => field.onChange(Number(e.target.value))}
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
                    <FieldLabel htmlFor="food-type-category-visible">노출 여부</FieldLabel>
                    <Switch
                      id="food-type-category-visible"
                      checked={field.value}
                      onCheckedChange={field.onChange}
                      disabled={busy}
                    />
                  </Field>
                )}
              />
              <div className="flex gap-2">
                <Button type="submit" size="sm" disabled={busy}>
                  {isPending ? "저장 중..." : editingId ? "수정" : "카테고리 추가"}
                </Button>
                {editingId ? (
                  <Button type="button" size="sm" variant="outline" onClick={cancelEdit} disabled={busy}>
                    취소
                  </Button>
                ) : null}
              </div>
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
