"use client";

import * as React from "react";

import { toast } from "sonner";

import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Sheet, SheetContent, SheetDescription, SheetFooter, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { Skeleton } from "@/components/ui/skeleton";
import { Switch } from "@/components/ui/switch";
import {
  loadProductShopLinksAction,
  loadShopCategoriesAction,
  updateProductShopLinksAction,
} from "@/feature/product/actions";
import type { MenuCategory, ProductShopLink } from "@/feature/product/domain";
import { PRODUCT_SHOP_LINK_COPY, PRODUCT_SHOP_LINK_MESSAGE } from "@/feature/product/message";

interface MenuShopLinkSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  productId: number;
  /** 권한 판정 기준 가게(현재 보고 있는 가게). 연결 대상인 `link.shopId` 와 다르다 */
  shopId: number;
  /** 현재 가게의 메뉴그룹. 다른 가게를 토글로 켜면 그 가게의 메뉴그룹은 `loadShopCategoriesAction`으로 따로 불러온다 */
  categories: MenuCategory[];
  /** 저장이 끝나면 부모가 요약 줄을 갱신하도록 알린다 */
  onSaved: (links: ProductShopLink[]) => void;
}

/** Select 는 문자열만 다룬다. 미선택은 빈 문자열이다 */
type CategoryValueByShopId = Record<number, string>;

/**
 * '이 메뉴를 판매하는 가게' 변경 시트.
 *
 * 점주 소유 **전체 가게**를 토글로 나열하고, 켠 가게마다 메뉴그룹을 고르게 한다.
 * 저장은 전체 교체(PUT)라 토글이 꺼진 가게는 연결이 해제된다.
 *
 * **마지막 1개는 끌 수 없다** — 서버도 `..._LAST_CANNOT_UNLINK` 로 막지만, 저장을 눌러
 * 왕복한 뒤 알려주면 점주가 무엇을 되돌려야 할지 헷갈린다. 토글 자체를 비활성한다.
 */
export function MenuShopLinkSheet({
  open,
  onOpenChange,
  productId,
  shopId,
  categories,
  onSaved,
}: MenuShopLinkSheetProps) {
  const [isLoading, setIsLoading] = React.useState(false);
  const [isPending, startTransition] = React.useTransition();
  const [shopLinks, setShopLinks] = React.useState<ProductShopLink[]>([]);
  const [linkedShopIds, setLinkedShopIds] = React.useState<ReadonlySet<number>>(() => new Set());
  const [categoryValues, setCategoryValues] = React.useState<CategoryValueByShopId>({});
  const [categoriesByShopId, setCategoriesByShopId] = React.useState<Record<number, MenuCategory[]>>({});
  const [loadingCategoryShopIds, setLoadingCategoryShopIds] = React.useState<ReadonlySet<number>>(() => new Set());

  React.useEffect(() => {
    if (!open) return;

    let alive = true;
    setIsLoading(true);
    setCategoriesByShopId({ [shopId]: categories });
    setLoadingCategoryShopIds(new Set());

    void loadProductShopLinksAction(productId, shopId).then(({ success, message, data }) => {
      if (!alive) return;
      setIsLoading(false);

      if (!success || !data) {
        toast.error(message ?? PRODUCT_SHOP_LINK_MESSAGE.LOAD_FAILED);
        return;
      }

      setShopLinks(data);
      setLinkedShopIds(new Set(data.filter((link) => link.linked).map((link) => link.shopId)));
      setCategoryValues(
        Object.fromEntries(
          data.map((link) => [link.shopId, link.productCategoryId === null ? "" : String(link.productCategoryId)]),
        ),
      );
      // 이미 연결된 다른 가게도 그룹을 바꿀 수 있어야 하므로 미리 불러온다.
      data
        .filter((link) => link.linked && link.shopId !== shopId)
        .forEach((link) => void ensureShopCategoriesLoaded(link.shopId));
    });

    return () => {
      alive = false;
    };
    // biome-ignore lint/correctness/useExhaustiveDependencies: categories/ensureShopCategoriesLoaded는 초기화 시점 값만 쓴다
  }, [open, productId, shopId]);

  /** 대상 가게의 메뉴그룹을 아직 모르면 서버에서 불러와 캐시한다(가게당 한 번만) */
  function ensureShopCategoriesLoaded(targetShopId: number) {
    if (categoriesByShopId[targetShopId] !== undefined || loadingCategoryShopIds.has(targetShopId)) {
      return;
    }

    setLoadingCategoryShopIds((prev) => new Set(prev).add(targetShopId));

    return loadShopCategoriesAction(targetShopId).then(({ success, message, data }) => {
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

  /** 마지막 하나 남은 연결은 끌 수 없다 */
  const isLastLinked = linkedShopIds.size <= 1;

  function toggleShop(targetShopId: number, next: boolean) {
    if (!next && isLastLinked) {
      toast.error(PRODUCT_SHOP_LINK_MESSAGE.LAST_CANNOT_UNLINK);
      return;
    }

    if (next) {
      void ensureShopCategoriesLoaded(targetShopId);
    }

    setLinkedShopIds((prev) => {
      const updated = new Set(prev);
      if (next) updated.add(targetShopId);
      else updated.delete(targetShopId);
      return updated;
    });
  }

  function handleSave() {
    const links = shopLinks
      .filter((link) => linkedShopIds.has(link.shopId))
      .map((link) => ({
        shopId: link.shopId,
        productCategoryId: Number(categoryValues[link.shopId] ?? ""),
      }));

    // 메뉴그룹 미선택은 서버도 거절하지만, 어느 가게가 비었는지는 화면만 알고 있다.
    if (links.some((link) => !Number.isInteger(link.productCategoryId) || link.productCategoryId <= 0)) {
      toast.error(PRODUCT_SHOP_LINK_MESSAGE.CATEGORY_REQUIRED);
      return;
    }

    startTransition(async () => {
      const { success, message } = await updateProductShopLinksAction(productId, shopId, links);
      if (!success) {
        // 연결 해제로 그 가게 메뉴판이 비는 경우(`PRODUCT_LAST_VISIBLE_CANNOT_HIDE`)도 여기로 온다.
        // 서버가 한국어 문구를 내려주므로 그대로 노출한다.
        toast.error(message ?? PRODUCT_SHOP_LINK_MESSAGE.SAVE_FAILED);
        return;
      }

      toast.success(PRODUCT_SHOP_LINK_MESSAGE.SAVE_SUCCESS);
      onSaved(
        shopLinks.map((link) => {
          const linked = linkedShopIds.has(link.shopId);
          const categoryId = Number(categoryValues[link.shopId] ?? "");
          return {
            ...link,
            linked,
            productCategoryId: linked ? categoryId : null,
            productCategoryName: linked
              ? ((categoriesByShopId[link.shopId] ?? []).find((category) => category.id === categoryId)?.name ??
                link.productCategoryName)
              : null,
          };
        }),
      );
      onOpenChange(false);
    });
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col overflow-y-auto sm:max-w-lg">
        <SheetHeader>
          <SheetTitle>{PRODUCT_SHOP_LINK_COPY.SHEET_TITLE}</SheetTitle>
          <SheetDescription>{PRODUCT_SHOP_LINK_COPY.GUIDE}</SheetDescription>
        </SheetHeader>

        <div className="flex flex-1 flex-col gap-4 px-4 pb-4">
          {/* 연결한 뒤에야 알면 가격을 되돌릴 방법이 없어 미리 알린다 */}
          <Alert>
            <AlertDescription>{PRODUCT_SHOP_LINK_COPY.PRICE_SHARED_WARNING}</AlertDescription>
          </Alert>

          {isLoading ? (
            <div className="flex flex-col gap-3">
              <Skeleton className="h-16 w-full" />
              <Skeleton className="h-16 w-full" />
            </div>
          ) : shopLinks.length === 0 ? (
            <p className="text-muted-foreground py-10 text-center text-sm">{PRODUCT_SHOP_LINK_COPY.EMPTY_SHOPS}</p>
          ) : (
            <ul className="flex flex-col">
              {shopLinks.map((link) => {
                const linked = linkedShopIds.has(link.shopId);
                const shopCategories = categoriesByShopId[link.shopId];
                const isLoadingCategories = loadingCategoryShopIds.has(link.shopId);
                const canPickCategory = shopCategories !== undefined && shopCategories.length > 0;

                return (
                  <li key={link.shopId} className="flex flex-col gap-2 border-b py-4 last:border-b-0">
                    <div className="flex items-center justify-between gap-3">
                      <span className="truncate text-sm font-medium">{link.shopName}</span>
                      <Switch
                        checked={linked}
                        // 마지막 연결은 끌 수 없다 — 켜는 방향은 항상 허용한다.
                        disabled={isPending || (linked && isLastLinked)}
                        aria-label={link.shopName}
                        onCheckedChange={(next) => toggleShop(link.shopId, next)}
                      />
                    </div>

                    {linked &&
                      (canPickCategory ? (
                        <Select
                          value={categoryValues[link.shopId] ?? ""}
                          disabled={isPending}
                          onValueChange={(value) => setCategoryValues((prev) => ({ ...prev, [link.shopId]: value }))}
                        >
                          <SelectTrigger className="w-full">
                            <SelectValue placeholder={PRODUCT_SHOP_LINK_COPY.CATEGORY_PLACEHOLDER} />
                          </SelectTrigger>
                          <SelectContent>
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
                        // 조회 결과 메뉴그룹이 0개인 가게 — 저장 단계에서 막힌다.
                        <span className="text-muted-foreground text-xs">
                          {PRODUCT_SHOP_LINK_COPY.EMPTY_CATEGORIES}
                        </span>
                      ))}
                  </li>
                );
              })}
            </ul>
          )}
        </div>

        <SheetFooter className="flex-row gap-2">
          <Button type="button" variant="outline" className="flex-1" onClick={() => onOpenChange(false)}>
            {PRODUCT_SHOP_LINK_COPY.ACTION_CANCEL}
          </Button>
          <Button
            type="button"
            className="flex-1"
            disabled={isPending || isLoading || shopLinks.length === 0}
            onClick={handleSave}
          >
            {PRODUCT_SHOP_LINK_COPY.ACTION_SAVE}
          </Button>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
