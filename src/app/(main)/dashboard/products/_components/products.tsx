"use client";
"use no memo";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { getCoreRowModel, type PaginationState, useReactTable } from "@tanstack/react-table";
import { Layers, Plus, Search, X } from "lucide-react";

import type { ApiPagination } from "@/api/shared/types";
import { Button } from "@/components/ui/button";
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import type { ProductListItem } from "@/feature/product/domain";
import { PRODUCT_PAGE_COPY } from "@/feature/product/message";

import { DeactivateProductDialog } from "./deactivate-product-dialog";
import { ProductCategorySheet } from "./product-category-sheet";
import { ProductDetailSheet } from "./product-detail-sheet";
import { ProductFormSheet } from "./product-form-sheet";
import { ProductImagesSheet } from "./product-images-sheet";
import { ProductOptionsSheet } from "./product-options-sheet";
import { type ProductsTableMeta, productsColumns } from "./products-columns";
import { ProductsTable } from "./products-table";
import { SoldOutProductDialog } from "./sold-out-product-dialog";

interface Props {
  products: ProductListItem[];
  pagination: ApiPagination;
  initialShopId?: number;
  initialName?: string;
  initialVisible?: boolean;
  initialSoldOut?: boolean;
}

export function Products({ products, pagination, initialShopId, initialName, initialVisible, initialSoldOut }: Props) {
  const router = useRouter();

  const searchParams = useSearchParams();

  const [formOpen, setFormOpen] = React.useState(false);
  const [editing, setEditing] = React.useState<ProductListItem | null>(null);
  const [detailId, setDetailId] = React.useState<number | null>(null);
  const [optionsTarget, setOptionsTarget] = React.useState<ProductListItem | null>(null);
  const [imagesTarget, setImagesTarget] = React.useState<ProductListItem | null>(null);
  const [categoryOpen, setCategoryOpen] = React.useState(false);
  const [soldOutTarget, setSoldOutTarget] = React.useState<ProductListItem | null>(null);
  const [deactivateTarget, setDeactivateTarget] = React.useState<ProductListItem | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const [shopIdInput, setShopIdInput] = React.useState(initialShopId === undefined ? "" : String(initialShopId));
  const [nameInput, setNameInput] = React.useState(initialName ?? "");
  const [visibleInput, setVisibleInput] = React.useState(initialVisible === undefined ? "all" : String(initialVisible));
  const [soldOutInput, setSoldOutInput] = React.useState(initialSoldOut === undefined ? "all" : String(initialSoldOut));

  function pushParams(next: {
    page?: number;
    size?: number;
    shopId?: string;
    name?: string;
    visible?: string;
    soldOut?: string;
  }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.page !== undefined) params.set("page", String(next.page));
    if (next.size !== undefined) params.set("size", String(next.size));
    for (const key of ["shopId", "name", "visible", "soldOut"] as const) {
      if (next[key] === undefined) continue;
      const v = next[key];
      if (!v || v === "all") params.delete(key);
      else params.set(key, v);
    }
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  function handleSearch(override?: { visible?: string; soldOut?: string }) {
    pushParams({
      page: 0,
      shopId: shopIdInput,
      name: nameInput,
      visible: override?.visible ?? visibleInput,
      soldOut: override?.soldOut ?? soldOutInput,
    });
  }

  function handleReset() {
    setShopIdInput("");
    setNameInput("");
    setVisibleInput("all");
    setSoldOutInput("all");
    pushParams({ page: 0, shopId: "", name: "", visible: "all", soldOut: "all" });
  }

  function openCreate() {
    setEditing(null);
    setFormOpen(true);
  }

  function openEdit(product: ProductListItem) {
    setEditing(product);
    setFormOpen(true);
  }

  const table = useReactTable({
    data: products,
    columns: productsColumns,
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
      onView: (product) => setDetailId(product.id),
      onEdit: (product) => openEdit(product),
      onManageOptions: (product) => setOptionsTarget(product),
      onManageImages: (product) => setImagesTarget(product),
      onSoldOut: (product) => setSoldOutTarget(product),
      onDeactivate: (product) => setDeactivateTarget(product),
    } satisfies ProductsTableMeta,
  });

  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{PRODUCT_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{PRODUCT_PAGE_COPY.DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          <Button size="sm" variant="outline" onClick={() => setCategoryOpen(true)}>
            <Layers /> 카테고리 관리
          </Button>
          <Button size="sm" onClick={openCreate}>
            <Plus /> 상품 등록
          </Button>
        </CardAction>
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
            placeholder="매장 ID"
            value={shopIdInput}
            onChange={(e) => setShopIdInput(e.target.value)}
            disabled={isPending}
          />
          <Input
            className="w-40"
            placeholder="상품명"
            value={nameInput}
            onChange={(e) => setNameInput(e.target.value)}
            disabled={isPending}
          />
          <Select
            value={visibleInput}
            onValueChange={(value) => {
              setVisibleInput(value);
              handleSearch({ visible: value });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">노출 여부:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value="all">전체</SelectItem>
                <SelectItem value="true">노출</SelectItem>
                <SelectItem value="false">미노출</SelectItem>
              </SelectGroup>
            </SelectContent>
          </Select>
          <Select
            value={soldOutInput}
            onValueChange={(value) => {
              setSoldOutInput(value);
              handleSearch({ soldOut: value });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">품절 여부:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value="all">전체</SelectItem>
                <SelectItem value="true">품절</SelectItem>
                <SelectItem value="false">판매중</SelectItem>
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
        <ProductsTable table={table} isPending={isPending} />
      </CardContent>
      <ProductFormSheet open={formOpen} onOpenChange={setFormOpen} product={editing} />
      <ProductDetailSheet productId={detailId} onOpenChange={(open) => !open && setDetailId(null)} />
      <ProductOptionsSheet product={optionsTarget} onOpenChange={(open) => !open && setOptionsTarget(null)} />
      <ProductImagesSheet product={imagesTarget} onOpenChange={(open) => !open && setImagesTarget(null)} />
      <ProductCategorySheet open={categoryOpen} onOpenChange={setCategoryOpen} initialShopId={initialShopId} />
      <SoldOutProductDialog product={soldOutTarget} onOpenChange={(open) => !open && setSoldOutTarget(null)} />
      <DeactivateProductDialog product={deactivateTarget} onOpenChange={(open) => !open && setDeactivateTarget(null)} />
    </Card>
  );
}
