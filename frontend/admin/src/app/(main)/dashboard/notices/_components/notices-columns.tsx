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
import type { NoticeListItem } from "@/feature/notice/domain";
import { formatDateTime } from "@/lib/date";

export interface NoticesTableMeta {
  totalElements: number;
  onView: (notice: NoticeListItem) => void;
  onEdit: (notice: NoticeListItem) => void;
  onDelete: (notice: NoticeListItem) => void;
}

export const noticesColumns: ColumnDef<NoticeListItem>[] = [
  {
    accessorKey: "id",
    header: "ID",
    cell: ({ row }) => <span className="tabular-nums">{row.original.id}</span>,
    size: 80,
    minSize: 80,
    maxSize: 80,
  },
  {
    accessorKey: "title",
    header: "제목",
    cell: ({ row }) => <span className="line-clamp-1 font-medium">{row.original.title}</span>,
    size: 320,
    minSize: 200,
    maxSize: 360,
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
      const notice = row.original;
      const meta = table.options.meta as NoticesTableMeta;

      return (
        <div className="text-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label="공지 작업 메뉴">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onSelect={() => meta.onView(notice)}>상세 보기</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => meta.onEdit(notice)}>수정</DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem variant="destructive" onSelect={() => meta.onDelete(notice)}>
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
