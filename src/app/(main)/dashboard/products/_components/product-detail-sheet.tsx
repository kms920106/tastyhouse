"use client";

import * as React from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
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
import { fetchProductAction } from "@/feature/product/actions";
import type { ProductDetail } from "@/feature/product/domain";
import { formatDiscountRate, formatPrice, formatSpiciness } from "@/feature/product/format";
import { PRODUCT_MESSAGE } from "@/feature/product/message";
import { formatDateTime } from "@/lib/date";

interface ProductDetailSheetProps {
  /** 조회할 상품 ID. null 이면 닫힌 상태. */
  productId: number | null;
  onOpenChange: (open: boolean) => void;
}

export function ProductDetailSheet({ productId, onOpenChange }: ProductDetailSheetProps) {
  const [detail, setDetail] = React.useState<ProductDetail | null>(null);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    if (productId == null) {
      return;
    }

    let active = true;
    setIsLoading(true);
    setError(null);
    setDetail(null);

    void fetchProductAction(productId).then((result) => {
      const { success, message, data } = result;

      if (!active) return;
      if (success && data) {
        setDetail(data);
      } else {
        setError(message ?? PRODUCT_MESSAGE.DETAIL_LOAD_FAILED);
      }
      setIsLoading(false);
    });

    return () => {
      active = false;
    };
  }, [productId]);

  return (
    <Sheet open={productId != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>상품 상세</SheetTitle>
          <SheetDescription>상품의 상세 정보를 확인합니다.</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-5 overflow-y-auto px-4">
          {isLoading ? (
            <div className="space-y-3">
              <Skeleton className="h-6 w-3/4" />
              <Skeleton className="h-4 w-1/3" />
              <Skeleton className="h-32 w-full" />
            </div>
          ) : error ? (
            <p className="text-destructive text-sm">{error}</p>
          ) : detail ? (
            <>
              <div className="flex items-start justify-between gap-3">
                <h3 className="font-semibold text-lg leading-snug">{detail.name}</h3>
                <div className="flex flex-wrap justify-end gap-1">
                  {detail.representative ? <Badge variant="outline">대표</Badge> : null}
                  <Badge variant={detail.soldOut ? "destructive" : "secondary"}>
                    {detail.soldOut ? "품절" : "판매중"}
                  </Badge>
                  <Badge variant={detail.visible ? "default" : "secondary"}>{detail.visible ? "노출" : "미노출"}</Badge>
                </div>
              </div>

              {detail.description ? (
                <p className="whitespace-pre-wrap break-words text-muted-foreground text-sm leading-relaxed">
                  {detail.description}
                </p>
              ) : null}

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">ID</dt>
                <dd className="tabular-nums">{detail.id}</dd>
                <dt className="text-muted-foreground">매장 ID</dt>
                <dd className="tabular-nums">{detail.shopId}</dd>
                <dt className="text-muted-foreground">카테고리 ID</dt>
                <dd className="tabular-nums">{detail.productCategoryId ?? "-"}</dd>
              </dl>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">정가</dt>
                <dd className="tabular-nums">{formatPrice(detail.originalPrice)}</dd>
                <dt className="text-muted-foreground">할인가</dt>
                <dd className="tabular-nums">{formatPrice(detail.discountPrice)}</dd>
                <dt className="text-muted-foreground">할인율</dt>
                <dd className="tabular-nums">{formatDiscountRate(detail.discountRate)}</dd>
                <dt className="text-muted-foreground">맵기</dt>
                <dd className="tabular-nums">{formatSpiciness(detail.spiciness)}</dd>
                <dt className="text-muted-foreground">평점</dt>
                <dd className="tabular-nums">{detail.rating ?? "-"}</dd>
                <dt className="text-muted-foreground">리뷰 수</dt>
                <dd className="tabular-nums">{detail.reviewCount ?? "-"}</dd>
                <dt className="text-muted-foreground">정렬 순서</dt>
                <dd className="tabular-nums">{detail.sort}</dd>
              </dl>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">생성일시</dt>
                <dd className="tabular-nums">{formatDateTime(detail.createdAt)}</dd>
                <dt className="text-muted-foreground">수정일시</dt>
                <dd className="tabular-nums">{formatDateTime(detail.updatedAt)}</dd>
              </dl>
            </>
          ) : null}
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
