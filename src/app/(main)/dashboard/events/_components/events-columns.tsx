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
import type { EventListItem } from "@/feature/event/domain";
import { eventStatusBadgeVariant, eventStatusLabel } from "@/feature/event/format";
import { formatDateTime } from "@/lib/date";

export interface EventsTableMeta {
  totalElements: number;
  onView: (event: EventListItem) => void;
  onEdit: (event: EventListItem) => void;
  onAnnouncement: (event: EventListItem) => void;
  onWinners: (event: EventListItem) => void;
  onDelete: (event: EventListItem) => void;
}

export const eventsColumns: ColumnDef<EventListItem>[] = [
  {
    accessorKey: "id",
    header: "ID",
    cell: ({ row }) => <span className="tabular-nums">{row.original.id}</span>,
    size: 80,
    minSize: 80,
    maxSize: 80,
  },
  {
    id: "thumbnail",
    header: "썸네일",
    cell: ({ row }) => {
      const file = row.original.file;
      return file ? (
        // biome-ignore lint/performance/noImgElement: CDN 썸네일 URL 미리보기
        <img src={file.url} alt={file.name} className="h-10 w-16 rounded border object-cover" />
      ) : (
        <span className="text-muted-foreground">-</span>
      );
    },
    enableSorting: false,
    size: 100,
    minSize: 100,
    maxSize: 100,
  },
  {
    accessorKey: "name",
    header: "이벤트명",
    cell: ({ row }) => <span className="line-clamp-1 font-medium">{row.original.name}</span>,
    size: 280,
    minSize: 180,
    maxSize: 320,
  },
  {
    accessorKey: "status",
    header: "상태",
    cell: ({ row }) => (
      <Badge variant={eventStatusBadgeVariant(row.original.status)}>{eventStatusLabel(row.original.status)}</Badge>
    ),
    size: 100,
    minSize: 100,
    maxSize: 100,
  },
  {
    id: "period",
    header: "기간",
    cell: ({ row }) => (
      <span className="whitespace-nowrap text-muted-foreground text-sm tabular-nums">
        {formatDateTime(row.original.startAt)} ~ {formatDateTime(row.original.endAt)}
      </span>
    ),
    enableSorting: false,
    size: 300,
    minSize: 260,
    maxSize: 320,
  },
  {
    id: "actions",
    header: () => <div className="text-right">작업</div>,
    cell: ({ row, table }) => {
      const event = row.original;
      const meta = table.options.meta as EventsTableMeta;

      return (
        <div className="text-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label="이벤트 작업 메뉴">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onSelect={() => meta.onView(event)}>상세 보기</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => meta.onEdit(event)}>수정</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => meta.onAnnouncement(event)}>공지 관리</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => meta.onWinners(event)}>당첨자 관리</DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem variant="destructive" onSelect={() => meta.onDelete(event)}>
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
