"use client";

import * as React from "react";

import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Sheet, SheetContent, SheetDescription, SheetFooter, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { Skeleton } from "@/components/ui/skeleton";
import { importProductToShopAction, loadImportableProductsAction } from "@/feature/product/actions";
import type { ImportableProduct } from "@/feature/product/domain";
import { formatPrice } from "@/feature/product/format";
import { PRODUCT_IMPORT_COPY, PRODUCT_SHOP_LINK_MESSAGE } from "@/feature/product/message";
import type { ShopSummary } from "@/feature/shop/domain";

interface MenuImportSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** 불러올 대상 가게(현재 보고 있는 가게) */
  shopId: number;
  /** 이 메뉴그룹에 추가된다 */
  categoryId: number;
  categoryName: string;
  /** 점주 소유 가게 전체. 여기서 대상 가게를 뺀 나머지가 후보 출처다 */
  shops: ShopSummary[];
  onImported: () => void;
}

/**
 * 메뉴 불러오기(가게 기준).
 *
 * 메뉴 상세의 연결 변경(`menu-shop-link-sheet`)이 **메뉴 하나의 소속을 통째로 바꾸는** 것과
 * 달리, 이쪽은 이 가게 메뉴판에 남의 메뉴를 끌어온다. 다른 가게의 기존 연결은 건드리지 않는다.
 *
 * 여러 건을 고를 수 있어 **부분 실패가 날 수 있다** — 성공·실패 건수를 나눠 알리고, 한 건이라도
 * 성공하면 목록을 갱신한다. 전부 실패로 뭉뚱그리면 점주가 무엇을 다시 시도할지 알 수 없다.
 */
export function MenuImportSheet({
  open,
  onOpenChange,
  shopId,
  categoryId,
  categoryName,
  shops,
  onImported,
}: MenuImportSheetProps) {
  const [isLoading, setIsLoading] = React.useState(false);
  const [isPending, startTransition] = React.useTransition();
  const [products, setProducts] = React.useState<ImportableProduct[]>([]);
  const [selectedIds, setSelectedIds] = React.useState<ReadonlySet<number>>(() => new Set());
  const [keyword, setKeyword] = React.useState("");

  const shopNameById = React.useMemo(() => new Map(shops.map((shop) => [shop.id, shop.name])), [shops]);

  React.useEffect(() => {
    if (!open) return;

    let alive = true;
    setIsLoading(true);
    setSelectedIds(new Set());
    setKeyword("");

    const sourceShopIds = shops.map((shop) => shop.id);
    void loadImportableProductsAction(shopId, sourceShopIds).then(({ success, message, data }) => {
      if (!alive) return;
      setIsLoading(false);

      if (!success || !data) {
        toast.error(message ?? PRODUCT_IMPORT_COPY.LOAD_FAILED);
        return;
      }
      setProducts(data);
    });

    return () => {
      alive = false;
    };
  }, [open, shopId, shops]);

  const visibleProducts = React.useMemo(() => {
    const trimmed = keyword.trim();
    if (trimmed === "") return products;
    return products.filter((product) => product.name.includes(trimmed));
  }, [products, keyword]);

  function toggle(productId: number, checked: boolean) {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (checked) next.add(productId);
      else next.delete(productId);
      return next;
    });
  }

  function handleImport() {
    const targets = [...selectedIds];
    if (targets.length === 0) return;

    startTransition(async () => {
      const results = await Promise.all(
        targets.map((productId) => importProductToShopAction(productId, shopId, categoryId)),
      );

      const failed = results.filter((result) => !result.success);
      if (failed.length === targets.length) {
        toast.error(failed[0]?.message ?? PRODUCT_SHOP_LINK_MESSAGE.IMPORT_FAILED);
        return;
      }

      // 부분 성공. 실패분은 목록에 남겨 다시 시도할 수 있게 한다.
      if (failed.length > 0) toast.error(failed[0]?.message ?? PRODUCT_SHOP_LINK_MESSAGE.IMPORT_FAILED);
      else toast.success(PRODUCT_SHOP_LINK_MESSAGE.IMPORT_SUCCESS);

      onImported();
      onOpenChange(false);
    });
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col overflow-y-auto sm:max-w-lg">
        <SheetHeader>
          <SheetTitle>{PRODUCT_IMPORT_COPY.SHEET_TITLE}</SheetTitle>
          <SheetDescription>{`${PRODUCT_IMPORT_COPY.DESCRIPTION} (${categoryName})`}</SheetDescription>
        </SheetHeader>

        <div className="flex flex-1 flex-col gap-3 px-4 pb-4">
          <Input
            value={keyword}
            disabled={isLoading || isPending}
            placeholder={PRODUCT_IMPORT_COPY.SEARCH_PLACEHOLDER}
            onChange={(event) => setKeyword(event.target.value)}
          />

          {isLoading ? (
            <div className="flex flex-col gap-3">
              <Skeleton className="h-14 w-full" />
              <Skeleton className="h-14 w-full" />
              <Skeleton className="h-14 w-full" />
            </div>
          ) : visibleProducts.length === 0 ? (
            <p className="text-muted-foreground py-10 text-center text-sm">{PRODUCT_IMPORT_COPY.EMPTY}</p>
          ) : (
            <ul className="flex flex-col">
              {visibleProducts.map((product) => {
                const inputId = `menu-import-${product.productId}`;
                return (
                  <li key={product.productId} className="flex items-center gap-3 border-b py-3 last:border-b-0">
                    <Checkbox
                      id={inputId}
                      checked={selectedIds.has(product.productId)}
                      disabled={isPending}
                      onCheckedChange={(checked) => toggle(product.productId, checked === true)}
                    />
                    <label htmlFor={inputId} className="flex min-w-0 flex-1 flex-col gap-0.5">
                      <span className="truncate text-sm font-medium">{product.name}</span>
                      <span className="text-muted-foreground text-xs">
                        {`${shopNameById.get(product.shopId) ?? ""} · ${formatPrice(product.originalPrice)}`}
                      </span>
                    </label>
                  </li>
                );
              })}
            </ul>
          )}
        </div>

        <SheetFooter className="flex-row gap-2">
          <Button type="button" variant="outline" className="flex-1" onClick={() => onOpenChange(false)}>
            {PRODUCT_IMPORT_COPY.ACTION_CANCEL}
          </Button>
          <Button
            type="button"
            className="flex-1"
            disabled={isPending || selectedIds.size === 0}
            onClick={handleImport}
          >
            {`${PRODUCT_IMPORT_COPY.ACTION_IMPORT} (${PRODUCT_IMPORT_COPY.SELECTED_PREFIX}${selectedIds.size}${PRODUCT_IMPORT_COPY.SELECTED_SUFFIX})`}
          </Button>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
