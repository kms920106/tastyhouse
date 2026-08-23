"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel, FieldSeparator } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import { loadShopCategoriesAction } from "@/feature/product/actions";
import { SPICINESS_OPTIONS } from "@/feature/product/constants";
import type { MenuCategory, ProductShopLinkInput } from "@/feature/product/domain";
import { PRODUCT_MENU_COPY, PRODUCT_SHOP_LINK_COPY, PRODUCT_SHOP_LINK_MESSAGE } from "@/feature/product/message";
import { type MenuFormValues, menuFormSchema } from "@/feature/product/schema";
import type { ShopSummary } from "@/feature/shop/domain";

/**
 * 미분류 센티넬.
 *
 * Radix `Select` 는 빈 문자열을 항목 값으로 쓸 수 없다(빈 문자열은 "선택 해제"의 내부 표현이라
 * `SelectItem value=""` 이 런타임 에러를 낸다). 미분류를 고를 수 있어야 하므로 전용 토큰을 쓰고
 * 제출 시 `null` 로 되돌린다.
 */
const NO_CATEGORY_VALUE = "NONE";

const FORM_ID = "menu-create-form";

const DEFAULT_VALUES: MenuFormValues = {
  name: "",
  productCategoryId: NO_CATEGORY_VALUE,
  composition: "",
  description: "",
  weightText: "",
  originalPrice: "",
  discountPrice: "",
  singleServing: false,
  representative: false,
  spiciness: "",
  ratingExcluded: false,
};

export interface MenuCreateSubmitValues {
  name: string;
  productCategoryId: number | null;
  composition: string;
  description: string;
  weightText: string;
  originalPrice: number;
  discountPrice: number | null;
  singleServing: boolean;
  representative: boolean;
  spiciness: number | null;
  ratingExcluded: boolean;
}

interface MenuCreateDialogProps {
  open: boolean;
  pending?: boolean;
  categories: MenuCategory[];
  /** 현재 보고 있는 가게. 다중 선택에서 항상 켜져 있는 기준 가게다 */
  shopId?: number;
  /** 점주 소유 가게 전체. **2개 이상일 때만** 다중 선택 UI 를 보여준다 */
  shops: ShopSummary[];
  /** 미분류 그룹에 메뉴가 있으면 Select 에 미분류 항목을 노출한다 */
  onOpenChange: (open: boolean) => void;
  /** `links` 는 다중 선택을 쓴 경우에만 채워진다 — 가게 1개인 점주는 undefined 다 */
  onSubmit: (values: MenuCreateSubmitValues, links?: ProductShopLinkInput[]) => void;
}

/**
 * 메뉴 등록 폼.
 *
 * 원문 PDF 는 4단계 위저드지만 이 앱에는 위저드 선례가 없고 `Dialog` 단일 폼이 관례이므로
 * **한 화면의 섹션 구분**으로 구현한다(`docs/tasks/frontend.md` §3-4).
 *
 * 중복 메뉴명·금칙어는 DB 를 봐야 알 수 있어 클라이언트가 판정할 수 없다 —
 * 서버가 내려준 한국어 문구를 호출부가 토스트로 그대로 노출한다.
 */
export function MenuCreateDialog({
  open,
  pending,
  categories,
  shopId,
  shops,
  onOpenChange,
  onSubmit,
}: MenuCreateDialogProps) {
  /**
   * 다중 가게 지정은 **소유 가게가 2개 이상일 때만** 보인다(PDF STEP 3 / `frontend.md` §E).
   * 가게가 하나인 점주에게 고를 것이 하나뿐인 선택을 강요하지 않는다.
   */
  const isMultiShop = shops.length >= 2;

  /** 추가로 연결할 가게. 기준 가게(`shopId`)는 항상 연결되므로 여기 담지 않는다 */
  const [extraShopIds, setExtraShopIds] = React.useState<ReadonlySet<number>>(() => new Set());
  /** 추가 가게별로 고른 메뉴그룹. 기준 가게는 `productCategoryId` 폼 필드를 그대로 쓴다 */
  const [extraCategoryValues, setExtraCategoryValues] = React.useState<Record<number, string>>({});
  /** 가게별 메뉴그룹 캐시(가게당 한 번만 조회). 기준 가게는 `categories` prop 으로 미리 채운다 */
  const [categoriesByShopId, setCategoriesByShopId] = React.useState<Record<number, MenuCategory[]>>({});
  const [loadingCategoryShopIds, setLoadingCategoryShopIds] = React.useState<ReadonlySet<number>>(() => new Set());

  const form = useForm<MenuFormValues>({
    resolver: zodResolver(menuFormSchema),
    defaultValues: DEFAULT_VALUES,
  });

  // 닫았다 다시 열면 이전 입력이 남지 않도록 초기화한다.
  React.useEffect(() => {
    if (open) {
      form.reset(DEFAULT_VALUES);
      setExtraShopIds(new Set());
      setExtraCategoryValues({});
      setCategoriesByShopId(shopId !== undefined ? { [shopId]: categories } : {});
      setLoadingCategoryShopIds(new Set());
    }
    // biome-ignore lint/correctness/useExhaustiveDependencies: shopId/categories는 열리는 시점 값만 쓴다
  }, [open, form]);

  /** 대상 가게의 메뉴그룹을 아직 모르면 서버에서 불러와 캐시한다(가게당 한 번만) */
  function ensureShopCategoriesLoaded(targetShopId: number) {
    if (categoriesByShopId[targetShopId] !== undefined || loadingCategoryShopIds.has(targetShopId)) {
      return;
    }

    setLoadingCategoryShopIds((prev) => new Set(prev).add(targetShopId));

    void loadShopCategoriesAction(targetShopId).then(({ success, message, data }) => {
      setLoadingCategoryShopIds((prev) => {
        const updated = new Set(prev);
        updated.delete(targetShopId);
        return updated;
      });

      if (!success || !data) {
        toast.error(message ?? PRODUCT_SHOP_LINK_MESSAGE.LOAD_FAILED);
        return;
      }

      setCategoriesByShopId((prev) => ({ ...prev, [targetShopId]: data }));
    });
  }

  const handleSubmit = (values: MenuFormValues) => {
    const productCategoryId = values.productCategoryId === NO_CATEGORY_VALUE ? null : Number(values.productCategoryId);

    /**
     * 다중 연결을 실제로 쓴 경우에만 `links` 를 만든다.
     *
     * 추가 가게를 하나도 고르지 않았으면 단일 등록과 동작이 같으므로 `undefined` 로 두어
     * 서버의 기존 경로를 그대로 태운다.
     *
     * 추가 가게는 그 가게의 메뉴그룹을 따로 고른다(`extraCategoryValues`) — 기준 가게에서 고른
     * 그룹 id 를 재사용하면 그 가게의 그룹이 아닐 때 서버가 `PRODUCT_SHOP_LINK_CATEGORY_MISMATCH`
     * 로 거절한다.
     */
    if (isMultiShop && extraShopIds.size > 0 && productCategoryId !== null) {
      const missingCategory = Array.from(extraShopIds).some((linkShopId) => {
        const categoryId = Number(extraCategoryValues[linkShopId] ?? "");
        return !Number.isInteger(categoryId) || categoryId <= 0;
      });

      if (missingCategory) {
        toast.error(PRODUCT_SHOP_LINK_MESSAGE.CATEGORY_REQUIRED);
        return;
      }
    }

    const links: ProductShopLinkInput[] | undefined =
      isMultiShop && extraShopIds.size > 0 && shopId !== undefined && productCategoryId !== null
        ? [
            { shopId, productCategoryId },
            ...Array.from(extraShopIds).map((linkShopId) => ({
              shopId: linkShopId,
              productCategoryId: Number(extraCategoryValues[linkShopId] ?? ""),
            })),
          ]
        : undefined;

    onSubmit(
      {
        name: values.name.trim(),
        productCategoryId,
        composition: values.composition.trim(),
        description: values.description.trim(),
        weightText: values.weightText.trim(),
        originalPrice: Number(values.originalPrice),
        // 빈 문자열은 "할인 없음"이다 — 0 으로 보내면 전액 할인이 된다.
        discountPrice: values.discountPrice.trim() === "" ? null : Number(values.discountPrice),
        singleServing: values.singleServing,
        representative: values.representative,
        spiciness: values.spiciness === "" ? null : Number(values.spiciness),
        ratingExcluded: values.ratingExcluded,
      },
      links,
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90dvh] overflow-y-auto sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>{PRODUCT_MENU_COPY.DIALOG_MENU_CREATE_TITLE}</DialogTitle>
          <DialogDescription>{PRODUCT_MENU_COPY.DIALOG_MENU_CREATE_DESCRIPTION}</DialogDescription>
        </DialogHeader>

        <form id={FORM_ID} noValidate onSubmit={form.handleSubmit(handleSubmit)}>
          <FieldGroup className="gap-4">
            <Controller
              control={form.control}
              name="name"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="menu-create-name">{PRODUCT_MENU_COPY.FIELD_NAME}</FieldLabel>
                  <Input
                    id="menu-create-name"
                    value={field.value}
                    onChange={field.onChange}
                    aria-invalid={fieldState.invalid}
                    disabled={pending}
                  />
                  <FieldDescription>{PRODUCT_MENU_COPY.HELP_NAME}</FieldDescription>
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />

            <Controller
              control={form.control}
              name="productCategoryId"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="menu-create-category">{PRODUCT_MENU_COPY.FIELD_CATEGORY}</FieldLabel>
                  {/* Radix Select 의 value 는 lifetime 내내 문자열이어야 한다 — undefined 로
                      뒤집히면 uncontrolled → controlled 경고가 난다. */}
                  <Select value={field.value ?? ""} onValueChange={field.onChange} disabled={pending}>
                    <SelectTrigger id="menu-create-category" aria-invalid={fieldState.invalid}>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent position="popper">
                      <SelectGroup>
                        <SelectItem value={NO_CATEGORY_VALUE}>{PRODUCT_MENU_COPY.PLACEHOLDER_CATEGORY_NONE}</SelectItem>
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
              name="composition"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="menu-create-composition">{PRODUCT_MENU_COPY.FIELD_COMPOSITION}</FieldLabel>
                  <Textarea
                    id="menu-create-composition"
                    rows={2}
                    value={field.value}
                    onChange={field.onChange}
                    aria-invalid={fieldState.invalid}
                    disabled={pending}
                  />
                  <FieldDescription>{PRODUCT_MENU_COPY.HELP_COMPOSITION}</FieldDescription>
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />

            <Controller
              control={form.control}
              name="description"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="menu-create-description">{PRODUCT_MENU_COPY.FIELD_DESCRIPTION}</FieldLabel>
                  <Textarea
                    id="menu-create-description"
                    rows={3}
                    value={field.value}
                    onChange={field.onChange}
                    aria-invalid={fieldState.invalid}
                    disabled={pending}
                  />
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />

            {/* 소유 가게가 2개 이상일 때만 노출. 하나뿐인 점주에게 선택을 강요하지 않는다 */}
            {isMultiShop && (
              <>
                <FieldSeparator />

                <Field className="gap-1.5">
                  <FieldLabel>{PRODUCT_SHOP_LINK_COPY.SECTION_TITLE}</FieldLabel>
                  <FieldDescription>{PRODUCT_SHOP_LINK_COPY.GUIDE}</FieldDescription>

                  <div className="flex flex-col gap-2 pt-1">
                    {shops.map((shop) => {
                      // 기준 가게는 항상 연결된다 — 끌 수 있으면 어느 가게 메뉴인지가 모호해진다.
                      const isBase = shop.id === shopId;
                      const inputId = `menu-create-shop-${shop.id}`;
                      const checked = isBase || extraShopIds.has(shop.id);
                      const shopCategories = categoriesByShopId[shop.id];
                      const isLoadingCategories = loadingCategoryShopIds.has(shop.id);
                      const canPickCategory = shopCategories !== undefined && shopCategories.length > 0;

                      return (
                        <div key={shop.id} className="flex flex-col gap-2">
                          <div className="flex items-center gap-2">
                            <Checkbox
                              id={inputId}
                              checked={checked}
                              disabled={pending || isBase}
                              onCheckedChange={(nextChecked) => {
                                if (nextChecked === true) ensureShopCategoriesLoaded(shop.id);
                                setExtraShopIds((prev) => {
                                  const next = new Set(prev);
                                  if (nextChecked === true) next.add(shop.id);
                                  else next.delete(shop.id);
                                  return next;
                                });
                              }}
                            />
                            <FieldLabel htmlFor={inputId} className="font-normal">
                              {shop.name}
                            </FieldLabel>
                          </div>

                          {/* 기준 가게는 위 "메뉴그룹" Select 를 그대로 쓴다 — 여기서는 추가 가게만 고른다 */}
                          {!isBase &&
                            checked &&
                            (canPickCategory ? (
                              <Select
                                value={extraCategoryValues[shop.id] ?? ""}
                                disabled={pending}
                                onValueChange={(value) =>
                                  setExtraCategoryValues((prev) => ({ ...prev, [shop.id]: value }))
                                }
                              >
                                <SelectTrigger className="w-full">
                                  <SelectValue placeholder={PRODUCT_SHOP_LINK_COPY.CATEGORY_PLACEHOLDER} />
                                </SelectTrigger>
                                <SelectContent position="popper">
                                  {shopCategories.map((category) => (
                                    <SelectItem key={category.id} value={String(category.id)}>
                                      {category.name}
                                    </SelectItem>
                                  ))}
                                </SelectContent>
                              </Select>
                            ) : isLoadingCategories ? (
                              <Skeleton className="h-9 w-full" />
                            ) : (
                              <span className="text-muted-foreground text-xs">
                                {PRODUCT_SHOP_LINK_COPY.EMPTY_CATEGORIES}
                              </span>
                            ))}
                        </div>
                      );
                    })}
                  </div>

                  {/* 여러 가게에 걸면 가격이 공유된다 — 등록 전에 알려야 되돌릴 수 있다 */}
                  {extraShopIds.size > 0 && (
                    <FieldDescription>{PRODUCT_SHOP_LINK_COPY.PRICE_SHARED_WARNING}</FieldDescription>
                  )}

                  {/* 다중 연결은 메뉴그룹이 필수다(PDF STEP 3). 미분류면 단일 등록으로 떨어진다 */}
                  {extraShopIds.size > 0 && form.watch("productCategoryId") === NO_CATEGORY_VALUE && (
                    <FieldDescription className="text-destructive">
                      {PRODUCT_SHOP_LINK_MESSAGE.CATEGORY_REQUIRED}
                    </FieldDescription>
                  )}
                </Field>
              </>
            )}

            <FieldSeparator />

            <div className="grid gap-4 sm:grid-cols-2">
              <Controller
                control={form.control}
                name="originalPrice"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="menu-create-original-price">
                      {PRODUCT_MENU_COPY.FIELD_ORIGINAL_PRICE}
                    </FieldLabel>
                    <Input
                      id="menu-create-original-price"
                      type="number"
                      min={0}
                      value={field.value}
                      onChange={field.onChange}
                      aria-invalid={fieldState.invalid}
                      disabled={pending}
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
                    <FieldLabel htmlFor="menu-create-discount-price">
                      {PRODUCT_MENU_COPY.FIELD_DISCOUNT_PRICE}
                    </FieldLabel>
                    <Input
                      id="menu-create-discount-price"
                      type="number"
                      min={0}
                      value={field.value}
                      onChange={field.onChange}
                      aria-invalid={fieldState.invalid}
                      disabled={pending}
                    />
                    <FieldDescription>{PRODUCT_MENU_COPY.HELP_DISCOUNT_PRICE}</FieldDescription>
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />
            </div>

            <FieldSeparator />

            <Controller
              control={form.control}
              name="spiciness"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="menu-create-spiciness">{PRODUCT_MENU_COPY.FIELD_SPICINESS}</FieldLabel>
                  <Select value={field.value ?? ""} onValueChange={field.onChange} disabled={pending}>
                    <SelectTrigger id="menu-create-spiciness" aria-invalid={fieldState.invalid}>
                      <SelectValue placeholder={PRODUCT_MENU_COPY.PLACEHOLDER_SELECT} />
                    </SelectTrigger>
                    <SelectContent position="popper">
                      <SelectGroup>
                        {SPICINESS_OPTIONS.map((option) => (
                          <SelectItem key={option.value} value={String(option.value)}>
                            {option.label}
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
              name="singleServing"
              render={({ field }) => (
                <Field orientation="horizontal" className="gap-3">
                  <Switch
                    id="menu-create-single-serving"
                    checked={field.value}
                    onCheckedChange={field.onChange}
                    disabled={pending}
                  />
                  <div className="flex flex-col gap-0.5">
                    <FieldLabel htmlFor="menu-create-single-serving">
                      {PRODUCT_MENU_COPY.FIELD_SINGLE_SERVING}
                    </FieldLabel>
                    <FieldDescription>{PRODUCT_MENU_COPY.HELP_SINGLE_SERVING}</FieldDescription>
                  </div>
                </Field>
              )}
            />

            <Controller
              control={form.control}
              name="representative"
              render={({ field }) => (
                <Field orientation="horizontal" className="gap-3">
                  <Switch
                    id="menu-create-representative"
                    checked={field.value}
                    onCheckedChange={field.onChange}
                    disabled={pending}
                  />
                  <div className="flex flex-col gap-0.5">
                    <FieldLabel htmlFor="menu-create-representative">
                      {PRODUCT_MENU_COPY.FIELD_REPRESENTATIVE}
                    </FieldLabel>
                    <FieldDescription>{PRODUCT_MENU_COPY.HELP_REPRESENTATIVE}</FieldDescription>
                  </div>
                </Field>
              )}
            />

            <Controller
              control={form.control}
              name="ratingExcluded"
              render={({ field }) => (
                <Field orientation="horizontal" className="gap-3">
                  <Switch
                    id="menu-create-rating-excluded"
                    checked={field.value}
                    onCheckedChange={field.onChange}
                    disabled={pending}
                  />
                  <div className="flex flex-col gap-0.5">
                    <FieldLabel htmlFor="menu-create-rating-excluded">
                      {PRODUCT_MENU_COPY.FIELD_RATING_EXCLUDED}
                    </FieldLabel>
                    <FieldDescription>{PRODUCT_MENU_COPY.HELP_RATING_EXCLUDED}</FieldDescription>
                  </div>
                </Field>
              )}
            />
          </FieldGroup>
        </form>

        <DialogFooter>
          <Button type="submit" form={FORM_ID} disabled={pending}>
            {PRODUCT_MENU_COPY.BUTTON_SAVE}
          </Button>
          <DialogClose asChild>
            <Button variant="outline" disabled={pending}>
              {PRODUCT_MENU_COPY.BUTTON_CANCEL}
            </Button>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
