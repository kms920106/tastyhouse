"use client";
"use no memo";

import type { ColumnDef } from "@tanstack/react-table";
import { MoreHorizontal, Star } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  REVIEW_BLIND_REASON_LABEL,
  REVIEW_BLIND_REQUEST_STATUS_LABEL,
  REVIEW_CONTENT_PREVIEW_MAX,
} from "@/feature/review-blind-request/constants";
import type { ReviewBlindRequestListItem, ReviewBlindRequestStatus } from "@/feature/review-blind-request/domain";
import { formatDateTime } from "@/lib/date";

export interface ReviewBlindRequestsTableMeta {
  totalElements: number;
  onView: (blindRequest: ReviewBlindRequestListItem) => void;
  onApprove: (blindRequest: ReviewBlindRequestListItem) => void;
  onReject: (blindRequest: ReviewBlindRequestListItem) => void;
}

/** 심사 상태 Badge variant — 승인은 강조, 반려는 경고, 대기/취소는 중립. */
function statusBadgeVariant(status: ReviewBlindRequestStatus): "default" | "secondary" | "outline" | "destructive" {
  switch (status) {
    case "APPROVED":
      return "default";
    case "REJECTED":
      return "destructive";
    case "PENDING":
      return "outline";
    default:
      return "secondary";
  }
}

/** 목록 미리보기용 줄임 — 원문은 상세 Sheet 에서 확인한다. */
function truncate(value: string): string {
  if (value.length <= REVIEW_CONTENT_PREVIEW_MAX) return value;
  return `${value.slice(0, REVIEW_CONTENT_PREVIEW_MAX)}…`;
}

export const reviewBlindRequestsColumns: ColumnDef<ReviewBlindRequestListItem>[] = [
  {
    accessorKey: "createdAt",
    header: "요청일",
    cell: ({ row }) => <span className="tabular-nums">{formatDateTime(row.original.createdAt)}</span>,
    enableSorting: false,
    size: 160,
    minSize: 140,
    maxSize: 180,
  },
  {
    accessorKey: "shopName",
    header: "가게",
    cell: ({ row }) => <span className="line-clamp-1">{row.original.shopName}</span>,
    enableSorting: false,
    size: 160,
    minSize: 120,
    maxSize: 200,
  },
  {
    id: "review",
    header: "리뷰",
    cell: ({ row }) => {
      const { reviewTotalRating, reviewContent } = row.original;
      return (
        <div className="flex items-center gap-2">
          <span className="flex shrink-0 items-center gap-1 tabular-nums">
            <Star className="size-3.5 fill-current text-amber-500" />
            {reviewTotalRating.toFixed(1)}
          </span>
          <span className="line-clamp-1 text-muted-foreground">{truncate(reviewContent)}</span>
        </div>
      );
    },
    enableSorting: false,
    size: 320,
    minSize: 240,
    maxSize: 400,
  },
  {
    accessorKey: "reason",
    header: "사유",
    cell: ({ row }) => (
      <Badge variant="outline">
        {row.original.reasonDescription || REVIEW_BLIND_REASON_LABEL[row.original.reason]}
      </Badge>
    ),
    enableSorting: false,
    size: 120,
    minSize: 100,
    maxSize: 140,
  },
  {
    accessorKey: "status",
    header: "상태",
    cell: ({ row }) => (
      <Badge variant={statusBadgeVariant(row.original.status)}>
        {row.original.statusDescription || REVIEW_BLIND_REQUEST_STATUS_LABEL[row.original.status]}
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
      const blindRequest = row.original;
      const meta = table.options.meta as ReviewBlindRequestsTableMeta;
      const isPending = blindRequest.status === "PENDING";

      return (
        <div className="text-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label="게시중단 요청 심사 작업 메뉴">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onSelect={() => meta.onView(blindRequest)}>상세</DropdownMenuItem>
              <DropdownMenuItem disabled={!isPending} onSelect={() => meta.onApprove(blindRequest)}>
                승인
              </DropdownMenuItem>
              <DropdownMenuItem
                disabled={!isPending}
                variant="destructive"
                onSelect={() => meta.onReject(blindRequest)}
              >
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
