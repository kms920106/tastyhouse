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
import { DELIVERY_AREA_ADJUSTMENT_STATUS_LABEL } from "@/feature/shop/constants";
import type { DeliveryAreaAdjustmentListItem, DeliveryAreaAdjustmentStatus } from "@/feature/shop/domain";
import { formatDateTime } from "@/lib/date";

export interface ShopDeliveryAreaAdjustmentsTableMeta {
  totalElements: number;
  onView: (request: DeliveryAreaAdjustmentListItem) => void;
  onChangeStatus: (request: DeliveryAreaAdjustmentListItem) => void;
  onReject: (request: DeliveryAreaAdjustmentListItem) => void;
}

function statusBadgeVariant(status: DeliveryAreaAdjustmentStatus) {
  if (status === "COMPLETED") return "default" as const;
  if (status === "REJECTED") return "destructive" as const;
  if (status === "IN_PROGRESS") return "secondary" as const;
  return "outline" as const;
}

export const shopDeliveryAreaAdjustmentsColumns: ColumnDef<DeliveryAreaAdjustmentListItem>[] = [
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
    header: "가게",
    cell: ({ row }) => <span className="line-clamp-1">{row.original.shopName}</span>,
    enableSorting: false,
    size: 160,
    minSize: 120,
    maxSize: 220,
  },
  {
    accessorKey: "counterpartShopName",
    header: "상대 가맹점",
    cell: ({ row }) => <span className="line-clamp-1">{row.original.counterpartShopName}</span>,
    enableSorting: false,
    size: 160,
    minSize: 120,
    maxSize: 220,
  },
  {
    accessorKey: "franchiseName",
    header: "가맹본부",
    cell: ({ row }) => <span className="line-clamp-1">{row.original.franchiseName}</span>,
    enableSorting: false,
    size: 160,
    minSize: 120,
    maxSize: 220,
  },
  {
    accessorKey: "status",
    header: "상태",
    cell: ({ row }) => {
      const status = row.original.status;
      return <Badge variant={statusBadgeVariant(status)}>{DELIVERY_AREA_ADJUSTMENT_STATUS_LABEL[status]}</Badge>;
    },
    enableSorting: false,
    size: 110,
    minSize: 100,
    maxSize: 130,
  },
  {
    accessorKey: "createdAt",
    header: "접수일시",
    cell: ({ row }) => <span className="whitespace-nowrap tabular-nums">{formatDateTime(row.original.createdAt)}</span>,
    enableSorting: false,
    size: 150,
    minSize: 140,
    maxSize: 180,
  },
  {
    id: "actions",
    header: () => <div className="text-right">작업</div>,
    cell: ({ row, table }) => {
      const request = row.original;
      const meta = table.options.meta as ShopDeliveryAreaAdjustmentsTableMeta;
      // 종결된 신청(조정 완료·반려)은 더 이상 전이시킬 수 없다.
      const canChangeStatus = request.status === "PENDING" || request.status === "IN_PROGRESS";
      const canReject = request.status !== "COMPLETED" && request.status !== "REJECTED";

      return (
        <div className="text-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label="배달지역 조정 신청 작업 메뉴">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onSelect={() => meta.onView(request)}>상세 보기</DropdownMenuItem>
              <DropdownMenuItem disabled={!canChangeStatus} onSelect={() => meta.onChangeStatus(request)}>
                상태 변경
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem disabled={!canReject} variant="destructive" onSelect={() => meta.onReject(request)}>
                반려
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
