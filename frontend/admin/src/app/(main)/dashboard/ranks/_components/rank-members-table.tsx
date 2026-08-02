"use client";
"use no memo";

import { flexRender, getCoreRowModel, type Table as TableType, useReactTable } from "@tanstack/react-table";

import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import type { RankMember } from "@/feature/rank/domain";

import { rankMembersColumns } from "./rank-members-columns";

interface RankMembersTableProps {
  members: RankMember[];
  isPending: boolean;
}

export function RankMembersTable({ members, isPending }: RankMembersTableProps) {
  const table: TableType<RankMember> = useReactTable({
    data: members,
    columns: rankMembersColumns,
    getRowId: (row) => String(row.memberId),
    getCoreRowModel: getCoreRowModel(),
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
        {isPending ? (
          Array.from({ length: 5 }, (_, index) => `rank-member-row-skeleton-${index}`).map((key) => (
            <TableRow key={key} className="border-border/60">
              {leafColumns.map((column) => (
                <TableCell key={column.id} className="px-3 py-4 align-middle" style={{ width: column.getSize() }}>
                  <div className="h-5 w-full animate-pulse rounded bg-muted" />
                </TableCell>
              ))}
            </TableRow>
          ))
        ) : table.getRowModel().rows.length ? (
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
              랭킹 데이터가 없습니다.
            </TableCell>
          </TableRow>
        )}
      </TableBody>
    </Table>
  );
}
