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
import type { CouponListItem, DiscountType } from "@/feature/coupon/domain";
import { COUPON_PAGE_COPY } from "@/feature/coupon/message";

import { CouponDetailSheet } from "./coupon-detail-sheet";
import { CouponFormSheet } from "./coupon-form-sheet";
import { CouponIssueSheet } from "./coupon-issue-sheet";
import { type CouponsTableMeta, couponsColumns } from "./coupons-columns";
import { CouponsTable } from "./coupons-table";
import { DeleteCouponDialog } from "./delete-coupon-dialog";

interface Props {
  coupons: CouponListItem[];
  pagination: ApiPagination;
  initialName?: string;
  initialDiscountType?: DiscountType;
  initialVisible?: boolean;
}

export function Coupons({ coupons, pagination, initialName, initialDiscountType, initialVisible }: Props) {
  const router = useRouter();

  const searchParams = useSearchParams();

  const [formOpen, setFormOpen] = React.useState(false);
  const [editing, setEditing] = React.useState<CouponListItem | null>(null);
  const [detailId, setDetailId] = React.useState<number | null>(null);
  const [issuing, setIssuing] = React.useState<CouponListItem | null>(null);
  const [deleting, setDeleting] = React.useState<CouponListItem | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const [nameInput, setNameInput] = React.useState(initialName ?? "");
  const [discountTypeInput, setDiscountTypeInput] = React.useState(initialDiscountType ?? "all");
  const [visibleInput, setVisibleInput] = React.useState(initialVisible === undefined ? "all" : String(initialVisible));

  function pushParams(next: { page?: number; size?: number; name?: string; discountType?: string; visible?: string }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.page !== undefined) params.set("page", String(next.page));
    if (next.size !== undefined) params.set("size", String(next.size));
    for (const key of ["name", "discountType", "visible"] as const) {
      if (next[key] === undefined) continue;
      const v = next[key];
      if (!v || v === "all") params.delete(key);
      else params.set(key, v);
    }
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  function handleSearch(override?: { discountType?: string; visible?: string }) {
    pushParams({
      page: 0,
      name: nameInput,
      discountType: override?.discountType ?? discountTypeInput,
      visible: override?.visible ?? visibleInput,
    });
  }

  function handleReset() {
    setNameInput("");
    setDiscountTypeInput("all");
    setVisibleInput("all");
    pushParams({ page: 0, name: "", discountType: "all", visible: "all" });
  }

  function openCreate() {
    setEditing(null);
    setFormOpen(true);
  }

  function openEdit(coupon: CouponListItem) {
    setEditing(coupon);
    setFormOpen(true);
  }

  const table = useReactTable({
    data: coupons,
    columns: couponsColumns,
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
      onView: (coupon) => setDetailId(coupon.id),
      onEdit: (coupon) => openEdit(coupon),
      onIssue: (coupon) => setIssuing(coupon),
      onDelete: (coupon) => setDeleting(coupon),
    } satisfies CouponsTableMeta,
  });

  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{COUPON_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{COUPON_PAGE_COPY.DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          <Button size="sm" onClick={openCreate}>
            <Plus /> 쿠폰 등록
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
            placeholder="쿠폰 이름"
            value={nameInput}
            onChange={(e) => setNameInput(e.target.value)}
            disabled={isPending}
          />
          <Select
            value={discountTypeInput}
            onValueChange={(value) => {
              setDiscountTypeInput(value);
              handleSearch({ discountType: value });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">할인 유형:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value="all">전체</SelectItem>
                <SelectItem value="AMOUNT">정액</SelectItem>
                <SelectItem value="RATE">정률</SelectItem>
              </SelectGroup>
            </SelectContent>
          </Select>
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
          <Button type="submit" size="sm" disabled={isPending}>
            <Search className="size-4" />
            검색
          </Button>
          <Button type="button" size="sm" variant="destructive" onClick={handleReset} disabled={isPending}>
            <X className="size-4" />
            초기화
          </Button>
        </form>
        <CouponsTable table={table} isPending={isPending} />
      </CardContent>
      <CouponFormSheet open={formOpen} onOpenChange={setFormOpen} coupon={editing} />
      <CouponDetailSheet couponId={detailId} onOpenChange={(open) => !open && setDetailId(null)} />
      <CouponIssueSheet coupon={issuing} onOpenChange={(open) => !open && setIssuing(null)} />
      <DeleteCouponDialog coupon={deleting} onOpenChange={(open) => !open && setDeleting(null)} />
    </Card>
  );
}
