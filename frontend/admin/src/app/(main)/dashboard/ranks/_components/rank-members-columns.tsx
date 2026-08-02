"use client";
"use no memo";

import type { ColumnDef } from "@tanstack/react-table";

import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import type { RankMember } from "@/feature/rank/domain";
import { getInitials } from "@/lib/utils";

const GRADE_BADGE_VARIANT: Record<string, "default" | "secondary" | "outline"> = {
  GOLD: "default",
  SILVER: "secondary",
  BRONZE: "outline",
};

export const rankMembersColumns: ColumnDef<RankMember>[] = [
  {
    accessorKey: "rankNo",
    header: "순위",
    cell: ({ row }) => <span className="tabular-nums">{row.original.rankNo}</span>,
    size: 80,
    minSize: 80,
    maxSize: 80,
  },
  {
    id: "member",
    header: "회원",
    cell: ({ row }) => (
      <div className="flex items-center gap-2">
        <Avatar size="sm">
          <AvatarImage src={row.original.profileImageUrl ?? undefined} alt={row.original.nickname} />
          <AvatarFallback>{getInitials(row.original.nickname)}</AvatarFallback>
        </Avatar>
        <span className="line-clamp-1 font-medium">{row.original.nickname}</span>
      </div>
    ),
    size: 240,
    minSize: 160,
    maxSize: 300,
  },
  {
    accessorKey: "reviewCount",
    header: "리뷰 수",
    cell: ({ row }) => <span className="tabular-nums">{row.original.reviewCount}</span>,
    size: 120,
    minSize: 100,
    maxSize: 160,
  },
  {
    accessorKey: "grade",
    header: "등급",
    cell: ({ row }) => (
      <Badge variant={GRADE_BADGE_VARIANT[row.original.grade] ?? "secondary"}>{row.original.grade}</Badge>
    ),
    enableSorting: false,
    size: 120,
    minSize: 100,
    maxSize: 160,
  },
];
