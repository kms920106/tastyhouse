"use client";
"use no memo";

import type { ColumnDef } from "@tanstack/react-table";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import type { ShopRiderGuideListItem } from "@/feature/shop/domain";
import { SHOP_RIDER_GUIDE_ADMIN_COPY } from "@/feature/shop/message";
import { formatDateTime } from "@/lib/date";

export interface ShopRiderGuidesTableMeta {
  totalElements: number;
  onSelect: (riderGuide: ShopRiderGuideListItem) => void;
}

export const shopRiderGuidesColumns: ColumnDef<ShopRiderGuideListItem>[] = [
  {
    accessorKey: "shopName",
    header: "가게명",
    cell: ({ row, table }) => {
      const meta = table.options.meta as ShopRiderGuidesTableMeta;
      return (
        <Button
          variant="link"
          className="h-auto justify-start p-0 text-left"
          onClick={() => meta.onSelect(row.original)}
        >
          <span className="line-clamp-1">{row.original.shopName}</span>
        </Button>
      );
    },
    enableSorting: false,
    size: 180,
    minSize: 140,
    maxSize: 240,
  },
  {
    accessorKey: "visitGuide",
    header: "안내 문구",
    // summary 는 CSS 로 한 줄 truncate 한다 — JS 로 자르면 화면 폭에 따라 이중으로 잘린다.
    cell: ({ row }) => <span className="line-clamp-1">{row.original.visitGuide ?? "-"}</span>,
    enableSorting: false,
    size: 320,
    minSize: 200,
    maxSize: 480,
  },
  {
    accessorKey: "hasPickupLocation",
    header: "픽업 위치",
    cell: ({ row }) =>
      row.original.hasPickupLocation ? (
        <Badge variant="secondary">{SHOP_RIDER_GUIDE_ADMIN_COPY.PICKUP_SET_LABEL}</Badge>
      ) : (
        <Badge variant="outline">{SHOP_RIDER_GUIDE_ADMIN_COPY.PICKUP_FALLBACK_LABEL}</Badge>
      ),
    enableSorting: false,
    size: 120,
    minSize: 110,
    maxSize: 160,
  },
  {
    accessorKey: "updatedAt",
    header: "최근 변경",
    cell: ({ row }) => <span className="tabular-nums">{formatDateTime(row.original.updatedAt)}</span>,
    enableSorting: false,
    size: 160,
    minSize: 140,
    maxSize: 200,
  },
];
