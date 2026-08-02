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
import type { ShopListItem } from "@/feature/shop/domain";

export interface ShopsTableMeta {
  totalElements: number;
  onView: (shop: ShopListItem) => void;
  onEdit: (shop: ShopListItem) => void;
  onClose: (shop: ShopListItem) => void;
}

export const shopsColumns: ColumnDef<ShopListItem>[] = [
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
    header: "가게명",
    cell: ({ row }) => <span className="line-clamp-1 font-medium">{row.original.name}</span>,
    size: 200,
    minSize: 140,
    maxSize: 260,
  },
  {
    accessorKey: "stationName",
    header: "지하철역",
    cell: ({ row }) => <span className="line-clamp-1">{row.original.stationName}</span>,
    size: 120,
    minSize: 100,
    maxSize: 160,
  },
  {
    accessorKey: "roadAddress",
    header: "주소",
    cell: ({ row }) => <span className="line-clamp-1">{row.original.roadAddress}</span>,
    size: 280,
    minSize: 200,
    maxSize: 360,
  },
  {
    accessorKey: "rating",
    header: "평점",
    cell: ({ row }) => <span className="tabular-nums">{row.original.rating ?? "-"}</span>,
    size: 80,
    minSize: 80,
    maxSize: 80,
  },
  {
    id: "status",
    header: "상태",
    cell: ({ row }) => (
      <Badge variant={row.original.permanentlyClosed ? "destructive" : "default"}>
        {row.original.permanentlyClosed ? "폐업" : "영업중"}
      </Badge>
    ),
    enableSorting: false,
    size: 100,
    minSize: 100,
    maxSize: 120,
  },
  {
    id: "actions",
    header: () => <div className="text-right">작업</div>,
    cell: ({ row, table }) => {
      const shop = row.original;
      const meta = table.options.meta as ShopsTableMeta;

      return (
        <div className="text-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label="가게 작업 메뉴">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onSelect={() => meta.onView(shop)}>상세 보기</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => meta.onEdit(shop)}>수정</DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                variant="destructive"
                disabled={shop.permanentlyClosed}
                onSelect={() => meta.onClose(shop)}
              >
                폐업 처리
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
