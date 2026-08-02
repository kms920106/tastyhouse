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
import type { RankPeriod } from "@/feature/rank/domain";
import { formatDateTime } from "@/lib/date";

export interface RankPeriodsTableMeta {
  onView: (period: RankPeriod) => void;
  onEdit: (period: RankPeriod) => void;
  onManagePrizes: (period: RankPeriod) => void;
  onDelete: (period: RankPeriod) => void;
}

export const rankPeriodsColumns: ColumnDef<RankPeriod>[] = [
  {
    accessorKey: "id",
    header: "ID",
    cell: ({ row }) => <span className="tabular-nums">{row.original.id}</span>,
    size: 80,
    minSize: 80,
    maxSize: 80,
  },
  {
    accessorKey: "startAt",
    header: "시작 일시",
    cell: ({ row }) => <span className="tabular-nums">{formatDateTime(row.original.startAt)}</span>,
    size: 160,
    minSize: 140,
    maxSize: 200,
  },
  {
    accessorKey: "endAt",
    header: "종료 일시",
    cell: ({ row }) => <span className="tabular-nums">{formatDateTime(row.original.endAt)}</span>,
    size: 160,
    minSize: 140,
    maxSize: 200,
  },
  {
    accessorKey: "visible",
    header: "노출 여부",
    cell: ({ row }) => (
      <Badge variant={row.original.visible ? "default" : "secondary"}>{row.original.visible ? "노출" : "미노출"}</Badge>
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
      const period = row.original;
      const meta = table.options.meta as RankPeriodsTableMeta;

      return (
        <div className="text-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label="랭킹 기간 작업 메뉴">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onSelect={() => meta.onView(period)}>상세 보기</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => meta.onEdit(period)}>수정</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => meta.onManagePrizes(period)}>경품 관리</DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem variant="destructive" onSelect={() => meta.onDelete(period)}>
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
