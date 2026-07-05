"use client";
"use no memo";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { getCoreRowModel, type PaginationState, useReactTable } from "@tanstack/react-table";
import { Plus, Search, X } from "lucide-react";

import type { NoticeListItem } from "@/api/notice/notice.dto";
import type { ApiPagination } from "@/api/shared/types";
import { Button } from "@/components/ui/button";
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

import { DeleteNoticeDialog } from "./delete-notice-dialog";
import { NoticeDetailSheet } from "./notice-detail-sheet";
import { NoticeFormSheet } from "./notice-form-sheet";
import { type NoticesTableMeta, noticesColumns } from "./notices-columns";
import { NoticesTable } from "./notices-table";

interface Props {
  notices: NoticeListItem[];
  pagination: ApiPagination;
  initialTitle?: string;
  initialContent?: string;
  initialVisible?: boolean;
}

export function Notices({ notices, pagination, initialTitle, initialContent, initialVisible }: Props) {
  const router = useRouter();

  const searchParams = useSearchParams();

  const [formOpen, setFormOpen] = React.useState(false);
  const [editing, setEditing] = React.useState<NoticeListItem | null>(null);
  const [detailId, setDetailId] = React.useState<number | null>(null);
  const [deleting, setDeleting] = React.useState<NoticeListItem | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const [titleInput, setTitleInput] = React.useState(initialTitle ?? "");
  const [contentInput, setContentInput] = React.useState(initialContent ?? "");
  const [visibleInput, setVisibleInput] = React.useState(initialVisible === undefined ? "all" : String(initialVisible));

  function pushParams(next: { page?: number; size?: number; title?: string; content?: string; visible?: string }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.page !== undefined) params.set("page", String(next.page));
    if (next.size !== undefined) params.set("size", String(next.size));
    for (const key of ["title", "content", "visible"] as const) {
      if (next[key] === undefined) continue;
      const v = next[key];
      if (!v || v === "all") params.delete(key);
      else params.set(key, v);
    }
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  function handleSearch(overrideVisible?: string) {
    const visible = overrideVisible ?? visibleInput;
    pushParams({ page: 0, title: titleInput, content: contentInput, visible });
  }

  function handleReset() {
    setTitleInput("");
    setContentInput("");
    setVisibleInput("all");
    pushParams({ page: 0, title: "", content: "", visible: "all" });
  }

  function openCreate() {
    setEditing(null);
    setFormOpen(true);
  }

  function openEdit(notice: NoticeListItem) {
    setEditing(notice);
    setFormOpen(true);
  }

  const table = useReactTable({
    data: notices,
    columns: noticesColumns,
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
      onView: (notice) => setDetailId(notice.id),
      onEdit: (notice) => openEdit(notice),
      onDelete: (notice) => setDeleting(notice),
    } satisfies NoticesTableMeta,
  });

  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">공지사항</CardTitle>
        <CardDescription className="max-w-sm leading-snug">서비스 공지사항을 등록하고 관리합니다.</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          <Button size="sm" onClick={openCreate}>
            <Plus /> 공지 등록
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
            placeholder="제목"
            value={titleInput}
            onChange={(e) => setTitleInput(e.target.value)}
            disabled={isPending}
          />
          <Input
            className="w-40"
            placeholder="내용"
            value={contentInput}
            onChange={(e) => setContentInput(e.target.value)}
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
        <NoticesTable table={table} isPending={isPending} />
      </CardContent>
      <NoticeFormSheet open={formOpen} onOpenChange={setFormOpen} notice={editing} />
      <NoticeDetailSheet noticeId={detailId} onOpenChange={(open) => !open && setDetailId(null)} />
      <DeleteNoticeDialog notice={deleting} onOpenChange={(open) => !open && setDeleting(null)} />
    </Card>
  );
}
