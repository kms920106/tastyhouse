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
import type { PartnershipRequestListItem } from "@/feature/partnership-request/domain";
import { partnershipStatusBadgeVariant, partnershipStatusLabel } from "@/feature/partnership-request/format";
import { formatDateTime } from "@/lib/date";

export interface PartnershipRequestsTableMeta {
  totalElements: number;
  onView: (partnershipRequest: PartnershipRequestListItem) => void;
  onChangeStatus: (partnershipRequest: PartnershipRequestListItem) => void;
  onDelete: (partnershipRequest: PartnershipRequestListItem) => void;
}

export const partnershipRequestsColumns: ColumnDef<PartnershipRequestListItem>[] = [
  {
    accessorKey: "businessName",
    header: "상호명",
    cell: ({ row }) => <span className="line-clamp-1 font-medium">{row.original.businessName}</span>,
    size: 200,
    minSize: 160,
    maxSize: 260,
  },
  {
    accessorKey: "contactName",
    header: "담당자명",
    cell: ({ row }) => <span>{row.original.contactName}</span>,
    size: 100,
    minSize: 80,
    maxSize: 140,
  },
  {
    accessorKey: "contactPhone",
    header: "연락처",
    cell: ({ row }) => <span className="tabular-nums">{row.original.contactPhone}</span>,
    size: 140,
    minSize: 120,
    maxSize: 160,
  },
  {
    accessorKey: "status",
    header: "처리상태",
    cell: ({ row }) => (
      <Badge variant={partnershipStatusBadgeVariant(row.original.status)}>
        {partnershipStatusLabel(row.original.status)}
      </Badge>
    ),
    size: 90,
    minSize: 90,
    maxSize: 100,
  },
  {
    accessorKey: "consultationRequestedAt",
    header: "상담요청일시",
    cell: ({ row }) => (
      <span className="whitespace-nowrap text-muted-foreground text-sm tabular-nums">
        {formatDateTime(row.original.consultationRequestedAt)}
      </span>
    ),
    enableSorting: false,
    size: 160,
    minSize: 140,
    maxSize: 180,
  },
  {
    accessorKey: "createdAt",
    header: "접수일시",
    cell: ({ row }) => (
      <span className="whitespace-nowrap text-muted-foreground text-sm tabular-nums">
        {formatDateTime(row.original.createdAt)}
      </span>
    ),
    enableSorting: false,
    size: 160,
    minSize: 140,
    maxSize: 180,
  },
  {
    id: "actions",
    header: () => <div className="text-right">작업</div>,
    cell: ({ row, table }) => {
      const partnershipRequest = row.original;
      const meta = table.options.meta as PartnershipRequestsTableMeta;

      return (
        <div className="text-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label="제휴 신청 작업 메뉴">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onSelect={() => meta.onView(partnershipRequest)}>상세 보기</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => meta.onChangeStatus(partnershipRequest)}>상태 변경</DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem variant="destructive" onSelect={() => meta.onDelete(partnershipRequest)}>
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
