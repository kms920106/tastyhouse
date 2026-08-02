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
import type { ReviewListItem } from "@/feature/review/domain";
import { formatRating } from "@/feature/review/format";
import { formatDateTime } from "@/lib/date";

export interface ReviewsTableMeta {
  totalElements: number;
  onView: (review: ReviewListItem) => void;
  onManageComments: (review: ReviewListItem) => void;
  onToggleHidden: (review: ReviewListItem) => void;
  onDelete: (review: ReviewListItem) => void;
}

export const reviewsColumns: ColumnDef<ReviewListItem>[] = [
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
    header: "매장 ID",
    cell: ({ row }) => <span className="tabular-nums">{row.original.shopId}</span>,
    size: 100,
    minSize: 80,
    maxSize: 120,
  },
  {
    accessorKey: "productId",
    header: "상품 ID",
    cell: ({ row }) => <span className="tabular-nums">{row.original.productId}</span>,
    size: 100,
    minSize: 80,
    maxSize: 120,
  },
  {
    accessorKey: "memberNickname",
    header: "작성자",
    cell: ({ row }) => <span className="line-clamp-1">{row.original.memberNickname}</span>,
    size: 140,
    minSize: 100,
    maxSize: 180,
  },
  {
    accessorKey: "totalRating",
    header: "총점",
    cell: ({ row }) => <span className="tabular-nums">{formatRating(row.original.totalRating)}</span>,
    size: 80,
    minSize: 60,
    maxSize: 100,
  },
  {
    accessorKey: "content",
    header: "내용",
    cell: ({ row }) => <span className="line-clamp-1">{row.original.content}</span>,
    enableSorting: false,
    size: 280,
    minSize: 180,
    maxSize: 360,
  },
  {
    accessorKey: "hidden",
    header: "숨김 여부",
    cell: ({ row }) => (
      <Badge variant={row.original.hidden ? "destructive" : "default"}>{row.original.hidden ? "숨김" : "노출"}</Badge>
    ),
    enableSorting: false,
    size: 100,
    minSize: 100,
    maxSize: 120,
  },
  {
    accessorKey: "createdAt",
    header: "작성일시",
    cell: ({ row }) => <span className="tabular-nums">{formatDateTime(row.original.createdAt)}</span>,
    size: 160,
    minSize: 140,
    maxSize: 200,
  },
  {
    id: "actions",
    header: () => <div className="text-right">작업</div>,
    cell: ({ row, table }) => {
      const review = row.original;
      const meta = table.options.meta as ReviewsTableMeta;

      return (
        <div className="text-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label="리뷰 작업 메뉴">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onSelect={() => meta.onView(review)}>상세 보기</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => meta.onManageComments(review)}>댓글 관리</DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem onSelect={() => meta.onToggleHidden(review)}>
                {review.hidden ? "노출 전환" : "숨김 전환"}
              </DropdownMenuItem>
              <DropdownMenuItem variant="destructive" onSelect={() => meta.onDelete(review)}>
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
