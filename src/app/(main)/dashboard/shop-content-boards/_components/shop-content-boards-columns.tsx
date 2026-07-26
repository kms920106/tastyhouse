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
import { CONTENT_BOARD_CONTENT_TYPE_LABEL, CONTENT_BOARD_TOPIC_LABEL } from "@/feature/shop/constants";
import type { ContentBoard } from "@/feature/shop/domain";
import { formatDateTime } from "@/lib/date";

export interface ShopContentBoardsTableMeta {
  totalElements: number;
  onToggleHidden: (contentBoard: ContentBoard) => void;
  onDelete: (contentBoard: ContentBoard) => void;
}

export const shopContentBoardsColumns: ColumnDef<ContentBoard>[] = [
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
    accessorKey: "contentType",
    header: "유형",
    cell: ({ row }) => <Badge variant="outline">{CONTENT_BOARD_CONTENT_TYPE_LABEL[row.original.contentType]}</Badge>,
    enableSorting: false,
    size: 100,
    minSize: 80,
    maxSize: 120,
  },
  {
    accessorKey: "topic",
    header: "주제",
    cell: ({ row }) => <Badge variant="secondary">{CONTENT_BOARD_TOPIC_LABEL[row.original.topic]}</Badge>,
    enableSorting: false,
    size: 120,
    minSize: 100,
    maxSize: 140,
  },
  {
    id: "preview",
    header: "미리보기",
    cell: ({ row }) => {
      const { imageUrl, youtubeUrl } = row.original;
      if (imageUrl) {
        return (
          <a href={imageUrl} target="_blank" rel="noreferrer" className="block size-10">
            {/* biome-ignore lint/performance/noImgElement: 목록 썸네일 미리보기 */}
            <img src={imageUrl} alt="미리보기" className="size-10 rounded-md border object-cover" />
          </a>
        );
      }
      if (youtubeUrl) {
        return (
          <a href={youtubeUrl} target="_blank" rel="noreferrer" className="text-primary text-sm underline">
            영상 보기
          </a>
        );
      }
      return <span className="text-muted-foreground">-</span>;
    },
    enableSorting: false,
    size: 100,
    minSize: 80,
    maxSize: 120,
  },
  {
    accessorKey: "description",
    header: "설명",
    cell: ({ row }) => <span className="line-clamp-1">{row.original.description}</span>,
    enableSorting: false,
    size: 240,
    minSize: 160,
    maxSize: 320,
  },
  {
    accessorKey: "hidden",
    header: "노출 상태",
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
      const contentBoard = row.original;
      const meta = table.options.meta as ShopContentBoardsTableMeta;

      return (
        <div className="text-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label="콘텐츠보드 작업 메뉴">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onSelect={() => meta.onToggleHidden(contentBoard)}>
                {contentBoard.hidden ? "노출 전환" : "숨김 전환"}
              </DropdownMenuItem>
              <DropdownMenuItem variant="destructive" onSelect={() => meta.onDelete(contentBoard)}>
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
