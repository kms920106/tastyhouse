"use client";
"use no memo";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { getCoreRowModel, type PaginationState, useReactTable } from "@tanstack/react-table";

import type { ApiPagination } from "@/api/shared/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import {
  DELIVERY_AREA_ADJUSTMENT_STATUS_LABEL,
  DELIVERY_AREA_ADJUSTMENT_STATUS_OPTIONS,
} from "@/feature/shop/constants";
import type { DeliveryAreaAdjustmentListItem } from "@/feature/shop/domain";
import { DELIVERY_AREA_ADJUSTMENT_PAGE_COPY } from "@/feature/shop/message";

import { AdjustmentDetailSheet } from "./adjustment-detail-sheet";
import { AdjustmentRejectDialog } from "./adjustment-reject-dialog";
import { AdjustmentStatusDialog } from "./adjustment-status-dialog";
import {
  type ShopDeliveryAreaAdjustmentsTableMeta,
  shopDeliveryAreaAdjustmentsColumns,
} from "./shop-delivery-area-adjustments-columns";
import { ShopDeliveryAreaAdjustmentsTable } from "./shop-delivery-area-adjustments-table";

interface Props {
  requests: DeliveryAreaAdjustmentListItem[];
  pagination: ApiPagination;
  initialStatus?: DeliveryAreaAdjustmentListItem["status"];
  initialShopId?: number;
}

export function ShopDeliveryAreaAdjustments({ requests, pagination, initialStatus, initialShopId }: Props) {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [detailTarget, setDetailTarget] = React.useState<DeliveryAreaAdjustmentListItem | null>(null);
  const [statusTarget, setStatusTarget] = React.useState<DeliveryAreaAdjustmentListItem | null>(null);
  const [rejectTarget, setRejectTarget] = React.useState<DeliveryAreaAdjustmentListItem | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const [statusInput, setStatusInput] = React.useState<string>(initialStatus ?? "all");
  const [shopIdInput, setShopIdInput] = React.useState<string>(initialShopId ? String(initialShopId) : "");

  function pushParams(next: { page?: number; size?: number; status?: string; shopId?: string }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.page !== undefined) params.set("page", String(next.page));
    if (next.size !== undefined) params.set("size", String(next.size));
    if (next.status !== undefined) {
      if (!next.status || next.status === "all") params.delete("status");
      else params.set("status", next.status);
    }
    if (next.shopId !== undefined) {
      const trimmed = next.shopId.trim();
      if (!trimmed) params.delete("shopId");
      else params.set("shopId", trimmed);
    }
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  function handleSearch(override?: { status?: string }) {
    pushParams({ page: 0, status: override?.status ?? statusInput, shopId: shopIdInput });
  }

  function handleRefresh() {
    router.refresh();
  }

  const table = useReactTable({
    data: requests,
    columns: shopDeliveryAreaAdjustmentsColumns,
    state: {
      pagination: { pageIndex: pagination.page, pageSize: pagination.size },
    },
    manualPagination: true,
    pageCount: Math.max(pagination.totalPages, 1),
    getRowId: (row) => String(row.id),
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
      onView: (request) => setDetailTarget(request),
      onChangeStatus: (request) => setStatusTarget(request),
      onReject: (request) => setRejectTarget(request),
    } satisfies ShopDeliveryAreaAdjustmentsTableMeta,
  });

  return (
    <Card>
      <CardHeader className="border-b">
        <CardTitle className="text-xl leading-none">{DELIVERY_AREA_ADJUSTMENT_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">
          {DELIVERY_AREA_ADJUSTMENT_PAGE_COPY.DESCRIPTION}
        </CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4 px-0">
        <form
          className="flex flex-wrap items-center gap-2 px-4 pt-2"
          onSubmit={(event) => {
            event.preventDefault();
            handleSearch();
          }}
        >
          <Input
            className="w-32"
            inputMode="numeric"
            placeholder="가게 ID"
            value={shopIdInput}
            onChange={(event) => setShopIdInput(event.target.value)}
            disabled={isPending}
          />
          <Select
            value={statusInput}
            onValueChange={(value) => {
              setStatusInput(value);
              handleSearch({ status: value });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">상태:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value="all">전체</SelectItem>
                {DELIVERY_AREA_ADJUSTMENT_STATUS_OPTIONS.map((option) => (
                  <SelectItem key={option} value={option}>
                    {DELIVERY_AREA_ADJUSTMENT_STATUS_LABEL[option]}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
          <Button type="submit" size="sm" disabled={isPending}>
            검색
          </Button>
        </form>
        <ShopDeliveryAreaAdjustmentsTable table={table} isPending={isPending} />
      </CardContent>
      <AdjustmentDetailSheet
        requestId={detailTarget?.id ?? null}
        onOpenChange={(open) => !open && setDetailTarget(null)}
      />
      <AdjustmentStatusDialog
        request={statusTarget}
        onOpenChange={(open) => !open && setStatusTarget(null)}
        onSuccess={handleRefresh}
      />
      <AdjustmentRejectDialog
        request={rejectTarget}
        onOpenChange={(open) => !open && setRejectTarget(null)}
        onSuccess={handleRefresh}
      />
    </Card>
  );
}
