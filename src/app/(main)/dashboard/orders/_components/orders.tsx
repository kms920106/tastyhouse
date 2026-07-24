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
import { ORDER_METHOD_OPTIONS, ORDER_STATUS_OPTIONS, PAYMENT_STATUS_OPTIONS } from "@/feature/order/constants";
import type { OrderListItem, OrderMethod, OrderStatus, PaymentStatus } from "@/feature/order/domain";
import { ORDER_PAGE_COPY } from "@/feature/order/message";

import { DeleteOrderDialog } from "./delete-order-dialog";
import { OrderDetailSheet } from "./order-detail-sheet";
import { OrderStatusDialog } from "./order-status-dialog";
import { type OrdersTableMeta, ordersColumns } from "./orders-columns";
import { OrdersTable } from "./orders-table";

interface Props {
  orders: OrderListItem[];
  pagination: ApiPagination;
  initialShopId?: number;
  initialOrderStatus?: OrderStatus;
  initialOrderMethod?: OrderMethod;
  initialPaymentStatus?: PaymentStatus;
  initialOrderNumber?: string;
  initialOrdererName?: string;
  initialStartDate?: string;
  initialEndDate?: string;
}

export function Orders({
  orders,
  pagination,
  initialShopId,
  initialOrderStatus,
  initialOrderMethod,
  initialPaymentStatus,
  initialOrderNumber,
  initialOrdererName,
  initialStartDate,
  initialEndDate,
}: Props) {
  const router = useRouter();

  const searchParams = useSearchParams();

  const [detailId, setDetailId] = React.useState<number | null>(null);
  const [changingStatus, setChangingStatus] = React.useState<OrderListItem | null>(null);
  const [deleting, setDeleting] = React.useState<OrderListItem | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const [shopIdInput, setShopIdInput] = React.useState(initialShopId != null ? String(initialShopId) : "");
  const [orderNumberInput, setOrderNumberInput] = React.useState(initialOrderNumber ?? "");
  const [ordererNameInput, setOrdererNameInput] = React.useState(initialOrdererName ?? "");
  const [orderStatusInput, setOrderStatusInput] = React.useState<string>(initialOrderStatus ?? "all");
  const [orderMethodInput, setOrderMethodInput] = React.useState<string>(initialOrderMethod ?? "all");
  const [paymentStatusInput, setPaymentStatusInput] = React.useState<string>(initialPaymentStatus ?? "all");
  const [startDateInput, setStartDateInput] = React.useState(initialStartDate ?? "");
  const [endDateInput, setEndDateInput] = React.useState(initialEndDate ?? "");

  function pushParams(next: {
    page?: number;
    size?: number;
    shopId?: string;
    orderNumber?: string;
    ordererName?: string;
    orderStatus?: string;
    orderMethod?: string;
    paymentStatus?: string;
    startDate?: string;
    endDate?: string;
  }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.page !== undefined) params.set("page", String(next.page));
    if (next.size !== undefined) params.set("size", String(next.size));
    for (const key of [
      "shopId",
      "orderNumber",
      "ordererName",
      "orderStatus",
      "orderMethod",
      "paymentStatus",
      "startDate",
      "endDate",
    ] as const) {
      if (next[key] === undefined) continue;
      const v = next[key];
      if (!v || v === "all") params.delete(key);
      else params.set(key, v);
    }
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  /** datetime-local 입력값("YYYY-MM-DDTHH:mm")에 초 단위를 보정해 LocalDateTime 형식으로 맞춘다. */
  function toLocalDateTime(value: string): string {
    if (!value) return value;
    return value.length === 16 ? `${value}:00` : value;
  }

  function handleSearch(override?: { orderStatus?: string; orderMethod?: string; paymentStatus?: string }) {
    pushParams({
      page: 0,
      shopId: shopIdInput,
      orderNumber: orderNumberInput,
      ordererName: ordererNameInput,
      orderStatus: override?.orderStatus ?? orderStatusInput,
      orderMethod: override?.orderMethod ?? orderMethodInput,
      paymentStatus: override?.paymentStatus ?? paymentStatusInput,
      startDate: toLocalDateTime(startDateInput),
      endDate: toLocalDateTime(endDateInput),
    });
  }

  function handleReset() {
    setShopIdInput("");
    setOrderNumberInput("");
    setOrdererNameInput("");
    setOrderStatusInput("all");
    setOrderMethodInput("all");
    setPaymentStatusInput("all");
    setStartDateInput("");
    setEndDateInput("");
    pushParams({
      page: 0,
      shopId: "",
      orderNumber: "",
      ordererName: "",
      orderStatus: "all",
      orderMethod: "all",
      paymentStatus: "all",
      startDate: "",
      endDate: "",
    });
  }

  const table = useReactTable({
    data: orders,
    columns: ordersColumns,
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
      onView: (order) => setDetailId(order.id),
      onChangeStatus: (order) => setChangingStatus(order),
      onDelete: (order) => setDeleting(order),
    } satisfies OrdersTableMeta,
  });

  return (
    <Card>
      <CardHeader className="border-b">
        <CardTitle className="text-xl leading-none">{ORDER_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{ORDER_PAGE_COPY.DESCRIPTION}</CardDescription>
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
            className="w-24"
            placeholder="매장 ID"
            value={shopIdInput}
            onChange={(e) => setShopIdInput(e.target.value)}
            disabled={isPending}
          />
          <Input
            className="w-40"
            placeholder="주문번호"
            value={orderNumberInput}
            onChange={(e) => setOrderNumberInput(e.target.value)}
            disabled={isPending}
          />
          <Input
            className="w-32"
            placeholder="주문자명"
            value={ordererNameInput}
            onChange={(e) => setOrdererNameInput(e.target.value)}
            disabled={isPending}
          />
          <Select
            value={orderStatusInput}
            onValueChange={(value) => {
              setOrderStatusInput(value);
              handleSearch({ orderStatus: value });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">주문상태:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value="all">전체</SelectItem>
                {ORDER_STATUS_OPTIONS.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
          <Select
            value={orderMethodInput}
            onValueChange={(value) => {
              setOrderMethodInput(value);
              handleSearch({ orderMethod: value });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">주문방식:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value="all">전체</SelectItem>
                {ORDER_METHOD_OPTIONS.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
          <Select
            value={paymentStatusInput}
            onValueChange={(value) => {
              setPaymentStatusInput(value);
              handleSearch({ paymentStatus: value });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">결제상태:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value="all">전체</SelectItem>
                {PAYMENT_STATUS_OPTIONS.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
          <Input
            type="datetime-local"
            className="w-48"
            value={startDateInput}
            onChange={(e) => setStartDateInput(e.target.value)}
            disabled={isPending}
          />
          <span className="text-muted-foreground">~</span>
          <Input
            type="datetime-local"
            className="w-48"
            value={endDateInput}
            onChange={(e) => setEndDateInput(e.target.value)}
            disabled={isPending}
          />
          <Button type="submit" size="sm" disabled={isPending}>
            <Search className="size-4" />
            검색
          </Button>
          <Button type="button" size="sm" variant="destructive" onClick={handleReset} disabled={isPending}>
            <X className="size-4" />
            초기화
          </Button>
        </form>
        <OrdersTable table={table} isPending={isPending} />
      </CardContent>
      <OrderDetailSheet orderId={detailId} onOpenChange={(open) => !open && setDetailId(null)} />
      <OrderStatusDialog order={changingStatus} onOpenChange={(open) => !open && setChangingStatus(null)} />
      <DeleteOrderDialog order={deleting} onOpenChange={(open) => !open && setDeleting(null)} />
    </Card>
  );
}
