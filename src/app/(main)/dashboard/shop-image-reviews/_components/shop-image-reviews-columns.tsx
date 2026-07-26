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
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { SHOP_IMAGE_CHANGE_STATUS_LABEL, SHOP_IMAGE_TYPE_LABEL } from "@/feature/shop/constants";
import type { ShopImageChangeRequest } from "@/feature/shop/domain";

export interface ShopImageReviewsTableMeta {
  totalElements: number;
  onApprove: (request: ShopImageChangeRequest) => void;
  onReject: (request: ShopImageChangeRequest) => void;
}

export const shopImageReviewsColumns: ColumnDef<ShopImageChangeRequest>[] = [
  {
    accessorKey: "id",
    header: "ID",
    cell: ({ row }) => <span className="tabular-nums">{row.original.id}</span>,
    size: 80,
    minSize: 80,
    maxSize: 80,
  },
  {
    accessorKey: "shopId",
    header: "가게 ID",
    cell: ({ row }) => <span className="tabular-nums">{row.original.shopId}</span>,
    size: 100,
    minSize: 80,
    maxSize: 120,
  },
  {
    accessorKey: "imageType",
    header: "이미지 유형",
    cell: ({ row }) => <Badge variant="outline">{SHOP_IMAGE_TYPE_LABEL[row.original.imageType]}</Badge>,
    enableSorting: false,
    size: 120,
    minSize: 100,
    maxSize: 140,
  },
  {
    id: "preview",
    header: "미리보기",
    cell: ({ row }) => {
      const { imageUrl } = row.original;
      if (!imageUrl) return <span className="text-muted-foreground">-</span>;
      return (
        <a href={imageUrl} target="_blank" rel="noreferrer" className="block size-10">
          {/* biome-ignore lint/performance/noImgElement: 목록 썸네일 미리보기 */}
          <img src={imageUrl} alt="미리보기" className="size-10 rounded-md border object-cover" />
        </a>
      );
    },
    enableSorting: false,
    size: 100,
    minSize: 80,
    maxSize: 120,
  },
  {
    accessorKey: "status",
    header: "상태",
    cell: ({ row }) => {
      const status = row.original.status;
      return (
        <Badge variant={status === "APPROVED" ? "default" : status === "REJECTED" ? "destructive" : "secondary"}>
          {SHOP_IMAGE_CHANGE_STATUS_LABEL[status]}
        </Badge>
      );
    },
    enableSorting: false,
    size: 100,
    minSize: 100,
    maxSize: 120,
  },
  {
    accessorKey: "rejectReason",
    header: "반려 사유",
    cell: ({ row }) => <span className="line-clamp-1">{row.original.rejectReason ?? "-"}</span>,
    enableSorting: false,
    size: 240,
    minSize: 160,
    maxSize: 320,
  },
  {
    id: "actions",
    header: () => <div className="text-right">작업</div>,
    cell: ({ row, table }) => {
      const request = row.original;
      const meta = table.options.meta as ShopImageReviewsTableMeta;
      const isPending = request.status === "PENDING";

      return (
        <div className="text-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label="이미지 검수 작업 메뉴">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem disabled={!isPending} onSelect={() => meta.onApprove(request)}>
                승인
              </DropdownMenuItem>
              <DropdownMenuItem disabled={!isPending} variant="destructive" onSelect={() => meta.onReject(request)}>
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
