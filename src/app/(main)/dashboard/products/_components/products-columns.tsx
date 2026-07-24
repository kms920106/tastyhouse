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
import type { ProductListItem } from "@/feature/product/domain";
import { formatDiscountRate, formatPrice } from "@/feature/product/format";

export interface ProductsTableMeta {
  totalElements: number;
  onView: (product: ProductListItem) => void;
  onEdit: (product: ProductListItem) => void;
  onManageOptions: (product: ProductListItem) => void;
  onManageImages: (product: ProductListItem) => void;
  onSoldOut: (product: ProductListItem) => void;
  onDeactivate: (product: ProductListItem) => void;
}

export const productsColumns: ColumnDef<ProductListItem>[] = [
  {
    accessorKey: "id",
    header: "ID",
    cell: ({ row }) => <span className="tabular-nums">{row.original.id}</span>,
    size: 80,
    minSize: 80,
    maxSize: 80,
  },
  {
    accessorKey: "shopName",
    header: "매장",
    cell: ({ row }) => <span className="line-clamp-1">{row.original.shopName}</span>,
    size: 160,
    minSize: 120,
    maxSize: 200,
  },
  {
    accessorKey: "name",
    header: "상품명",
    cell: ({ row }) => <span className="line-clamp-1 font-medium">{row.original.name}</span>,
    size: 240,
    minSize: 160,
    maxSize: 300,
  },
  {
    id: "price",
    header: "가격",
    cell: ({ row }) => (
      <div className="whitespace-nowrap tabular-nums">
        <span>{formatPrice(row.original.originalPrice)}</span>
        {row.original.discountPrice != null ? (
          <span className="ml-1 text-muted-foreground text-xs">→ {formatPrice(row.original.discountPrice)}</span>
        ) : null}
        {row.original.discountRate != null ? (
          <span className="ml-1 text-destructive text-xs">({formatDiscountRate(row.original.discountRate)})</span>
        ) : null}
      </div>
    ),
    enableSorting: false,
    size: 200,
    minSize: 160,
    maxSize: 240,
  },
  {
    id: "badges",
    header: "상태",
    cell: ({ row }) => (
      <div className="flex flex-wrap gap-1">
        {row.original.representative ? <Badge variant="outline">대표</Badge> : null}
        <Badge variant={row.original.soldOut ? "destructive" : "secondary"}>
          {row.original.soldOut ? "품절" : "판매중"}
        </Badge>
        <Badge variant={row.original.visible ? "default" : "secondary"}>
          {row.original.visible ? "노출" : "미노출"}
        </Badge>
      </div>
    ),
    enableSorting: false,
    size: 200,
    minSize: 160,
    maxSize: 240,
  },
  {
    accessorKey: "sort",
    header: "정렬",
    cell: ({ row }) => <span className="tabular-nums">{row.original.sort}</span>,
    size: 80,
    minSize: 80,
    maxSize: 80,
  },
  {
    id: "actions",
    header: () => <div className="text-right">작업</div>,
    cell: ({ row, table }) => {
      const product = row.original;
      const meta = table.options.meta as ProductsTableMeta;

      return (
        <div className="text-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label="상품 작업 메뉴">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onSelect={() => meta.onView(product)}>상세 보기</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => meta.onEdit(product)}>수정</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => meta.onManageOptions(product)}>옵션 관리</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => meta.onManageImages(product)}>이미지 관리</DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem onSelect={() => meta.onSoldOut(product)}>품절 처리</DropdownMenuItem>
              <DropdownMenuItem variant="destructive" onSelect={() => meta.onDeactivate(product)}>
                비활성화
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
