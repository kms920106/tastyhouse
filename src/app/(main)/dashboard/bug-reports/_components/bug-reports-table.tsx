"use client";
"use no memo";

import type { MouseEvent } from "react";

import { flexRender, type Table as TableType } from "@tanstack/react-table";

import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import type { BugReportListItem } from "@/feature/bug-report/domain";

import type { BugReportsTableMeta } from "./bug-reports-columns";

function preventNavigation(event: MouseEvent<HTMLAnchorElement>) {
  event.preventDefault();
}

/** 1-based 현재 페이지 기준으로 표시할 1-based 페이지 번호 묶음. */
function getPageNumbers(currentPage: number, pageCount: number) {
  if (pageCount <= 3) {
    return Array.from({ length: pageCount }, (_, index) => index + 1);
  }

  if (currentPage <= 2) return [1, 2, 3];
  if (currentPage >= pageCount - 1) return [pageCount - 2, pageCount - 1, pageCount];

  return [currentPage - 1, currentPage, currentPage + 1];
}

export function BugReportsTable({ table, isPending }: { table: TableType<BugReportListItem>; isPending: boolean }) {
  const pageCount = Math.max(table.getPageCount(), 1);
  const currentPage = Math.min(table.getState().pagination.pageIndex + 1, pageCount);
  const pageNumbers = getPageNumbers(currentPage, pageCount);
  const pageSize = table.getState().pagination.pageSize;
  const rowsPerPage = `${pageSize}`;
  const leafColumns = table.getVisibleLeafColumns();
  const totalElements = (table.options.meta as BugReportsTableMeta).totalElements;

  return (
    <div className="flex flex-1 flex-col gap-4">
      <div className="overflow-x-auto">
        <Table
          className="table-fixed **:data-[slot='table-cell']:px-4 **:data-[slot='table-head']:px-4"
          style={{ minWidth: table.getTotalSize() }}
        >
          <TableHeader className="[&_tr]:border-t">
            {table.getHeaderGroups().map((headerGroup) => (
              <TableRow key={headerGroup.id}>
                {headerGroup.headers.map((header) => (
                  <TableHead key={header.id} className="py-4 font-normal" style={{ width: header.getSize() }}>
                    {header.isPlaceholder ? null : flexRender(header.column.columnDef.header, header.getContext())}
                  </TableHead>
                ))}
              </TableRow>
            ))}
          </TableHeader>

          <TableBody>
            {isPending ? (
              Array.from({ length: pageSize }, (_, index) => `bug-report-row-skeleton-${index}`).map((key) => (
                <TableRow key={key} className="border-border/60">
                  {leafColumns.map((column) => (
                    <TableCell key={column.id} className="px-3 py-4 align-middle" style={{ width: column.getSize() }}>
                      <div className="flex h-8 items-center">
                        <Skeleton className="h-5 w-full" />
                      </div>
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : table.getRowModel().rows.length ? (
              table.getRowModel().rows.map((row) => (
                <TableRow key={row.id} className="border-border/60 hover:bg-white/2.5">
                  {row.getVisibleCells().map((cell) => (
                    <TableCell
                      key={cell.id}
                      className="px-3 py-4 align-middle"
                      style={{ width: cell.column.getSize() }}
                    >
                      {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={table.getVisibleLeafColumns().length} className="h-24 text-center">
                  등록된 버그 제보가 없습니다.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      <Separator />

      <div className="flex items-center justify-between px-4">
        <div className="flex items-center gap-4 text-muted-foreground text-sm">
          <div className="flex items-center gap-2">
            <span>페이지 당</span>
            <Select
              value={`${table.getState().pagination.pageSize}`}
              onValueChange={(value) => table.setPageSize(Number(value))}
              disabled={isPending}
            >
              <SelectTrigger size="sm" className="w-20" id="bug-reports-rows-per-page">
                <SelectValue placeholder={rowsPerPage} />
              </SelectTrigger>
              <SelectContent side="top">
                <SelectGroup>
                  {[10, 20, 30, 40, 50].map((size) => (
                    <SelectItem key={size} value={`${size}`}>
                      {size}
                    </SelectItem>
                  ))}
                </SelectGroup>
              </SelectContent>
            </Select>
          </div>
          <span>
            {currentPage} / {pageCount} 페이지 · 총 {totalElements}건
          </span>
        </div>

        <Pagination className="mx-0 w-auto justify-start md:justify-end">
          <PaginationContent>
            <PaginationItem>
              <PaginationPrevious
                href="#"
                text=""
                className={!table.getCanPreviousPage() || isPending ? "pointer-events-none opacity-50" : undefined}
                onClick={(event) => {
                  preventNavigation(event);
                  table.previousPage();
                }}
              />
            </PaginationItem>
            {pageNumbers[0] > 1 ? (
              <PaginationItem>
                <PaginationEllipsis />
              </PaginationItem>
            ) : null}
            {pageNumbers.map((pageNumber) => (
              <PaginationItem key={`page-${pageNumber}`}>
                <PaginationLink
                  href="#"
                  isActive={table.getState().pagination.pageIndex === pageNumber - 1}
                  className={isPending ? "pointer-events-none opacity-50" : undefined}
                  onClick={(event) => {
                    preventNavigation(event);
                    table.setPageIndex(pageNumber - 1);
                  }}
                >
                  {pageNumber}
                </PaginationLink>
              </PaginationItem>
            ))}
            {pageNumbers[pageNumbers.length - 1] < pageCount ? (
              <PaginationItem>
                <PaginationEllipsis />
              </PaginationItem>
            ) : null}
            <PaginationItem>
              <PaginationNext
                href="#"
                text=""
                className={!table.getCanNextPage() || isPending ? "pointer-events-none opacity-50" : undefined}
                onClick={(event) => {
                  preventNavigation(event);
                  table.nextPage();
                }}
              />
            </PaginationItem>
          </PaginationContent>
        </Pagination>
      </div>
    </div>
  );
}
