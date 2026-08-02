"use client";
"use no memo";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { getCoreRowModel, type PaginationState, useReactTable } from "@tanstack/react-table";
import { Plus, Search, X } from "lucide-react";

import type { ApiPagination } from "@/api/shared/types";
import { Button } from "@/components/ui/button";
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { EVENT_STATUS_OPTIONS } from "@/feature/event/constants";
import type { EventListItem, EventStatus } from "@/feature/event/domain";
import { EVENT_PAGE_COPY } from "@/feature/event/message";

import { DeleteEventDialog } from "./delete-event-dialog";
import { EventAnnouncementSheet } from "./event-announcement-sheet";
import { EventDetailSheet } from "./event-detail-sheet";
import { EventFormSheet } from "./event-form-sheet";
import { EventWinnersSheet } from "./event-winners-sheet";
import { type EventsTableMeta, eventsColumns } from "./events-columns";
import { EventsTable } from "./events-table";

interface Props {
  events: EventListItem[];
  pagination: ApiPagination;
  initialName?: string;
  initialStatus?: EventStatus;
}

export function Events({ events, pagination, initialName, initialStatus }: Props) {
  const router = useRouter();

  const searchParams = useSearchParams();

  const [formOpen, setFormOpen] = React.useState(false);
  const [editing, setEditing] = React.useState<EventListItem | null>(null);
  const [detailId, setDetailId] = React.useState<number | null>(null);
  const [announcing, setAnnouncing] = React.useState<EventListItem | null>(null);
  const [managingWinners, setManagingWinners] = React.useState<EventListItem | null>(null);
  const [deleting, setDeleting] = React.useState<EventListItem | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const [nameInput, setNameInput] = React.useState(initialName ?? "");
  const [statusInput, setStatusInput] = React.useState<string>(initialStatus ?? "all");

  function pushParams(next: { page?: number; size?: number; name?: string; status?: string }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.page !== undefined) params.set("page", String(next.page));
    if (next.size !== undefined) params.set("size", String(next.size));
    for (const key of ["name", "status"] as const) {
      if (next[key] === undefined) continue;
      const v = next[key];
      if (!v || v === "all") params.delete(key);
      else params.set(key, v);
    }
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  function handleSearch(override?: { status?: string }) {
    pushParams({
      page: 0,
      name: nameInput,
      status: override?.status ?? statusInput,
    });
  }

  function handleReset() {
    setNameInput("");
    setStatusInput("all");
    pushParams({ page: 0, name: "", status: "all" });
  }

  function openCreate() {
    setEditing(null);
    setFormOpen(true);
  }

  function openEdit(event: EventListItem) {
    setEditing(event);
    setFormOpen(true);
  }

  const table = useReactTable({
    data: events,
    columns: eventsColumns,
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
      onView: (event) => setDetailId(event.id),
      onEdit: (event) => openEdit(event),
      onAnnouncement: (event) => setAnnouncing(event),
      onWinners: (event) => setManagingWinners(event),
      onDelete: (event) => setDeleting(event),
    } satisfies EventsTableMeta,
  });

  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{EVENT_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{EVENT_PAGE_COPY.DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          <Button size="sm" onClick={openCreate}>
            <Plus /> 이벤트 등록
          </Button>
        </CardAction>
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
            placeholder="이벤트명"
            value={nameInput}
            onChange={(e) => setNameInput(e.target.value)}
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
                {EVENT_STATUS_OPTIONS.map((option) => (
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
        <EventsTable table={table} isPending={isPending} />
      </CardContent>
      <EventFormSheet open={formOpen} onOpenChange={setFormOpen} event={editing} />
      <EventDetailSheet eventId={detailId} onOpenChange={(open) => !open && setDetailId(null)} />
      <EventAnnouncementSheet event={announcing} onOpenChange={(open) => !open && setAnnouncing(null)} />
      <EventWinnersSheet event={managingWinners} onOpenChange={(open) => !open && setManagingWinners(null)} />
      <DeleteEventDialog event={deleting} onOpenChange={(open) => !open && setDeleting(null)} />
    </Card>
  );
}
