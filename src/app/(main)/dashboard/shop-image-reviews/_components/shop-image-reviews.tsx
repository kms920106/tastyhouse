"use client";
"use no memo";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { getCoreRowModel, type PaginationState, useReactTable } from "@tanstack/react-table";

import type { ApiPagination } from "@/api/shared/types";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import {
  SHOP_IMAGE_CHANGE_STATUS_LABEL,
  SHOP_IMAGE_CHANGE_STATUS_OPTIONS,
  SHOP_IMAGE_TYPE_LABEL,
  SHOP_IMAGE_TYPE_OPTIONS,
} from "@/feature/shop/constants";
import type { ShopImageChangeRequest } from "@/feature/shop/domain";
import { SHOP_IMAGE_REVIEW_PAGE_COPY } from "@/feature/shop/message";

import { ImageChangeApproveDialog } from "./image-change-approve-dialog";
import { ImageChangeRejectDialog } from "./image-change-reject-dialog";
import { type ShopImageReviewsTableMeta, shopImageReviewsColumns } from "./shop-image-reviews-columns";
import { ShopImageReviewsTable } from "./shop-image-reviews-table";

interface Props {
  requests: ShopImageChangeRequest[];
  pagination: ApiPagination;
  initialStatus?: ShopImageChangeRequest["status"];
  initialImageType?: ShopImageChangeRequest["imageType"];
}

export function ShopImageReviews({ requests, pagination, initialStatus, initialImageType }: Props) {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [approveTarget, setApproveTarget] = React.useState<ShopImageChangeRequest | null>(null);
  const [rejectTarget, setRejectTarget] = React.useState<ShopImageChangeRequest | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const [statusInput, setStatusInput] = React.useState(initialStatus ?? "all");
  const [imageTypeInput, setImageTypeInput] = React.useState(initialImageType ?? "all");

  function pushParams(next: { page?: number; size?: number; status?: string; imageType?: string }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.page !== undefined) params.set("page", String(next.page));
    if (next.size !== undefined) params.set("size", String(next.size));
    for (const key of ["status", "imageType"] as const) {
      if (next[key] === undefined) continue;
      const v = next[key];
      if (!v || v === "all") params.delete(key);
      else params.set(key, v);
    }
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  function handleRefresh() {
    router.refresh();
  }

  const table = useReactTable({
    data: requests,
    columns: shopImageReviewsColumns,
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
      onApprove: (request) => setApproveTarget(request),
      onReject: (request) => setRejectTarget(request),
    } satisfies ShopImageReviewsTableMeta,
  });

  return (
    <Card>
      <CardHeader className="border-b">
        <CardTitle className="text-xl leading-none">{SHOP_IMAGE_REVIEW_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{SHOP_IMAGE_REVIEW_PAGE_COPY.DESCRIPTION}</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4 px-0">
        <div className="flex flex-wrap items-center gap-2 px-4 pt-2">
          <Select
            value={statusInput}
            onValueChange={(value) => {
              setStatusInput(value as typeof statusInput);
              pushParams({ page: 0, status: value });
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
                {SHOP_IMAGE_CHANGE_STATUS_OPTIONS.map((option) => (
                  <SelectItem key={option} value={option}>
                    {SHOP_IMAGE_CHANGE_STATUS_LABEL[option]}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
          <Select
            value={imageTypeInput}
            onValueChange={(value) => {
              setImageTypeInput(value as typeof imageTypeInput);
              pushParams({ page: 0, imageType: value });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">이미지 유형:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value="all">전체</SelectItem>
                {SHOP_IMAGE_TYPE_OPTIONS.map((option) => (
                  <SelectItem key={option} value={option}>
                    {SHOP_IMAGE_TYPE_LABEL[option]}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
        </div>
        <ShopImageReviewsTable table={table} isPending={isPending} />
      </CardContent>
      <ImageChangeApproveDialog
        request={approveTarget}
        onOpenChange={(open) => !open && setApproveTarget(null)}
        onSuccess={handleRefresh}
      />
      <ImageChangeRejectDialog
        request={rejectTarget}
        onOpenChange={(open) => !open && setRejectTarget(null)}
        onSuccess={handleRefresh}
      />
    </Card>
  );
}
