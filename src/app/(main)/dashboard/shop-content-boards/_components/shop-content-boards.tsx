"use client";
"use no memo";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { getCoreRowModel, type PaginationState, useReactTable } from "@tanstack/react-table";
import { Search, X } from "lucide-react";

import type { ApiPagination } from "@/api/shared/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { CONTENT_BOARD_CONTENT_TYPE_LABEL, CONTENT_BOARD_CONTENT_TYPE_OPTIONS } from "@/feature/shop/constants";
import type { ContentBoard } from "@/feature/shop/domain";
import { CONTENT_BOARD_PAGE_COPY } from "@/feature/shop/message";

import { ContentBoardHideDialog } from "./content-board-hide-dialog";
import { DeleteContentBoardDialog } from "./delete-content-board-dialog";
import { type ShopContentBoardsTableMeta, shopContentBoardsColumns } from "./shop-content-boards-columns";
import { ShopContentBoardsTable } from "./shop-content-boards-table";

interface Props {
  contentBoards: ContentBoard[];
  pagination: ApiPagination;
  initialShopId?: number;
  initialHidden?: boolean;
  initialContentType?: ContentBoard["contentType"];
}

export function ShopContentBoards({
  contentBoards,
  pagination,
  initialShopId,
  initialHidden,
  initialContentType,
}: Props) {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [hideTarget, setHideTarget] = React.useState<ContentBoard | null>(null);
  const [deleteTarget, setDeleteTarget] = React.useState<ContentBoard | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const [shopIdInput, setShopIdInput] = React.useState(initialShopId === undefined ? "" : String(initialShopId));
  const [hiddenInput, setHiddenInput] = React.useState(initialHidden === undefined ? "all" : String(initialHidden));
  const [contentTypeInput, setContentTypeInput] = React.useState(initialContentType ?? "all");

  function pushParams(next: { page?: number; size?: number; shopId?: string; hidden?: string; contentType?: string }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.page !== undefined) params.set("page", String(next.page));
    if (next.size !== undefined) params.set("size", String(next.size));
    for (const key of ["shopId", "hidden", "contentType"] as const) {
      if (next[key] === undefined) continue;
      const v = next[key];
      if (!v || v === "all") params.delete(key);
      else params.set(key, v);
    }
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  function handleSearch(override?: { hidden?: string; contentType?: string }) {
    pushParams({
      page: 0,
      shopId: shopIdInput,
      hidden: override?.hidden ?? hiddenInput,
      contentType: override?.contentType ?? contentTypeInput,
    });
  }

  function handleReset() {
    setShopIdInput("");
    setHiddenInput("all");
    setContentTypeInput("all");
    pushParams({ page: 0, shopId: "", hidden: "all", contentType: "all" });
  }

  function handleRefresh() {
    router.refresh();
  }

  const table = useReactTable({
    data: contentBoards,
    columns: shopContentBoardsColumns,
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
      onToggleHidden: (contentBoard) => setHideTarget(contentBoard),
      onDelete: (contentBoard) => setDeleteTarget(contentBoard),
    } satisfies ShopContentBoardsTableMeta,
  });

  return (
    <Card>
      <CardHeader className="border-b">
        <CardTitle className="text-xl leading-none">{CONTENT_BOARD_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{CONTENT_BOARD_PAGE_COPY.DESCRIPTION}</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4 px-0">
        <form
          className="flex flex-wrap items-center gap-2 px-4 pt-2"
          onSubmit={(e) => {
            e.preventDefault();
            handleSearch();
          }}
        >
          <Input
            className="w-28"
            type="number"
            min={1}
            placeholder="가게 ID"
            value={shopIdInput}
            onChange={(e) => setShopIdInput(e.target.value)}
            disabled={isPending}
          />
          <Select
            value={hiddenInput}
            onValueChange={(value) => {
              setHiddenInput(value);
              handleSearch({ hidden: value });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">노출 상태:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value="all">전체</SelectItem>
                <SelectItem value="true">숨김</SelectItem>
                <SelectItem value="false">노출</SelectItem>
              </SelectGroup>
            </SelectContent>
          </Select>
          <Select
            value={contentTypeInput}
            onValueChange={(value) => {
              setContentTypeInput(value as typeof contentTypeInput);
              handleSearch({ contentType: value });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">유형:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value="all">전체</SelectItem>
                {CONTENT_BOARD_CONTENT_TYPE_OPTIONS.map((option) => (
                  <SelectItem key={option} value={option}>
                    {CONTENT_BOARD_CONTENT_TYPE_LABEL[option]}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
          <Button type="submit" size="sm" disabled={isPending}>
            <Search className="size-4" />
            검색
          </Button>
          <Button type="button" size="sm" variant="destructive" onClick={handleReset} disabled={isPending}>
            <X className="size-4" />
            초기화
          </Button>
        </form>
        <ShopContentBoardsTable table={table} isPending={isPending} />
      </CardContent>
      <ContentBoardHideDialog
        contentBoard={hideTarget}
        onOpenChange={(open) => !open && setHideTarget(null)}
        onSuccess={handleRefresh}
      />
      <DeleteContentBoardDialog
        contentBoard={deleteTarget}
        onOpenChange={(open) => !open && setDeleteTarget(null)}
        onSuccess={handleRefresh}
      />
    </Card>
  );
}
