"use client";
"use no memo";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { getCoreRowModel, type PaginationState, useReactTable } from "@tanstack/react-table";
import { Plus, Search, X } from "lucide-react";

import type { ApiPagination } from "@/api/shared/types";
import { Button } from "@/components/ui/button";
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import type { BannerListItem, BannerType } from "@/feature/banner/domain";
import { BANNER_PAGE_COPY } from "@/feature/banner/message";

import { BannerDetailSheet } from "./banner-detail-sheet";
import { BannerFormSheet } from "./banner-form-sheet";
import { type BannersTableMeta, bannersColumns } from "./banners-columns";
import { BannersTable } from "./banners-table";
import { DeleteBannerDialog } from "./delete-banner-dialog";

interface Props {
  banners: BannerListItem[];
  pagination: ApiPagination;
  initialType?: BannerType;
  initialTitle?: string;
  initialVisible?: boolean;
}

export function Banners({ banners, pagination, initialType, initialTitle, initialVisible }: Props) {
  const router = useRouter();

  const searchParams = useSearchParams();

  const [formOpen, setFormOpen] = React.useState(false);
  const [editing, setEditing] = React.useState<BannerListItem | null>(null);
  const [detailId, setDetailId] = React.useState<number | null>(null);
  const [deleting, setDeleting] = React.useState<BannerListItem | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const [typeInput, setTypeInput] = React.useState(initialType ?? "all");
  const [titleInput, setTitleInput] = React.useState(initialTitle ?? "");
  const [visibleInput, setVisibleInput] = React.useState(initialVisible === undefined ? "all" : String(initialVisible));

  function pushParams(next: { page?: number; size?: number; type?: string; title?: string; visible?: string }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.page !== undefined) params.set("page", String(next.page));
    if (next.size !== undefined) params.set("size", String(next.size));
    for (const key of ["type", "title", "visible"] as const) {
      if (next[key] === undefined) continue;
      const v = next[key];
      if (!v || v === "all") params.delete(key);
      else params.set(key, v);
    }
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  function handleTypeChange(value: string) {
    setTypeInput(value as BannerType | "all");
    pushParams({ page: 0, type: value });
  }

  function handleSearch(overrideVisible?: string) {
    const visible = overrideVisible ?? visibleInput;
    pushParams({ page: 0, title: titleInput, visible });
  }

  function handleReset() {
    setTypeInput("all");
    setTitleInput("");
    setVisibleInput("all");
    pushParams({ page: 0, type: "all", title: "", visible: "all" });
  }

  function openCreate() {
    setEditing(null);
    setFormOpen(true);
  }

  function openEdit(banner: BannerListItem) {
    setEditing(banner);
    setFormOpen(true);
  }

  const table = useReactTable({
    data: banners,
    columns: bannersColumns,
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
      onView: (banner) => setDetailId(banner.id),
      onEdit: (banner) => openEdit(banner),
      onDelete: (banner) => setDeleting(banner),
    } satisfies BannersTableMeta,
  });

  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{BANNER_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{BANNER_PAGE_COPY.DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          <Button size="sm" onClick={openCreate}>
            <Plus /> 배너 등록
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
          <Select value={typeInput} onValueChange={handleTypeChange} disabled={isPending}>
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">유형:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value="all">전체</SelectItem>
                <SelectItem value="HOME">홈</SelectItem>
                <SelectItem value="SIDEBAR">사이드바</SelectItem>
              </SelectGroup>
            </SelectContent>
          </Select>
          <Input
            className="w-40"
            placeholder="제목"
            value={titleInput}
            onChange={(e) => setTitleInput(e.target.value)}
            disabled={isPending}
          />
          <Select
            value={visibleInput}
            onValueChange={(value) => {
              setVisibleInput(value);
              handleSearch(value);
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
          <Button type="submit" size="sm" disabled={isPending}>
            <Search className="size-4" />
            검색
          </Button>
          <Button type="button" size="sm" variant="destructive" onClick={handleReset} disabled={isPending}>
            <X className="size-4" />
            초기화
          </Button>
        </form>
        <BannersTable table={table} isPending={isPending} />
      </CardContent>
      <BannerFormSheet open={formOpen} onOpenChange={setFormOpen} banner={editing} />
      <BannerDetailSheet bannerId={detailId} onOpenChange={(open) => !open && setDetailId(null)} />
      <DeleteBannerDialog banner={deleting} onOpenChange={(open) => !open && setDeleting(null)} />
    </Card>
  );
}
