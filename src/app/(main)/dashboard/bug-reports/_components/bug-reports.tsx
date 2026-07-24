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
import {
  BUG_CATEGORY,
  BUG_CATEGORY_LABEL,
  BUG_PRIORITY,
  BUG_PRIORITY_LABEL,
  BUG_STATUS,
  BUG_STATUS_LABEL,
  type BugCategory,
  type BugPriority,
  type BugStatus,
} from "@/feature/bug-report/constants";
import type { BugReportListItem } from "@/feature/bug-report/domain";
import { BUG_REPORT_PAGE_COPY } from "@/feature/bug-report/message";

import { BugReportDetailSheet } from "./bug-report-detail-sheet";
import { type BugReportsTableMeta, bugReportsColumns } from "./bug-reports-columns";
import { BugReportsTable } from "./bug-reports-table";

interface Props {
  bugReports: BugReportListItem[];
  pagination: ApiPagination;
  initialTitle?: string;
  initialContent?: string;
  initialMemberId?: number;
  initialStatus?: BugStatus;
  initialCategory?: BugCategory;
  initialPriority?: BugPriority;
}

type FilterParams = {
  page?: number;
  size?: number;
  title?: string;
  content?: string;
  memberId?: string;
  status?: string;
  category?: string;
  priority?: string;
};

const ALL = "all";

export function BugReports({
  bugReports,
  pagination,
  initialTitle,
  initialContent,
  initialMemberId,
  initialStatus,
  initialCategory,
  initialPriority,
}: Props) {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [detailId, setDetailId] = React.useState<number | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const [titleInput, setTitleInput] = React.useState(initialTitle ?? "");
  const [contentInput, setContentInput] = React.useState(initialContent ?? "");
  const [memberIdInput, setMemberIdInput] = React.useState(initialMemberId != null ? String(initialMemberId) : "");
  const [statusInput, setStatusInput] = React.useState<string>(initialStatus ?? ALL);
  const [categoryInput, setCategoryInput] = React.useState<string>(initialCategory ?? ALL);
  const [priorityInput, setPriorityInput] = React.useState<string>(initialPriority ?? ALL);

  function pushParams(next: FilterParams) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.page !== undefined) params.set("page", String(next.page));
    if (next.size !== undefined) params.set("size", String(next.size));
    for (const key of ["title", "content", "memberId", "status", "category", "priority"] as const) {
      if (next[key] === undefined) continue;
      const value = next[key];
      if (!value || value === ALL) params.delete(key);
      else params.set(key, value);
    }
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  function handleSearch(override?: Partial<Pick<FilterParams, "status" | "category" | "priority">>) {
    pushParams({
      page: 0,
      title: titleInput,
      content: contentInput,
      memberId: memberIdInput.trim(),
      status: override?.status ?? statusInput,
      category: override?.category ?? categoryInput,
      priority: override?.priority ?? priorityInput,
    });
  }

  function handleReset() {
    setTitleInput("");
    setContentInput("");
    setMemberIdInput("");
    setStatusInput(ALL);
    setCategoryInput(ALL);
    setPriorityInput(ALL);
    pushParams({ page: 0, title: "", content: "", memberId: "", status: ALL, category: ALL, priority: ALL });
  }

  const table = useReactTable({
    data: bugReports,
    columns: bugReportsColumns,
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
      onView: (bugReport) => setDetailId(bugReport.id),
    } satisfies BugReportsTableMeta,
  });

  return (
    <Card>
      <CardHeader className="border-b">
        <CardTitle className="text-xl leading-none">{BUG_REPORT_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{BUG_REPORT_PAGE_COPY.DESCRIPTION}</CardDescription>
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
          <Input
            className="w-32"
            placeholder="회원 ID"
            inputMode="numeric"
            value={memberIdInput}
            onChange={(e) => setMemberIdInput(e.target.value.replace(/[^0-9]/g, ""))}
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
                <SelectItem value={ALL}>전체</SelectItem>
                {BUG_STATUS.map((status) => (
                  <SelectItem key={status} value={status}>
                    {BUG_STATUS_LABEL[status]}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
          <Select
            value={categoryInput}
            onValueChange={(value) => {
              setCategoryInput(value);
              handleSearch({ category: value });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">분류:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value={ALL}>전체</SelectItem>
                {BUG_CATEGORY.map((category) => (
                  <SelectItem key={category} value={category}>
                    {BUG_CATEGORY_LABEL[category]}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
          <Select
            value={priorityInput}
            onValueChange={(value) => {
              setPriorityInput(value);
              handleSearch({ priority: value });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">우선순위:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value={ALL}>전체</SelectItem>
                {BUG_PRIORITY.map((priority) => (
                  <SelectItem key={priority} value={priority}>
                    {BUG_PRIORITY_LABEL[priority]}
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
        <BugReportsTable table={table} isPending={isPending} />
      </CardContent>
      <BugReportDetailSheet bugReportId={detailId} onOpenChange={(open) => !open && setDetailId(null)} />
    </Card>
  );
}
