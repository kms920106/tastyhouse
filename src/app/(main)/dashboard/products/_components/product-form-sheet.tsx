"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
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
import { Textarea } from "@/components/ui/textarea";
import {
  createProductAction,
  fetchCategoriesAction,
  fetchProductAction,
  updateProductAction,
} from "@/feature/product/actions";
import type { ProductCategory, ProductListItem } from "@/feature/product/domain";
import { PRODUCT_MESSAGE } from "@/feature/product/message";
import {
  PRODUCT_DESC_MAX,
  PRODUCT_NAME_MAX,
  type ProductFormValues,
  productFormSchema,
} from "@/feature/product/schema";

interface ProductFormSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  product?: Pick<ProductListItem, "id"> | null;
}

const EMPTY_VALUES: ProductFormValues = {
  shopId: undefined as unknown as number,
  productCategoryId: undefined,
  name: "",
  description: undefined,
  originalPrice: 0,
  discountPrice: undefined,
  discountRate: undefined,
  rating: undefined,
  reviewCount: undefined,
  representative: false,
  spiciness: undefined,
  soldOut: false,
  visible: true,
  sort: 0,
};

/** 숫자 input onChange: 빈 값이면 undefined, 아니면 Number */
function parseOptionalNumber(value: string): number | undefined {
  return value.trim() === "" ? undefined : Number(value);
}

export function ProductFormSheet({ open, onOpenChange, product }: ProductFormSheetProps) {
  const isEdit = Boolean(product);
  const [isPending, startTransition] = React.useTransition();
  const [isLoadingDetail, setIsLoadingDetail] = React.useState(false);
  const [categories, setCategories] = React.useState<ProductCategory[]>([]);
  const [isLoadingCategories, setIsLoadingCategories] = React.useState(false);

  const form = useForm<ProductFormValues>({
    resolver: zodResolver(productFormSchema),
    defaultValues: EMPTY_VALUES,
  });

  // 진행 중인 카테고리 조회 중 가장 마지막 요청의 shopId. 응답이 늦게 와도 최신 요청 결과만 반영한다.
  const latestShopIdRef = React.useRef<number | undefined>(undefined);

  // shopId 가 유효한 양수일 때만 카테고리 목록을 조회한다. (400 방지)
  const loadCategories = React.useCallback((shopId: number | undefined) => {
    latestShopIdRef.current = shopId;

    if (shopId === undefined || !Number.isInteger(shopId) || shopId <= 0) {
      setCategories([]);
      return;
    }

    setIsLoadingCategories(true);
    void fetchCategoriesAction(shopId).then((result) => {
      if (latestShopIdRef.current !== shopId) return;
      setIsLoadingCategories(false);
      if (result.success && result.data) {
        setCategories(result.data);
      } else {
        setCategories([]);
      }
    });
  }, []);

  // 시트가 열릴 때마다 대상 값으로 초기화한다. 수정 모드는 상세를 조회해 값을 확보한다.
  React.useEffect(() => {
    if (!open) return;

    if (!product) {
      form.reset(EMPTY_VALUES);
      setCategories([]);
      return;
    }

    let active = true;
    setIsLoadingDetail(true);

    void fetchProductAction(product.id).then((result) => {
      if (!active) return;
      setIsLoadingDetail(false);

      if (!result.success || !result.data) {
        toast.error(result.message ?? PRODUCT_MESSAGE.DETAIL_LOAD_FAILED);
        onOpenChange(false);
        return;
      }

      const detail = result.data;
      form.reset({
        shopId: detail.shopId,
        productCategoryId: detail.productCategoryId ?? undefined,
        name: detail.name,
        description: detail.description ?? undefined,
        originalPrice: detail.originalPrice,
        discountPrice: detail.discountPrice ?? undefined,
        discountRate: detail.discountRate ?? undefined,
        rating: detail.rating ?? undefined,
        reviewCount: detail.reviewCount ?? undefined,
        representative: detail.representative,
        spiciness: detail.spiciness ?? undefined,
        soldOut: detail.soldOut,
        visible: detail.visible,
        sort: detail.sort,
      });
      loadCategories(detail.shopId);
    });

    return () => {
      active = false;
    };
  }, [open, product, form.reset, onOpenChange, loadCategories]);

  const onSubmit = (values: ProductFormValues) => {
    startTransition(async () => {
      const { success, message } = product
        ? await updateProductAction(product.id, values)
        : await createProductAction(values);

      if (success) {
        toast.success(isEdit ? PRODUCT_MESSAGE.UPDATE_SUCCESS : PRODUCT_MESSAGE.CREATE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? PRODUCT_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  const busy = isPending || isLoadingDetail;
  const categoriesDisabled = busy || isLoadingCategories || categories.length === 0;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{isEdit ? "상품 수정" : "상품 등록"}</SheetTitle>
          <SheetDescription>
            {isEdit
              ? "상품 정보를 수정합니다. 품절 되돌리기는 이 화면의 품절 여부 스위치로만 가능합니다."
              : "새로운 상품을 등록합니다. 매장 ID를 입력하면 해당 매장의 카테고리를 선택할 수 있습니다."}
          </SheetDescription>
        </SheetHeader>

        {isLoadingDetail ? (
          <div className="flex-1 space-y-3 px-4">
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
          </div>
        ) : (
          <form
            id="product-form"
            noValidate
            onSubmit={form.handleSubmit(onSubmit)}
            className="flex-1 overflow-y-auto px-4"
          >
            <FieldGroup className="gap-4">
              <Controller
                control={form.control}
                name="shopId"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="product-shop-id">매장 ID</FieldLabel>
                    <Input
                      id="product-shop-id"
                      type="number"
                      min={1}
                      placeholder="매장 ID를 입력하세요"
                      value={field.value ?? ""}
                      onChange={(e) => field.onChange(parseOptionalNumber(e.target.value))}
                      onBlur={() => {
                        field.onBlur();
                        // 매장이 바뀌면 이전 카테고리 선택을 초기화하고 새 매장 카테고리를 조회한다.
                        form.setValue("productCategoryId", undefined);
                        loadCategories(field.value);
                      }}
                      aria-invalid={fieldState.invalid}
                      disabled={busy || isEdit}
                    />
                    {isEdit ? (
                      <p className="text-muted-foreground text-xs">매장은 등록 후 변경할 수 없습니다.</p>
                    ) : null}
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="productCategoryId"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="product-category">카테고리</FieldLabel>
                    <Select
                      value={field.value === undefined ? "" : String(field.value)}
                      onValueChange={(value) => field.onChange(value === "" ? undefined : Number(value))}
                      disabled={categoriesDisabled}
                    >
                      <SelectTrigger id="product-category" className="w-full" aria-invalid={fieldState.invalid}>
                        <SelectValue
                          placeholder={
                            isLoadingCategories
                              ? "불러오는 중..."
                              : categories.length === 0
                                ? "매장 ID 입력 후 선택 가능"
                                : "카테고리 선택"
                          }
                        />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectGroup>
                          {categories.map((category) => (
                            <SelectItem key={category.id} value={String(category.id)}>
                              {category.name}
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
                name="name"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="product-name">상품 이름</FieldLabel>
                    <Input
                      {...field}
                      id="product-name"
                      placeholder="상품 이름을 입력하세요"
                      maxLength={PRODUCT_NAME_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="description"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="product-description">설명</FieldLabel>
                    <Textarea
                      {...field}
                      value={field.value ?? ""}
                      id="product-description"
                      placeholder="상품 설명을 입력하세요 (선택)"
                      maxLength={PRODUCT_DESC_MAX}
                      rows={3}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="originalPrice"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="product-original-price">정가 (원)</FieldLabel>
                    <Input
                      id="product-original-price"
                      type="number"
                      min={0}
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
                name="discountPrice"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="product-discount-price">할인가 (원, 미입력=없음)</FieldLabel>
                    <Input
                      id="product-discount-price"
                      type="number"
                      min={0}
                      value={field.value ?? ""}
                      onChange={(e) => field.onChange(parseOptionalNumber(e.target.value))}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="discountRate"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="product-discount-rate">할인율 (%, 미입력=없음)</FieldLabel>
                    <Input
                      id="product-discount-rate"
                      type="number"
                      min={0}
                      max={100}
                      step="0.01"
                      value={field.value ?? ""}
                      onChange={(e) => field.onChange(parseOptionalNumber(e.target.value))}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="spiciness"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="product-spiciness">맵기 단계 (0~5, 미입력=미지정)</FieldLabel>
                    <Input
                      id="product-spiciness"
                      type="number"
                      min={0}
                      max={5}
                      value={field.value ?? ""}
                      onChange={(e) => field.onChange(parseOptionalNumber(e.target.value))}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="rating"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="product-rating">평점 (미입력=없음)</FieldLabel>
                    <Input
                      id="product-rating"
                      type="number"
                      min={0}
                      value={field.value ?? ""}
                      onChange={(e) => field.onChange(parseOptionalNumber(e.target.value))}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="reviewCount"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="product-review-count">리뷰 수 (미입력=없음)</FieldLabel>
                    <Input
                      id="product-review-count"
                      type="number"
                      min={0}
                      value={field.value ?? ""}
                      onChange={(e) => field.onChange(parseOptionalNumber(e.target.value))}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="sort"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="product-sort">정렬 순서</FieldLabel>
                    <Input
                      id="product-sort"
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
                name="representative"
                render={({ field }) => (
                  <Field orientation="horizontal">
                    <FieldLabel htmlFor="product-representative">대표 상품</FieldLabel>
                    <Switch
                      id="product-representative"
                      checked={field.value}
                      onCheckedChange={field.onChange}
                      disabled={busy}
                    />
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="soldOut"
                render={({ field }) => (
                  <Field orientation="horizontal">
                    <FieldLabel htmlFor="product-sold-out">품절 여부</FieldLabel>
                    <Switch
                      id="product-sold-out"
                      checked={field.value}
                      onCheckedChange={field.onChange}
                      disabled={busy}
                    />
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="visible"
                render={({ field }) => (
                  <Field orientation="horizontal">
                    <FieldLabel htmlFor="product-visible">노출 여부</FieldLabel>
                    <Switch
                      id="product-visible"
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
          <Button type="submit" form="product-form" disabled={busy}>
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
