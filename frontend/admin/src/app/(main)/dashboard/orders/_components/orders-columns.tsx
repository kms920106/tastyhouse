"use client";
"use no memo";

import type { ColumnDef } from "@tanstack/react-table";
import { MoreHorizontal } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import type { OrderListItem } from "@/feature/order/domain";
import {
  formatScheduledAt,
  formatWon,
  orderMethodLabel,
  orderStatusBadgeVariant,
  orderStatusLabel,
  paymentStatusBadgeVariant,
  paymentStatusLabel,
} from "@/feature/order/format";
import { formatDateTime } from "@/lib/date";

export interface OrdersTableMeta {
  totalElements: number;
  onView: (order: OrderListItem) => void;
  onChangeStatus: (order: OrderListItem) => void;
  onDelete: (order: OrderListItem) => void;
}

export const ordersColumns: ColumnDef<OrderListItem>[] = [
  {
    accessorKey: "orderNumber",
    header: "주문번호",
    cell: ({ row }) => <span className="line-clamp-1 font-medium">{row.original.orderNumber}</span>,
    size: 220,
    minSize: 180,
    maxSize: 260,
  },
  {
    accessorKey: "shopName",
    header: "매장명",
    cell: ({ row }) => <span className="line-clamp-1">{row.original.shopName}</span>,
    size: 140,
    minSize: 100,
    maxSize: 180,
  },
  {
    accessorKey: "ordererName",
    header: "주문자명",
    cell: ({ row }) => <span>{row.original.ordererName}</span>,
    size: 100,
    minSize: 80,
    maxSize: 140,
  },
  {
    accessorKey: "orderMethod",
    header: "주문방식",
    cell: ({ row }) => <span>{orderMethodLabel(row.original.orderMethod)}</span>,
    enableSorting: false,
    size: 100,
    minSize: 90,
    maxSize: 120,
  },
  {
    accessorKey: "orderStatus",
    header: "주문상태",
    cell: ({ row }) => (
      <Badge variant={orderStatusBadgeVariant(row.original.orderStatus)}>
        {orderStatusLabel(row.original.orderStatus)}
      </Badge>
    ),
    size: 90,
    minSize: 90,
    maxSize: 100,
  },
  {
    accessorKey: "paymentStatus",
    header: "결제상태",
    cell: ({ row }) => (
      <Badge variant={paymentStatusBadgeVariant(row.original.paymentStatus)}>
        {paymentStatusLabel(row.original.paymentStatus)}
      </Badge>
    ),
    size: 90,
    minSize: 90,
    maxSize: 100,
  },
  {
    accessorKey: "finalAmount",
    header: () => <div className="text-right">결제금액</div>,
    cell: ({ row }) => <div className="text-right tabular-nums">{formatWon(row.original.finalAmount)}</div>,
    size: 110,
    minSize: 100,
    maxSize: 130,
  },
  {
    accessorKey: "totalItemCount",
    header: () => <div className="text-right">수량</div>,
    cell: ({ row }) => <div className="text-right tabular-nums">{row.original.totalItemCount}</div>,
    enableSorting: false,
    size: 70,
    minSize: 60,
    maxSize: 90,
  },
  {
    accessorKey: "createdAt",
    header: "주문일시",
    cell: ({ row }) => (
      <span className="whitespace-nowrap text-muted-foreground text-sm tabular-nums">
        {formatDateTime(row.original.createdAt)}
      </span>
    ),
    enableSorting: false,
    size: 160,
    minSize: 140,
    maxSize: 180,
  },
  {
    accessorKey: "scheduledAt",
    header: "수령 예약",
    cell: ({ row }) => (
      <span className="whitespace-nowrap text-muted-foreground text-sm tabular-nums">
        {formatScheduledAt(row.original.scheduledAt)}
      </span>
    ),
    enableSorting: false,
    size: 160,
    minSize: 140,
    maxSize: 180,
  },
  {
    id: "actions",
    header: () => <div className="text-right">작업</div>,
    cell: ({ row, table }) => {
      const order = row.original;
      const meta = table.options.meta as OrdersTableMeta;

      return (
        <div className="text-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label="주문 작업 메뉴">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onSelect={() => meta.onView(order)}>상세 보기</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => meta.onChangeStatus(order)}>상태 변경</DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem variant="destructive" onSelect={() => meta.onDelete(order)}>
                삭제
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      );
    },
    enableSorting: false,
    enableHiding: false,
    size: 80,
    minSize: 80,
    maxSize: 80,
  },
];
