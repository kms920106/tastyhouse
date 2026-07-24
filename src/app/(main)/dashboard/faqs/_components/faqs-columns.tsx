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
import type { FaqListItem } from "@/feature/faq/domain";
import { formatDateTime } from "@/lib/date";

export interface FaqsTableMeta {
  totalElements: number;
  categoryNameById: Map<number, string>;
  onView: (faq: FaqListItem) => void;
  onEdit: (faq: FaqListItem) => void;
  onDelete: (faq: FaqListItem) => void;
}

export const faqsColumns: ColumnDef<FaqListItem>[] = [
  {
    accessorKey: "id",
    header: "ID",
    cell: ({ row }) => <span className="tabular-nums">{row.original.id}</span>,
    size: 80,
    minSize: 80,
    maxSize: 80,
  },
  {
    accessorKey: "faqCategoryId",
    header: "카테고리",
    cell: ({ row, table }) => {
      const meta = table.options.meta as FaqsTableMeta;
      const name = meta.categoryNameById.get(row.original.faqCategoryId) ?? "-";
      return <span className="line-clamp-1">{name}</span>;
    },
    size: 160,
    minSize: 120,
    maxSize: 200,
  },
  {
    accessorKey: "question",
    header: "질문",
    cell: ({ row }) => <span className="line-clamp-1 font-medium">{row.original.question}</span>,
    size: 320,
    minSize: 200,
    maxSize: 400,
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
      const faq = row.original;
      const meta = table.options.meta as FaqsTableMeta;

      return (
        <div className="text-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label="FAQ 작업 메뉴">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onSelect={() => meta.onView(faq)}>상세 보기</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => meta.onEdit(faq)}>수정</DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem variant="destructive" onSelect={() => meta.onDelete(faq)}>
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
