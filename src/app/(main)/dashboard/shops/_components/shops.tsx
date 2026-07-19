"use client";
"use no memo";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { getCoreRowModel, type PaginationState, useReactTable } from "@tanstack/react-table";
import { Layers, Plus, Search, Sparkles, Tag as TagIcon, UtensilsCrossed, X } from "lucide-react";

import type { ApiPagination } from "@/api/shared/types";
import { Button } from "@/components/ui/button";
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import type { ShopListItem } from "@/feature/shop/domain";
import { SHOP_PAGE_COPY } from "@/feature/shop/message";

import { AmenityCategoriesSheet } from "./amenity-categories-sheet";
import { CloseShopDialog } from "./close-shop-dialog";
import { EditorChoicesSheet } from "./editor-choices-sheet";
import { FoodTypeCategoriesSheet } from "./food-type-categories-sheet";
import { ShopFormSheet } from "./shop-form-sheet";
import { type ShopsTableMeta, shopsColumns } from "./shops-columns";
import { ShopsTable } from "./shops-table";
import { TagsSheet } from "./tags-sheet";

interface Props {
  shops: ShopListItem[];
  pagination: ApiPagination;
  initialName?: string;
  initialStationId?: number;
  initialPermanentlyClosed?: boolean;
}

export function Shops({ shops, pagination, initialName, initialStationId, initialPermanentlyClosed }: Props) {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [formOpen, setFormOpen] = React.useState(false);
  const [editing, setEditing] = React.useState<ShopListItem | null>(null);
  const [closeTarget, setCloseTarget] = React.useState<ShopListItem | null>(null);
  const [amenityCategoriesOpen, setAmenityCategoriesOpen] = React.useState(false);
  const [foodTypeCategoriesOpen, setFoodTypeCategoriesOpen] = React.useState(false);
  const [tagsOpen, setTagsOpen] = React.useState(false);
  const [editorChoicesOpen, setEditorChoicesOpen] = React.useState(false);
  const [isPending, startTransition] = React.useTransition();

  const [nameInput, setNameInput] = React.useState(initialName ?? "");
  const [stationIdInput, setStationIdInput] = React.useState(
    initialStationId === undefined ? "" : String(initialStationId),
  );
  const [permanentlyClosedInput, setPermanentlyClosedInput] = React.useState(
    initialPermanentlyClosed === undefined ? "all" : String(initialPermanentlyClosed),
  );

  function pushParams(next: {
    page?: number;
    size?: number;
    name?: string;
    stationId?: string;
    permanentlyClosed?: string;
  }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.page !== undefined) params.set("page", String(next.page));
    if (next.size !== undefined) params.set("size", String(next.size));
    for (const key of ["name", "stationId", "permanentlyClosed"] as const) {
      if (next[key] === undefined) continue;
      const v = next[key];
      if (!v || v === "all") params.delete(key);
      else params.set(key, v);
    }
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  function handleSearch(override?: { permanentlyClosed?: string }) {
    pushParams({
      page: 0,
      name: nameInput,
      stationId: stationIdInput,
      permanentlyClosed: override?.permanentlyClosed ?? permanentlyClosedInput,
    });
  }

  function handleReset() {
    setNameInput("");
    setStationIdInput("");
    setPermanentlyClosedInput("all");
    pushParams({ page: 0, name: "", stationId: "", permanentlyClosed: "all" });
  }

  function openCreate() {
    setEditing(null);
    setFormOpen(true);
  }

  function openEdit(shop: ShopListItem) {
    setEditing(shop);
    setFormOpen(true);
  }

  const table = useReactTable({
    data: shops,
    columns: shopsColumns,
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
      onView: (shop) => router.push(`/dashboard/shops/${shop.id}`),
      onEdit: (shop) => openEdit(shop),
      onClose: (shop) => setCloseTarget(shop),
    } satisfies ShopsTableMeta,
  });

  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{SHOP_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{SHOP_PAGE_COPY.DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          <Button size="sm" variant="outline" onClick={() => setAmenityCategoriesOpen(true)}>
            <Layers /> 편의시설 카테고리
          </Button>
          <Button size="sm" variant="outline" onClick={() => setFoodTypeCategoriesOpen(true)}>
            <UtensilsCrossed /> 음식종류 카테고리
          </Button>
          <Button size="sm" variant="outline" onClick={() => setTagsOpen(true)}>
            <TagIcon /> 태그 관리
          </Button>
          <Button size="sm" variant="outline" onClick={() => setEditorChoicesOpen(true)}>
            <Sparkles /> 테하 초이스
          </Button>
          <Button size="sm" onClick={openCreate}>
            <Plus /> 가게 등록
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
            className="w-40"
            placeholder="상호명"
            value={nameInput}
            onChange={(e) => setNameInput(e.target.value)}
            disabled={isPending}
          />
          <Input
            className="w-28"
            type="number"
            min={1}
            placeholder="지하철역 ID"
            value={stationIdInput}
            onChange={(e) => setStationIdInput(e.target.value)}
            disabled={isPending}
          />
          <Select
            value={permanentlyClosedInput}
            onValueChange={(value) => {
              setPermanentlyClosedInput(value);
              handleSearch({ permanentlyClosed: value });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">폐업 여부:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value="all">전체</SelectItem>
                <SelectItem value="true">폐업</SelectItem>
                <SelectItem value="false">영업중</SelectItem>
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
        <ShopsTable table={table} isPending={isPending} />
      </CardContent>
      <ShopFormSheet open={formOpen} onOpenChange={setFormOpen} shop={editing} />
      <CloseShopDialog shop={closeTarget} onOpenChange={(open) => !open && setCloseTarget(null)} />
      <AmenityCategoriesSheet open={amenityCategoriesOpen} onOpenChange={setAmenityCategoriesOpen} />
      <FoodTypeCategoriesSheet open={foodTypeCategoriesOpen} onOpenChange={setFoodTypeCategoriesOpen} />
      <TagsSheet open={tagsOpen} onOpenChange={setTagsOpen} />
      <EditorChoicesSheet open={editorChoicesOpen} onOpenChange={setEditorChoicesOpen} />
    </Card>
  );
}
