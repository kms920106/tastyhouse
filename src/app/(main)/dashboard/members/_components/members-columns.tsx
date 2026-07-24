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
import type { MemberListItem } from "@/feature/member/domain";
import { genderLabel, memberGradeLabel, memberStatusBadgeVariant, memberStatusLabel } from "@/feature/member/format";
import { formatDateTime } from "@/lib/date";

export interface MembersTableMeta {
  totalElements: number;
  onView: (member: MemberListItem) => void;
  onManagePoints: (member: MemberListItem) => void;
  onSuspend: (member: MemberListItem) => void;
  onActivate: (member: MemberListItem) => void;
  onWithdraw: (member: MemberListItem) => void;
}

export const membersColumns: ColumnDef<MemberListItem>[] = [
  {
    accessorKey: "id",
    header: "ID",
    cell: ({ row }) => <span className="tabular-nums">{row.original.id}</span>,
    size: 70,
    minSize: 70,
    maxSize: 70,
  },
  {
    accessorKey: "nickname",
    header: "닉네임",
    cell: ({ row }) => <span className="line-clamp-1 font-medium">{row.original.nickname}</span>,
    size: 140,
    minSize: 100,
    maxSize: 180,
  },
  {
    accessorKey: "username",
    header: "아이디",
    cell: ({ row }) => <span className="text-muted-foreground text-sm">{row.original.username}</span>,
    size: 140,
    minSize: 100,
    maxSize: 180,
  },
  {
    accessorKey: "fullName",
    header: "실명",
    cell: ({ row }) => <span>{row.original.fullName}</span>,
    size: 100,
    minSize: 80,
    maxSize: 140,
  },
  {
    accessorKey: "phoneNumber",
    header: "휴대폰번호",
    cell: ({ row }) => <span className="tabular-nums">{row.original.phoneNumber}</span>,
    enableSorting: false,
    size: 140,
    minSize: 120,
    maxSize: 160,
  },
  {
    accessorKey: "gender",
    header: "성별",
    cell: ({ row }) => <span>{genderLabel(row.original.gender)}</span>,
    size: 70,
    minSize: 70,
    maxSize: 90,
  },
  {
    accessorKey: "memberGrade",
    header: "등급",
    cell: ({ row }) => <Badge variant="secondary">{memberGradeLabel(row.original.memberGrade)}</Badge>,
    size: 100,
    minSize: 90,
    maxSize: 120,
  },
  {
    accessorKey: "memberStatus",
    header: "상태",
    cell: ({ row }) => (
      <Badge variant={memberStatusBadgeVariant(row.original.memberStatus)}>
        {memberStatusLabel(row.original.memberStatus)}
      </Badge>
    ),
    size: 90,
    minSize: 90,
    maxSize: 100,
  },
  {
    accessorKey: "createdAt",
    header: "가입일시",
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
      const member = row.original;
      const meta = table.options.meta as MembersTableMeta;
      const status = member.memberStatus;

      return (
        <div className="text-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label="회원 작업 메뉴">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onSelect={() => meta.onView(member)}>상세 보기</DropdownMenuItem>
              <DropdownMenuItem onSelect={() => meta.onManagePoints(member)}>포인트 관리</DropdownMenuItem>
              {status === "ACTIVE" || status === "SUSPENDED" ? <DropdownMenuSeparator /> : null}
              {status === "ACTIVE" ? (
                <DropdownMenuItem onSelect={() => meta.onSuspend(member)}>정지</DropdownMenuItem>
              ) : null}
              {status === "SUSPENDED" ? (
                <DropdownMenuItem onSelect={() => meta.onActivate(member)}>정지 해제</DropdownMenuItem>
              ) : null}
              {status === "ACTIVE" || status === "SUSPENDED" ? (
                <DropdownMenuItem variant="destructive" onSelect={() => meta.onWithdraw(member)}>
                  강제 탈퇴
                </DropdownMenuItem>
              ) : null}
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
