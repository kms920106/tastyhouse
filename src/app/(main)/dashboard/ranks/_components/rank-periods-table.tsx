"use client";
"use no memo";

import { flexRender, getCoreRowModel, type Table as TableType, useReactTable } from "@tanstack/react-table";

import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import type { RankPeriod } from "@/feature/rank/domain";

import { type RankPeriodsTableMeta, rankPeriodsColumns } from "./rank-periods-columns";

interface RankPeriodsTableProps {
  periods: RankPeriod[];
  meta: RankPeriodsTableMeta;
}

export function RankPeriodsTable({ periods, meta }: RankPeriodsTableProps) {
  const table: TableType<RankPeriod> = useReactTable({
    data: periods,
    columns: rankPeriodsColumns,
    getRowId: (row) => String(row.id),
    getCoreRowModel: getCoreRowModel(),
    meta,
  });

  const leafColumns = table.getVisibleLeafColumns();

  return (
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
        {table.getRowModel().rows.length ? (
          table.getRowModel().rows.map((row) => (
            <TableRow key={row.id} className="border-border/60 hover:bg-white/2.5">
              {row.getVisibleCells().map((cell) => (
                <TableCell key={cell.id} className="px-3 py-4 align-middle" style={{ width: cell.column.getSize() }}>
                  {flexRender(cell.column.columnDef.cell, cell.getContext())}
                </TableCell>
              ))}
            </TableRow>
          ))
        ) : (
          <TableRow>
            <TableCell colSpan={leafColumns.length} className="h-24 text-center">
              등록된 랭킹 기간이 없습니다.
            </TableCell>
          </TableRow>
        )}
      </TableBody>
    </Table>
  );
}
