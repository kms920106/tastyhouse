"use client";

import * as React from "react";

import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Field, FieldLabel } from "@/components/ui/field";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import {
  createOrderMethodAction,
  createShopAmenityAction,
  createShopFoodTypeAction,
  deleteOrderMethodAction,
  deleteShopAmenityAction,
  deleteShopFoodTypeAction,
  fetchAmenityCategoriesAction,
  fetchFoodTypeCategoriesAction,
  fetchOrderMethodsAction,
  fetchShopAmenitiesAction,
  fetchShopFoodTypesAction,
} from "@/feature/shop/actions";
import { ORDER_METHOD_LABEL, ORDER_METHOD_OPTIONS } from "@/feature/shop/constants";
import type { AmenityCategory, FoodTypeCategory, OrderMethod, ShopAmenity, ShopFoodType } from "@/feature/shop/domain";
import { SHOP_MESSAGE } from "@/feature/shop/message";

interface TabProps {
  shopId: number;
}

function AmenitiesSection({ shopId }: TabProps) {
  const [assigned, setAssigned] = React.useState<ShopAmenity[]>([]);
  const [categories, setCategories] = React.useState<AmenityCategory[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [selected, setSelected] = React.useState<string>("");
  const [isPending, startTransition] = React.useTransition();

  const load = React.useCallback(() => {
    setIsLoading(true);
    setError(null);
    void Promise.all([fetchShopAmenitiesAction(shopId), fetchAmenityCategoriesAction()]).then(
      ([assignedResult, categoriesResult]) => {
        setIsLoading(false);
        if (assignedResult.success && assignedResult.data) {
          setAssigned(assignedResult.data);
        } else {
          setError(assignedResult.message ?? SHOP_MESSAGE.SHOP_AMENITIES_LOAD_FAILED);
        }
        if (categoriesResult.success && categoriesResult.data) {
          setCategories(categoriesResult.data);
        }
      },
    );
  }, [shopId]);

  React.useEffect(() => {
    load();
  }, [load]);

  const availableCategories = categories.filter(
    (category) => !assigned.some((item) => item.amenityCategoryId === category.id),
  );

  function handleAssign() {
    const amenityCategoryId = Number(selected);
    if (!Number.isInteger(amenityCategoryId) || amenityCategoryId <= 0) return;
    startTransition(async () => {
      const { success, message } = await createShopAmenityAction(shopId, { amenityCategoryId });
      if (success) {
        toast.success(SHOP_MESSAGE.SHOP_AMENITY_CREATE_SUCCESS);
        setSelected("");
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  }

  function handleUnassign(amenityCategoryId: number) {
    startTransition(async () => {
      const { success, message } = await deleteShopAmenityAction(shopId, amenityCategoryId);
      if (success) {
        toast.success(SHOP_MESSAGE.SHOP_AMENITY_DELETE_SUCCESS);
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <div className="space-y-3">
      <h4 className="font-medium text-sm">편의시설</h4>
      {error ? (
        <p className="text-destructive text-sm">{error}</p>
      ) : isLoading ? (
        <Skeleton className="h-16 w-full" />
      ) : assigned.length ? (
        <ul className="flex flex-wrap gap-2">
          {assigned.map((item) => {
            // 응답의 displayName 을 우선 사용하되, 아직 백엔드에 신규 필드가 반영되기 전이라면
            // 마스터 카테고리 목록에서 보완한다. (스펙 변경이 additive 라 하위 호환 유지)
            const displayName =
              item.displayName || categories.find((c) => c.id === item.amenityCategoryId)?.displayName || item.amenity;
            return (
              <li
                key={item.amenityCategoryId}
                className="flex items-center gap-2 rounded-md border px-3 py-1.5 text-sm"
              >
                {item.activeFilePath ? (
                  // biome-ignore lint/performance/noImgElement: CDN 아이콘 미리보기
                  <img
                    src={item.activeFilePath}
                    alt=""
                    className="size-4 object-contain"
                    onError={(e) => {
                      e.currentTarget.style.display = "none";
                    }}
                  />
                ) : null}
                <span>{displayName}</span>
                <button
                  type="button"
                  className="text-destructive text-xs disabled:opacity-50"
                  disabled={isPending}
                  onClick={() => handleUnassign(item.amenityCategoryId)}
                >
                  해제
                </button>
              </li>
            );
          })}
        </ul>
      ) : (
        <p className="text-muted-foreground text-sm">지정된 편의시설이 없습니다.</p>
      )}

      <div className="flex items-end gap-2">
        <Field className="w-64 gap-1.5">
          <FieldLabel htmlFor="amenity-select">편의시설 지정</FieldLabel>
          <Select value={selected} onValueChange={setSelected} disabled={isPending || availableCategories.length === 0}>
            <SelectTrigger id="amenity-select" className="w-full">
              <SelectValue
                placeholder={availableCategories.length === 0 ? "지정 가능한 편의시설 없음" : "편의시설 선택"}
              />
            </SelectTrigger>
            <SelectContent>
              <SelectGroup>
                {availableCategories.map((category) => (
                  <SelectItem key={category.id} value={String(category.id)}>
                    {category.displayName}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
        </Field>
        <Button type="button" size="sm" onClick={handleAssign} disabled={isPending || !selected}>
          지정
        </Button>
      </div>
    </div>
  );
}

function FoodTypesSection({ shopId }: TabProps) {
  const [assigned, setAssigned] = React.useState<ShopFoodType[]>([]);
  const [categories, setCategories] = React.useState<FoodTypeCategory[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [selected, setSelected] = React.useState<string>("");
  const [isPending, startTransition] = React.useTransition();

  const load = React.useCallback(() => {
    setIsLoading(true);
    setError(null);
    void Promise.all([fetchShopFoodTypesAction(shopId), fetchFoodTypeCategoriesAction()]).then(
      ([assignedResult, categoriesResult]) => {
        setIsLoading(false);
        if (assignedResult.success && assignedResult.data) {
          setAssigned(assignedResult.data);
        } else {
          setError(assignedResult.message ?? SHOP_MESSAGE.SHOP_FOOD_TYPES_LOAD_FAILED);
        }
        if (categoriesResult.success && categoriesResult.data) {
          setCategories(categoriesResult.data);
        }
      },
    );
  }, [shopId]);

  React.useEffect(() => {
    load();
  }, [load]);

  const availableCategories = categories.filter(
    (category) => !assigned.some((item) => item.foodTypeCategoryId === category.id),
  );

  function handleAssign() {
    const foodTypeCategoryId = Number(selected);
    if (!Number.isInteger(foodTypeCategoryId) || foodTypeCategoryId <= 0) return;
    startTransition(async () => {
      const { success, message } = await createShopFoodTypeAction(shopId, { foodTypeCategoryId });
      if (success) {
        toast.success(SHOP_MESSAGE.SHOP_FOOD_TYPE_CREATE_SUCCESS);
        setSelected("");
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  }

  function handleUnassign(foodTypeCategoryId: number) {
    startTransition(async () => {
      const { success, message } = await deleteShopFoodTypeAction(shopId, foodTypeCategoryId);
      if (success) {
        toast.success(SHOP_MESSAGE.SHOP_FOOD_TYPE_DELETE_SUCCESS);
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <div className="space-y-3">
      <h4 className="font-medium text-sm">음식종류</h4>
      {error ? (
        <p className="text-destructive text-sm">{error}</p>
      ) : isLoading ? (
        <Skeleton className="h-16 w-full" />
      ) : assigned.length ? (
        <ul className="flex flex-wrap gap-2">
          {assigned.map((item) => {
            // 응답의 displayName 을 우선 사용하되, 아직 백엔드에 신규 필드가 반영되기 전이라면
            // 마스터 카테고리 목록에서 보완한다. (스펙 변경이 additive 라 하위 호환 유지)
            const displayName =
              item.displayName ||
              categories.find((c) => c.id === item.foodTypeCategoryId)?.displayName ||
              item.foodType;
            return (
              <li
                key={item.foodTypeCategoryId}
                className="flex items-center gap-2 rounded-md border px-3 py-1.5 text-sm"
              >
                {item.activeFilePath ? (
                  // biome-ignore lint/performance/noImgElement: CDN 아이콘 미리보기
                  <img
                    src={item.activeFilePath}
                    alt=""
                    className="size-4 object-contain"
                    onError={(e) => {
                      e.currentTarget.style.display = "none";
                    }}
                  />
                ) : null}
                <span>{displayName}</span>
                <button
                  type="button"
                  className="text-destructive text-xs disabled:opacity-50"
                  disabled={isPending}
                  onClick={() => handleUnassign(item.foodTypeCategoryId)}
                >
                  해제
                </button>
              </li>
            );
          })}
        </ul>
      ) : (
        <p className="text-muted-foreground text-sm">지정된 음식종류가 없습니다.</p>
      )}

      <div className="flex items-end gap-2">
        <Field className="w-64 gap-1.5">
          <FieldLabel htmlFor="food-type-select">음식종류 지정</FieldLabel>
          <Select value={selected} onValueChange={setSelected} disabled={isPending || availableCategories.length === 0}>
            <SelectTrigger id="food-type-select" className="w-full">
              <SelectValue
                placeholder={availableCategories.length === 0 ? "지정 가능한 음식종류 없음" : "음식종류 선택"}
              />
            </SelectTrigger>
            <SelectContent>
              <SelectGroup>
                {availableCategories.map((category) => (
                  <SelectItem key={category.id} value={String(category.id)}>
                    {category.displayName}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
        </Field>
        <Button type="button" size="sm" onClick={handleAssign} disabled={isPending || !selected}>
          지정
        </Button>
      </div>
    </div>
  );
}

function OrderMethodsSection({ shopId }: TabProps) {
  const [assigned, setAssigned] = React.useState<OrderMethod[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [selected, setSelected] = React.useState<string>("");
  const [isPending, startTransition] = React.useTransition();

  const load = React.useCallback(() => {
    setIsLoading(true);
    setError(null);
    void fetchOrderMethodsAction(shopId).then((result) => {
      setIsLoading(false);
      if (result.success && result.data) {
        setAssigned(result.data);
      } else {
        setError(result.message ?? SHOP_MESSAGE.ORDER_METHODS_LOAD_FAILED);
      }
    });
  }, [shopId]);

  React.useEffect(() => {
    load();
  }, [load]);

  const availableOptions = ORDER_METHOD_OPTIONS.filter(
    (option) => !assigned.some((item) => item.orderMethod === option),
  );

  function handleAssign() {
    if (!selected) return;
    startTransition(async () => {
      const { success, message } = await createOrderMethodAction(shopId, {
        orderMethod: selected as (typeof ORDER_METHOD_OPTIONS)[number],
      });
      if (success) {
        toast.success(SHOP_MESSAGE.ORDER_METHOD_CREATE_SUCCESS);
        setSelected("");
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  }

  function handleUnassign(orderMethod: string) {
    startTransition(async () => {
      const { success, message } = await deleteOrderMethodAction(shopId, orderMethod);
      if (success) {
        toast.success(SHOP_MESSAGE.ORDER_METHOD_DELETE_SUCCESS);
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <div className="space-y-3">
      <h4 className="font-medium text-sm">주문수단</h4>
      {error ? (
        <p className="text-destructive text-sm">{error}</p>
      ) : isLoading ? (
        <Skeleton className="h-16 w-full" />
      ) : assigned.length ? (
        <ul className="flex flex-wrap gap-2">
          {assigned.map((item) => (
            <li key={item.orderMethod} className="flex items-center gap-2 rounded-md border px-3 py-1.5 text-sm">
              <span>{item.displayName}</span>
              <button
                type="button"
                className="text-destructive text-xs disabled:opacity-50"
                disabled={isPending}
                onClick={() => handleUnassign(item.orderMethod)}
              >
                해제
              </button>
            </li>
          ))}
        </ul>
      ) : (
        <p className="text-muted-foreground text-sm">지정된 주문수단이 없습니다.</p>
      )}

      <div className="flex items-end gap-2">
        <Field className="w-64 gap-1.5">
          <FieldLabel htmlFor="order-method-select">주문수단 지정</FieldLabel>
          <Select value={selected} onValueChange={setSelected} disabled={isPending || availableOptions.length === 0}>
            <SelectTrigger id="order-method-select" className="w-full">
              <SelectValue
                placeholder={availableOptions.length === 0 ? "지정 가능한 주문수단 없음" : "주문수단 선택"}
              />
            </SelectTrigger>
            <SelectContent>
              <SelectGroup>
                {availableOptions.map((option) => (
                  <SelectItem key={option} value={option}>
                    {ORDER_METHOD_LABEL[option]}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
        </Field>
        <Button type="button" size="sm" onClick={handleAssign} disabled={isPending || !selected}>
          지정
        </Button>
      </div>
    </div>
  );
}

export function ClassificationTab({ shopId }: TabProps) {
  return (
    <div className="space-y-6">
      <AmenitiesSection shopId={shopId} />
      <Separator />
      <FoodTypesSection shopId={shopId} />
      <Separator />
      <OrderMethodsSection shopId={shopId} />
    </div>
  );
}
