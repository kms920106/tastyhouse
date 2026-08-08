"use client";
"use no memo";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { getCoreRowModel, type PaginationState, useReactTable } from "@tanstack/react-table";

import type { ApiPagination } from "@/api/shared/types";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import type { ShopRiderGuideListItem } from "@/feature/shop/domain";
import { SHOP_RIDER_GUIDE_ADMIN_COPY } from "@/feature/shop/message";

import { ShopRiderGuideDetailSheet } from "./shop-rider-guide-detail-sheet";
import { type ShopRiderGuidesTableMeta, shopRiderGuidesColumns } from "./shop-rider-guides-columns";
import { ShopRiderGuidesTable } from "./shop-rider-guides-table";

interface Props {
  riderGuides: ShopRiderGuideListItem[];
  pagination: ApiPagination;
  initialShopName?: string;
  initialHasVisitGuide: boolean;
}

export function ShopRiderGuides({ riderGuides, pagination, initialShopName, initialHasVisitGuide }: Props) {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [detailTarget, setDetailTarget] = React.useState<ShopRiderGuideListItem | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const [shopNameInput, setShopNameInput] = React.useState(initialShopName ?? "");
  const [hasVisitGuideInput, setHasVisitGuideInput] = React.useState(initialHasVisitGuide);

  function pushParams(next: { page?: number; size?: number; shopName?: string; hasVisitGuide?: boolean }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.page !== undefined) params.set("page", String(next.page));
    if (next.size !== undefined) params.set("size", String(next.size));
    if (next.shopName !== undefined) {
      if (next.shopName.length === 0) params.delete("shopName");
      else params.set("shopName", next.shopName);
    }
    if (next.hasVisitGuide !== undefined) {
      // 체크 해제는 '전체 조회'이므로 파라미터 자체를 지운다.
      if (next.hasVisitGuide) params.set("hasVisitGuide", "true");
      else params.delete("hasVisitGuide");
    }
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  function handleRefresh() {
    router.refresh();
  }

  const table = useReactTable({
    data: riderGuides,
    columns: shopRiderGuidesColumns,
    state: {
      pagination: { pageIndex: pagination.page, pageSize: pagination.size },
    },
    manualPagination: true,
    pageCount: Math.max(pagination.totalPages, 1),
    getRowId: (row) => String(row.shopId),
    autoResetPageIndex: false,
    getCoreRowModel: getCoreRowModel(),
    onPaginationChange: (updater) => {
      const previous: PaginationState = {
        pageIndex: pagination.page,
        pageSize: pagination.size,
      };
      const next = typeof updater === "function" ? updater(previous) : updater;
      if (next.pageSize !== previous.pageSize) {
        pushParams({ page: 0, size: next.pageSize });
      } else if (next.pageIndex !== previous.pageIndex) {
        pushParams({ page: next.pageIndex });
      }
    },
    meta: {
      totalElements: pagination.totalElements,
      onSelect: (riderGuide) => setDetailTarget(riderGuide),
    } satisfies ShopRiderGuidesTableMeta,
  });

  return (
    <Card>
      <CardHeader className="border-b">
        <CardTitle className="text-xl leading-none">{SHOP_RIDER_GUIDE_ADMIN_COPY.PAGE_TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">
          {SHOP_RIDER_GUIDE_ADMIN_COPY.PAGE_DESCRIPTION}
        </CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4 px-0">
        <div className="flex flex-wrap items-center gap-3 px-4 pt-2">
          <form
            className="flex items-center gap-2"
            onSubmit={(event) => {
              event.preventDefault();
              pushParams({ page: 0, shopName: shopNameInput.trim() });
            }}
          >
            <Input
              id="shop-rider-guides-shop-name"
              className="h-8 w-56"
              placeholder={SHOP_RIDER_GUIDE_ADMIN_COPY.SEARCH_PLACEHOLDER}
              value={shopNameInput}
              onChange={(event) => setShopNameInput(event.target.value)}
              disabled={isPending}
            />
          </form>

          <div className="flex items-center gap-2">
            <Checkbox
              id="shop-rider-guides-has-visit-guide"
              checked={hasVisitGuideInput}
              disabled={isPending}
              onCheckedChange={(checked) => {
                const next = checked === true;
                setHasVisitGuideInput(next);
                pushParams({ page: 0, hasVisitGuide: next });
              }}
            />
            <Label htmlFor="shop-rider-guides-has-visit-guide" className="font-normal text-sm">
              {SHOP_RIDER_GUIDE_ADMIN_COPY.FILTER_HAS_VISIT_GUIDE}
            </Label>
          </div>
        </div>
        <ShopRiderGuidesTable table={table} isPending={isPending} />
      </CardContent>

      <ShopRiderGuideDetailSheet
        shopId={detailTarget?.shopId ?? null}
        onOpenChange={(open) => !open && setDetailTarget(null)}
        onSuccess={handleRefresh}
      />
    </Card>
  );
}
