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
import type { CouponListItem } from "@/feature/coupon/domain";
import { discountTypeLabel, formatDiscountValue } from "@/feature/coupon/format";
import { formatDateTime } from "@/lib/date";

export interface CouponsTableMeta {
  totalElements: number;
  onView: (coupon: CouponListItem) => void;
  onEdit: (coupon: CouponListItem) => void;
  onIssue: (coupon: CouponListItem) => void;
  onDelete: (coupon: CouponListItem) => void;
}

export const couponsColumns: ColumnDef<CouponListItem>[] = [
  {
    accessorKey: "id",
    header: "ID",
    cell: ({ row }) => <span className="tabular-nums">{row.original.id}</span>,
    size: 80,
    minSize: 80,
    maxSize: 80,
  },
  {
    accessorKey: "name",
    header: "이름",
    cell: ({ row }) => <span className="line-clamp-1 font-medium">{row.original.name}</span>,
    size: 280,
    minSize: 180,
    maxSize: 320,
  },
  {
    accessorKey: "discountType",
    header: "할인 유형",
    cell: ({ row }) => (
      <Badge variant={row.original.discountType === "RATE" ? "outline" : "secondary"}>
        {discountTypeLabel(row.original.discountType)}
      </Badge>
    ),
    size: 100,
    minSize: 100,
    maxSize: 100,
  },
  {
    accessorKey: "discountAmount",
    header: "할인 값",
    cell: ({ row }) => (
      <span className="whitespace-nowrap tabular-nums">
        {formatDiscountValue(row.original.discountType, row.original.discountAmount)}
      </span>
    ),
    size: 120,
    minSize: 100,
    maxSize: 140,
  },
  {
    id: "usePeriod",
    header: "사용 기간",
    cell: ({ row }) => (
      <span className="whitespace-nowrap text-muted-foreground text-sm tabular-nums">
        {formatDateTime(row.original.useStartAt)} ~ {formatDateTime(row.original.useEndAt)}
      </span>
    ),
    enableSorting: false,
    size: 300,
    minSize: 260,
    maxSize: 320,
  },
  {
    accessorKey: "visible",
    header: "노출 여부",
    cell: ({ row }) => (
      <Badge variant={row.original.visible ? "default" : "secondary"}>{row.original.visible ? "노출" : "미노출"}</Badge>
    ),
    size: 100,
    minSize: 100,
    maxSize: 100,
  },
  {
    id: "actions",
    header: () => <div className="text-right">작업</div>,
    cell: ({ row, table }) => {
      const coupon = row.original;
      const meta = table.options.meta as CouponsTableMeta;

      return (
        <div className="text-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label="쿠폰 작업 메뉴">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onSelect={() => meta.onView(coupon)}>상세 보기</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => meta.onEdit(coupon)}>수정</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => meta.onIssue(coupon)}>발급 관리</DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem variant="destructive" onSelect={() => meta.onDelete(coupon)}>
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
