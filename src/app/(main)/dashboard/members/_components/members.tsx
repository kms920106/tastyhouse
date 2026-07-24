"use client";
"use no memo";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { getCoreRowModel, type PaginationState, useReactTable } from "@tanstack/react-table";
import { Search, X } from "lucide-react";

import type { ApiPagination } from "@/api/shared/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { MEMBER_GRADE_OPTIONS, MEMBER_STATUS_OPTIONS } from "@/feature/member/constants";
import type { MemberGrade, MemberListItem, MemberStatus } from "@/feature/member/domain";
import { MEMBER_PAGE_COPY } from "@/feature/member/message";

import { ActivateMemberDialog } from "./activate-member-dialog";
import { MemberDetailSheet } from "./member-detail-sheet";
import { MemberPointSheet } from "./member-point-sheet";
import { type MembersTableMeta, membersColumns } from "./members-columns";
import { MembersTable } from "./members-table";
import { SuspendMemberDialog } from "./suspend-member-dialog";
import { WithdrawMemberDialog } from "./withdraw-member-dialog";

interface Props {
  members: MemberListItem[];
  pagination: ApiPagination;
  initialNickname?: string;
  initialUsername?: string;
  initialPhone?: string;
  initialStatus?: MemberStatus;
  initialGrade?: MemberGrade;
}

export function Members({
  members,
  pagination,
  initialNickname,
  initialUsername,
  initialPhone,
  initialStatus,
  initialGrade,
}: Props) {
  const router = useRouter();

  const searchParams = useSearchParams();

  const [detailId, setDetailId] = React.useState<number | null>(null);
  const [managingPoints, setManagingPoints] = React.useState<MemberListItem | null>(null);
  const [suspending, setSuspending] = React.useState<MemberListItem | null>(null);
  const [activating, setActivating] = React.useState<MemberListItem | null>(null);
  const [withdrawing, setWithdrawing] = React.useState<MemberListItem | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const [nicknameInput, setNicknameInput] = React.useState(initialNickname ?? "");
  const [usernameInput, setUsernameInput] = React.useState(initialUsername ?? "");
  const [phoneInput, setPhoneInput] = React.useState(initialPhone ?? "");
  const [statusInput, setStatusInput] = React.useState<string>(initialStatus ?? "all");
  const [gradeInput, setGradeInput] = React.useState<string>(initialGrade ?? "all");

  function pushParams(next: {
    page?: number;
    size?: number;
    nickname?: string;
    username?: string;
    phone?: string;
    status?: string;
    grade?: string;
  }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.page !== undefined) params.set("page", String(next.page));
    if (next.size !== undefined) params.set("size", String(next.size));
    for (const key of ["nickname", "username", "phone", "status", "grade"] as const) {
      if (next[key] === undefined) continue;
      const v = next[key];
      if (!v || v === "all") params.delete(key);
      else params.set(key, v);
    }
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  function handleSearch(override?: { status?: string; grade?: string }) {
    pushParams({
      page: 0,
      nickname: nicknameInput,
      username: usernameInput,
      phone: phoneInput,
      status: override?.status ?? statusInput,
      grade: override?.grade ?? gradeInput,
    });
  }

  function handleReset() {
    setNicknameInput("");
    setUsernameInput("");
    setPhoneInput("");
    setStatusInput("all");
    setGradeInput("all");
    pushParams({ page: 0, nickname: "", username: "", phone: "", status: "all", grade: "all" });
  }

  const table = useReactTable({
    data: members,
    columns: membersColumns,
    state: {
      pagination: { pageIndex: pagination.page, pageSize: pagination.size },
    },
    manualPagination: true,
    pageCount: Math.max(pagination.totalPages, 1),
    getRowId: (row) => String(row.id),
    autoResetPageIndex: false,
    getCoreRowModel: getCoreRowModel(),
    onPaginationChange: (updater) => {
      const previous: PaginationState = {
        pageIndex: pagination.page,
        pageSize: pagination.size,
      };
      const next = typeof updater === "function" ? updater(previous) : updater;
      if (next.pageSize !== previous.pageSize) {
        pushParams({ page: 0, size: next.pageSize });
      } else if (next.pageIndex !== previous.pageIndex) {
        pushParams({ page: next.pageIndex });
      }
    },
    meta: {
      totalElements: pagination.totalElements,
      onView: (member) => setDetailId(member.id),
      onManagePoints: (member) => setManagingPoints(member),
      onSuspend: (member) => setSuspending(member),
      onActivate: (member) => setActivating(member),
      onWithdraw: (member) => setWithdrawing(member),
    } satisfies MembersTableMeta,
  });

  return (
    <Card>
      <CardHeader className="border-b">
        <CardTitle className="text-xl leading-none">{MEMBER_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{MEMBER_PAGE_COPY.DESCRIPTION}</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4 px-0">
        <form
          className="flex flex-wrap items-center gap-2 px-4 pt-2"
          onSubmit={(e) => {
            e.preventDefault();
            handleSearch();
          }}
        >
          <Input
            className="w-32"
            placeholder="닉네임"
            value={nicknameInput}
            onChange={(e) => setNicknameInput(e.target.value)}
            disabled={isPending}
          />
          <Input
            className="w-32"
            placeholder="아이디"
            value={usernameInput}
            onChange={(e) => setUsernameInput(e.target.value)}
            disabled={isPending}
          />
          <Input
            className="w-36"
            placeholder="휴대폰번호"
            value={phoneInput}
            onChange={(e) => setPhoneInput(e.target.value)}
            disabled={isPending}
          />
          <Select
            value={statusInput}
            onValueChange={(value) => {
              setStatusInput(value);
              handleSearch({ status: value });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">상태:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value="all">전체</SelectItem>
                {MEMBER_STATUS_OPTIONS.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
          <Select
            value={gradeInput}
            onValueChange={(value) => {
              setGradeInput(value);
              handleSearch({ grade: value });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">등급:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value="all">전체</SelectItem>
                {MEMBER_GRADE_OPTIONS.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
          <Button type="submit" size="sm" disabled={isPending}>
            <Search className="size-4" />
            검색
          </Button>
          <Button type="button" size="sm" variant="destructive" onClick={handleReset} disabled={isPending}>
            <X className="size-4" />
            초기화
          </Button>
        </form>
        <MembersTable table={table} isPending={isPending} />
      </CardContent>
      <MemberDetailSheet memberId={detailId} onOpenChange={(open) => !open && setDetailId(null)} />
      <MemberPointSheet member={managingPoints} onOpenChange={(open) => !open && setManagingPoints(null)} />
      <SuspendMemberDialog member={suspending} onOpenChange={(open) => !open && setSuspending(null)} />
      <ActivateMemberDialog member={activating} onOpenChange={(open) => !open && setActivating(null)} />
      <WithdrawMemberDialog member={withdrawing} onOpenChange={(open) => !open && setWithdrawing(null)} />
    </Card>
  );
}
