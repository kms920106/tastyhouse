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
import {
  BUG_CATEGORY_LABEL,
  BUG_PRIORITY_BADGE_VARIANT,
  BUG_PRIORITY_LABEL,
  BUG_STATUS_BADGE_VARIANT,
  BUG_STATUS_LABEL,
} from "@/feature/bug-report/constants";
import type { BugReportListItem } from "@/feature/bug-report/domain";
import { formatDateTime } from "@/lib/date";

export interface BugReportsTableMeta {
  totalElements: number;
  onView: (bugReport: BugReportListItem) => void;
}

export const bugReportsColumns: ColumnDef<BugReportListItem>[] = [
  {
    accessorKey: "id",
    header: "ID",
    cell: ({ row }) => <span className="tabular-nums">{row.original.id}</span>,
    size: 72,
    minSize: 72,
    maxSize: 72,
  },
  {
    accessorKey: "member",
    header: "회원",
    cell: ({ row }) => <span className="line-clamp-1">{row.original.member?.nickname ?? "-"}</span>,
    size: 140,
    minSize: 120,
    maxSize: 160,
  },
  {
    accessorKey: "device",
    header: "기기",
    cell: ({ row }) => <span className="line-clamp-1 text-muted-foreground">{row.original.device}</span>,
    size: 200,
    minSize: 160,
    maxSize: 220,
  },
  {
    accessorKey: "title",
    header: "제목",
    cell: ({ row }) => <span className="line-clamp-1 font-medium">{row.original.title}</span>,
    size: 280,
    minSize: 200,
    maxSize: 320,
  },
  {
    accessorKey: "status",
    header: "상태",
    cell: ({ row }) => (
      <Badge variant={BUG_STATUS_BADGE_VARIANT[row.original.status]}>{BUG_STATUS_LABEL[row.original.status]}</Badge>
    ),
    size: 90,
    minSize: 90,
    maxSize: 90,
  },
  {
    accessorKey: "category",
    header: "분류",
    cell: ({ row }) => (
      <span className="whitespace-nowrap">
        {row.original.category ? BUG_CATEGORY_LABEL[row.original.category] : "-"}
      </span>
    ),
    size: 90,
    minSize: 90,
    maxSize: 90,
  },
  {
    accessorKey: "priority",
    header: "우선순위",
    cell: ({ row }) =>
      row.original.priority ? (
        <Badge variant={BUG_PRIORITY_BADGE_VARIANT[row.original.priority]}>
          {BUG_PRIORITY_LABEL[row.original.priority]}
        </Badge>
      ) : (
        <span>-</span>
      ),
    size: 90,
    minSize: 90,
    maxSize: 90,
  },
  {
    accessorKey: "imageCount",
    header: "이미지",
    cell: ({ row }) => <span className="tabular-nums">{row.original.imageCount}</span>,
    size: 80,
    minSize: 80,
    maxSize: 80,
  },
  {
    accessorKey: "createdAt",
    header: "생성일시",
    cell: ({ row }) => <span className="whitespace-nowrap tabular-nums">{formatDateTime(row.original.createdAt)}</span>,
    size: 180,
    minSize: 180,
    maxSize: 180,
  },
  {
    id: "actions",
    header: () => <div className="text-right">작업</div>,
    cell: ({ row, table }) => {
      const bugReport = row.original;
      const meta = table.options.meta as BugReportsTableMeta;

      return (
        <div className="text-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label="버그 제보 작업 메뉴">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onSelect={() => meta.onView(bugReport)}>상세 보기</DropdownMenuItem>
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
