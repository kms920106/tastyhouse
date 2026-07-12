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
import type { BannerListItem } from "@/feature/banner/domain";
import { BANNER_TYPE_LABEL } from "@/feature/banner/message";
import { formatDateTime } from "@/lib/date";

export interface BannersTableMeta {
  totalElements: number;
  onView: (banner: BannerListItem) => void;
  onEdit: (banner: BannerListItem) => void;
  onDelete: (banner: BannerListItem) => void;
}

export const bannersColumns: ColumnDef<BannerListItem>[] = [
  {
    accessorKey: "id",
    header: "ID",
    cell: ({ row }) => <span className="tabular-nums">{row.original.id}</span>,
    size: 60,
    minSize: 60,
    maxSize: 60,
  },
  {
    accessorKey: "type",
    header: "유형",
    cell: ({ row }) => <Badge variant="outline">{BANNER_TYPE_LABEL[row.original.type]}</Badge>,
    size: 90,
    minSize: 90,
    maxSize: 90,
  },
  {
    id: "image",
    header: "이미지",
    cell: ({ row }) =>
      row.original.file ? (
        <a
          href={row.original.file.url}
          target="_blank"
          rel="noopener noreferrer"
          className="text-primary underline-offset-4 underline"
        >
          미리 보기
        </a>
      ) : (
        <span className="text-muted-foreground">-</span>
      ),
    size: 100,
    minSize: 100,
    maxSize: 100,
  },
  {
    accessorKey: "title",
    header: "제목",
    cell: ({ row }) => <span className="line-clamp-1 font-medium">{row.original.title ?? "-"}</span>,
    size: 260,
    minSize: 160,
  },
  {
    id: "period",
    header: "노출기간",
    cell: ({ row }) => (
      <span className="whitespace-nowrap tabular-nums">
        {formatDateTime(row.original.startDate)} ~ {formatDateTime(row.original.endDate)}
      </span>
    ),
    size: 260,
    minSize: 260,
  },
  {
    accessorKey: "sort",
    header: "정렬",
    cell: ({ row }) => <span className="tabular-nums">{row.original.sort}</span>,
    size: 70,
    minSize: 70,
    maxSize: 70,
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
    id: "actions",
    header: () => <div className="text-right">작업</div>,
    cell: ({ row, table }) => {
      const banner = row.original;
      const meta = table.options.meta as BannersTableMeta;

      return (
        <div className="text-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label="배너 작업 메뉴">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onSelect={() => meta.onView(banner)}>상세 보기</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => meta.onEdit(banner)}>수정</DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem variant="destructive" onSelect={() => meta.onDelete(banner)}>
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
