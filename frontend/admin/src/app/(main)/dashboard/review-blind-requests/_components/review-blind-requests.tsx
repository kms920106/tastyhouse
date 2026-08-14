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
import {
  REVIEW_BLIND_REASON_LABEL,
  REVIEW_BLIND_REASON_OPTIONS,
  REVIEW_BLIND_REQUEST_STATUS_LABEL,
  REVIEW_BLIND_REQUEST_STATUS_OPTIONS,
} from "@/feature/review-blind-request/constants";
import type {
  ReviewBlindReason,
  ReviewBlindRequestDetail,
  ReviewBlindRequestListItem,
  ReviewBlindRequestStatus,
} from "@/feature/review-blind-request/domain";
import { REVIEW_BLIND_REQUEST_PAGE_COPY } from "@/feature/review-blind-request/message";

import { BlindRequestApproveDialog } from "./blind-request-approve-dialog";
import { BlindRequestRejectDialog } from "./blind-request-reject-dialog";
import { ReviewBlindRequestDetailSheet } from "./review-blind-request-detail-sheet";
import { type ReviewBlindRequestsTableMeta, reviewBlindRequestsColumns } from "./review-blind-requests-columns";
import { ReviewBlindRequestsTable } from "./review-blind-requests-table";

/** "전체" 센티넬 — Radix Select 의 value 는 항상 안정 문자열이어야 하므로 빈 값 대신 사용한다. */
const ALL_VALUE = "all";

interface Props {
  blindRequests: ReviewBlindRequestListItem[];
  pagination: ApiPagination;
  initialStatus: ReviewBlindRequestStatus;
  initialShopId?: number;
  initialReason?: ReviewBlindReason;
  initialStartDate?: string;
  initialEndDate?: string;
  detailRequestId?: number;
  detail?: ReviewBlindRequestDetail;
  detailError?: string;
}

export function ReviewBlindRequests({
  blindRequests,
  pagination,
  initialStatus,
  initialShopId,
  initialReason,
  initialStartDate,
  initialEndDate,
  detailRequestId,
  detail,
  detailError,
}: Props) {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [approveTarget, setApproveTarget] = React.useState<ReviewBlindRequestListItem | null>(null);
  const [rejectTarget, setRejectTarget] = React.useState<ReviewBlindRequestListItem | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const [statusInput, setStatusInput] = React.useState<string>(initialStatus);
  const [shopIdInput, setShopIdInput] = React.useState(initialShopId ? String(initialShopId) : "");
  const [reasonInput, setReasonInput] = React.useState<string>(initialReason ?? ALL_VALUE);
  const [startDateInput, setStartDateInput] = React.useState(initialStartDate ?? "");
  const [endDateInput, setEndDateInput] = React.useState(initialEndDate ?? "");

  function pushParams(next: {
    page?: number;
    size?: number;
    status?: string;
    shopId?: string;
    reason?: string;
    startDate?: string;
    endDate?: string;
    requestId?: string;
  }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.page !== undefined) params.set("page", String(next.page));
    if (next.size !== undefined) params.set("size", String(next.size));
    for (const key of ["status", "shopId", "reason", "startDate", "endDate", "requestId"] as const) {
      if (next[key] === undefined) continue;
      const value = next[key];
      if (!value || value === ALL_VALUE) params.delete(key);
      else params.set(key, value);
    }
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  function handleSearch(override?: { status?: string; reason?: string }) {
    pushParams({
      page: 0,
      status: override?.status ?? statusInput,
      shopId: shopIdInput,
      reason: override?.reason ?? reasonInput,
      startDate: startDateInput,
      endDate: endDateInput,
    });
  }

  function handleReset() {
    setStatusInput("PENDING");
    setShopIdInput("");
    setReasonInput(ALL_VALUE);
    setStartDateInput("");
    setEndDateInput("");
    pushParams({
      page: 0,
      status: "PENDING",
      shopId: "",
      reason: ALL_VALUE,
      startDate: "",
      endDate: "",
    });
  }

  function handleRefresh() {
    router.refresh();
  }

  const table = useReactTable({
    data: blindRequests,
    columns: reviewBlindRequestsColumns,
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
      onView: (blindRequest) => pushParams({ requestId: String(blindRequest.id) }),
      onApprove: (blindRequest) => setApproveTarget(blindRequest),
      onReject: (blindRequest) => setRejectTarget(blindRequest),
    } satisfies ReviewBlindRequestsTableMeta,
  });

  return (
    <Card>
      <CardHeader className="border-b">
        <CardTitle className="text-xl leading-none">{REVIEW_BLIND_REQUEST_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">
          {REVIEW_BLIND_REQUEST_PAGE_COPY.DESCRIPTION}
        </CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4 px-0">
        <form
          className="flex flex-wrap items-center gap-2 px-4 pt-2"
          onSubmit={(event) => {
            event.preventDefault();
            handleSearch();
          }}
        >
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
                {REVIEW_BLIND_REQUEST_STATUS_OPTIONS.map((option) => (
                  <SelectItem key={option} value={option}>
                    {REVIEW_BLIND_REQUEST_STATUS_LABEL[option]}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
          <Select
            value={reasonInput}
            onValueChange={(value) => {
              setReasonInput(value);
              handleSearch({ reason: value });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">사유:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value={ALL_VALUE}>전체</SelectItem>
                {REVIEW_BLIND_REASON_OPTIONS.map((option) => (
                  <SelectItem key={option} value={option}>
                    {REVIEW_BLIND_REASON_LABEL[option]}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
          <Input
            className="w-32"
            placeholder="가게 ID"
            inputMode="numeric"
            value={shopIdInput}
            onChange={(event) => setShopIdInput(event.target.value)}
            disabled={isPending}
          />
          <Input
            type="date"
            className="w-40"
            value={startDateInput}
            onChange={(event) => setStartDateInput(event.target.value)}
            disabled={isPending}
          />
          <span className="text-muted-foreground">~</span>
          <Input
            type="date"
            className="w-40"
            value={endDateInput}
            onChange={(event) => setEndDateInput(event.target.value)}
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
        <ReviewBlindRequestsTable table={table} isPending={isPending} />
      </CardContent>
      <ReviewBlindRequestDetailSheet
        requestId={detailRequestId ?? null}
        detail={detail}
        error={detailError}
        onOpenChange={(open) => !open && pushParams({ requestId: "" })}
      />
      <BlindRequestApproveDialog
        blindRequest={approveTarget}
        onOpenChange={(open) => !open && setApproveTarget(null)}
        onSettled={handleRefresh}
      />
      <BlindRequestRejectDialog
        blindRequest={rejectTarget}
        onOpenChange={(open) => !open && setRejectTarget(null)}
        onSettled={handleRefresh}
      />
    </Card>
  );
}
