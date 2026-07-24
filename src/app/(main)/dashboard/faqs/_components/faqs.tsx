"use client";
"use no memo";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { getCoreRowModel, type PaginationState, useReactTable } from "@tanstack/react-table";
import { Plus, Search, Settings, X } from "lucide-react";

import type { ApiPagination } from "@/api/shared/types";
import { Button } from "@/components/ui/button";
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import type { FaqCategory, FaqListItem } from "@/feature/faq/domain";
import { FAQ_PAGE_COPY } from "@/feature/faq/message";

import { DeleteFaqDialog } from "./delete-faq-dialog";
import { FaqCategoryManagerSheet } from "./faq-category-manager-sheet";
import { FaqDetailSheet } from "./faq-detail-sheet";
import { FaqFormSheet } from "./faq-form-sheet";
import { type FaqsTableMeta, faqsColumns } from "./faqs-columns";
import { FaqsTable } from "./faqs-table";

interface Props {
  faqs: FaqListItem[];
  pagination: ApiPagination;
  categories: FaqCategory[];
  initialCategoryId?: number;
  initialQuestion?: string;
  initialVisible?: boolean;
}

export function Faqs({ faqs, pagination, categories, initialCategoryId, initialQuestion, initialVisible }: Props) {
  const router = useRouter();

  const searchParams = useSearchParams();

  const [formOpen, setFormOpen] = React.useState(false);
  const [editing, setEditing] = React.useState<FaqListItem | null>(null);
  const [detailId, setDetailId] = React.useState<number | null>(null);
  const [deleting, setDeleting] = React.useState<FaqListItem | null>(null);
  const [categoryManagerOpen, setCategoryManagerOpen] = React.useState(false);
  const [isPending, startTransition] = React.useTransition();

  const [categoryIdInput, setCategoryIdInput] = React.useState(
    initialCategoryId === undefined ? "all" : String(initialCategoryId),
  );
  const [questionInput, setQuestionInput] = React.useState(initialQuestion ?? "");
  const [visibleInput, setVisibleInput] = React.useState(initialVisible === undefined ? "all" : String(initialVisible));

  const categoryNameById = React.useMemo(() => new Map(categories.map((c) => [c.id, c.name])), [categories]);

  function pushParams(next: {
    page?: number;
    size?: number;
    categoryId?: string;
    question?: string;
    visible?: string;
  }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.page !== undefined) params.set("page", String(next.page));
    if (next.size !== undefined) params.set("size", String(next.size));
    for (const key of ["categoryId", "question", "visible"] as const) {
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
    pushParams({ page: 0, categoryId: categoryIdInput, question: questionInput, visible });
  }

  function handleReset() {
    setCategoryIdInput("all");
    setQuestionInput("");
    setVisibleInput("all");
    pushParams({ page: 0, categoryId: "all", question: "", visible: "all" });
  }

  function openCreate() {
    setEditing(null);
    setFormOpen(true);
  }

  function openEdit(faq: FaqListItem) {
    setEditing(faq);
    setFormOpen(true);
  }

  function handleCategoryManagerOpenChange(open: boolean) {
    setCategoryManagerOpen(open);
    if (!open) {
      router.refresh();
    }
  }

  const table = useReactTable({
    data: faqs,
    columns: faqsColumns,
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
      categoryNameById,
      onView: (faq) => setDetailId(faq.id),
      onEdit: (faq) => openEdit(faq),
      onDelete: (faq) => setDeleting(faq),
    } satisfies FaqsTableMeta,
  });

  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{FAQ_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{FAQ_PAGE_COPY.DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          <Button size="sm" variant="outline" onClick={() => setCategoryManagerOpen(true)}>
            <Settings /> 카테고리 관리
          </Button>
          <Button size="sm" onClick={openCreate}>
            <Plus /> FAQ 등록
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
          <Select
            value={categoryIdInput}
            onValueChange={(value) => {
              setCategoryIdInput(value);
              pushParams({ page: 0, categoryId: value, question: questionInput, visible: visibleInput });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm" className="w-40">
              <span className="text-muted-foreground">카테고리:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value="all">전체</SelectItem>
                {categories.map((category) => (
                  <SelectItem key={category.id} value={String(category.id)}>
                    {category.name}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
          <Input
            className="w-40"
            placeholder="질문"
            value={questionInput}
            onChange={(e) => setQuestionInput(e.target.value)}
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
        <FaqsTable table={table} isPending={isPending} />
      </CardContent>
      <FaqFormSheet open={formOpen} onOpenChange={setFormOpen} faq={editing} categories={categories} />
      <FaqDetailSheet
        faqId={detailId}
        onOpenChange={(open) => !open && setDetailId(null)}
        categoryNameById={categoryNameById}
      />
      <DeleteFaqDialog faq={deleting} onOpenChange={(open) => !open && setDeleting(null)} />
      <FaqCategoryManagerSheet
        open={categoryManagerOpen}
        onOpenChange={handleCategoryManagerOpenChange}
        categories={categories}
      />
    </Card>
  );
}
