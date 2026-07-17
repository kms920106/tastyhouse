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
import { PARTNERSHIP_STATUS_OPTIONS } from "@/feature/partnership-request/constants";
import type { PartnershipRequestListItem, PartnershipStatus } from "@/feature/partnership-request/domain";
import { PARTNERSHIP_PAGE_COPY } from "@/feature/partnership-request/message";

import { DeletePartnershipRequestDialog } from "./delete-partnership-request-dialog";
import { PartnershipRequestDetailSheet } from "./partnership-request-detail-sheet";
import { PartnershipRequestStatusDialog } from "./partnership-request-status-dialog";
import { type PartnershipRequestsTableMeta, partnershipRequestsColumns } from "./partnership-requests-columns";
import { PartnershipRequestsTable } from "./partnership-requests-table";

interface Props {
  partnershipRequests: PartnershipRequestListItem[];
  pagination: ApiPagination;
  initialBusinessName?: string;
  initialContactName?: string;
  initialContactPhone?: string;
  initialStatus?: PartnershipStatus;
  initialStartDate?: string;
  initialEndDate?: string;
}

export function PartnershipRequests({
  partnershipRequests,
  pagination,
  initialBusinessName,
  initialContactName,
  initialContactPhone,
  initialStatus,
  initialStartDate,
  initialEndDate,
}: Props) {
  const router = useRouter();

  const searchParams = useSearchParams();

  const [detailId, setDetailId] = React.useState<number | null>(null);
  const [changingStatus, setChangingStatus] = React.useState<PartnershipRequestListItem | null>(null);
  const [deleting, setDeleting] = React.useState<PartnershipRequestListItem | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const [businessNameInput, setBusinessNameInput] = React.useState(initialBusinessName ?? "");
  const [contactNameInput, setContactNameInput] = React.useState(initialContactName ?? "");
  const [contactPhoneInput, setContactPhoneInput] = React.useState(initialContactPhone ?? "");
  const [statusInput, setStatusInput] = React.useState<string>(initialStatus ?? "all");
  const [startDateInput, setStartDateInput] = React.useState(initialStartDate ?? "");
  const [endDateInput, setEndDateInput] = React.useState(initialEndDate ?? "");

  function pushParams(next: {
    page?: number;
    size?: number;
    businessName?: string;
    contactName?: string;
    contactPhone?: string;
    status?: string;
    startDate?: string;
    endDate?: string;
  }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.page !== undefined) params.set("page", String(next.page));
    if (next.size !== undefined) params.set("size", String(next.size));
    for (const key of ["businessName", "contactName", "contactPhone", "status", "startDate", "endDate"] as const) {
      if (next[key] === undefined) continue;
      const v = next[key];
      if (!v || v === "all") params.delete(key);
      else params.set(key, v);
    }
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  /** datetime-local 입력값("YYYY-MM-DDTHH:mm")에 초 단위를 보정해 LocalDateTime 형식으로 맞춘다. */
  function toLocalDateTime(value: string): string {
    if (!value) return value;
    return value.length === 16 ? `${value}:00` : value;
  }

  function handleSearch(override?: { status?: string }) {
    pushParams({
      page: 0,
      businessName: businessNameInput,
      contactName: contactNameInput,
      contactPhone: contactPhoneInput,
      status: override?.status ?? statusInput,
      startDate: toLocalDateTime(startDateInput),
      endDate: toLocalDateTime(endDateInput),
    });
  }

  function handleReset() {
    setBusinessNameInput("");
    setContactNameInput("");
    setContactPhoneInput("");
    setStatusInput("all");
    setStartDateInput("");
    setEndDateInput("");
    pushParams({
      page: 0,
      businessName: "",
      contactName: "",
      contactPhone: "",
      status: "all",
      startDate: "",
      endDate: "",
    });
  }

  const table = useReactTable({
    data: partnershipRequests,
    columns: partnershipRequestsColumns,
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
      onView: (partnershipRequest) => setDetailId(partnershipRequest.id),
      onChangeStatus: (partnershipRequest) => setChangingStatus(partnershipRequest),
      onDelete: (partnershipRequest) => setDeleting(partnershipRequest),
    } satisfies PartnershipRequestsTableMeta,
  });

  return (
    <Card>
      <CardHeader className="border-b">
        <CardTitle className="text-xl leading-none">{PARTNERSHIP_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{PARTNERSHIP_PAGE_COPY.DESCRIPTION}</CardDescription>
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
            className="w-40"
            placeholder="상호명"
            value={businessNameInput}
            onChange={(e) => setBusinessNameInput(e.target.value)}
            disabled={isPending}
          />
          <Input
            className="w-32"
            placeholder="담당자명"
            value={contactNameInput}
            onChange={(e) => setContactNameInput(e.target.value)}
            disabled={isPending}
          />
          <Input
            className="w-40"
            placeholder="연락처"
            value={contactPhoneInput}
            onChange={(e) => setContactPhoneInput(e.target.value)}
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
              <span className="text-muted-foreground">처리상태:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value="all">전체</SelectItem>
                {PARTNERSHIP_STATUS_OPTIONS.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
          <Input
            type="datetime-local"
            className="w-48"
            value={startDateInput}
            onChange={(e) => setStartDateInput(e.target.value)}
            disabled={isPending}
          />
          <span className="text-muted-foreground">~</span>
          <Input
            type="datetime-local"
            className="w-48"
            value={endDateInput}
            onChange={(e) => setEndDateInput(e.target.value)}
            disabled={isPending}
          />
          <Button type="submit" size="sm" disabled={isPending}>
            <Search className="size-4" />
            검색
          </Button>
          <Button type="button" size="sm" variant="destructive" onClick={handleReset} disabled={isPending}>
            <X className="size-4" />
            초기화
          </Button>
        </form>
        <PartnershipRequestsTable table={table} isPending={isPending} />
      </CardContent>
      <PartnershipRequestDetailSheet
        partnershipRequestId={detailId}
        onOpenChange={(open) => !open && setDetailId(null)}
      />
      <PartnershipRequestStatusDialog
        partnershipRequest={changingStatus}
        onOpenChange={(open) => !open && setChangingStatus(null)}
      />
      <DeletePartnershipRequestDialog
        partnershipRequest={deleting}
        onOpenChange={(open) => !open && setDeleting(null)}
      />
    </Card>
  );
}
