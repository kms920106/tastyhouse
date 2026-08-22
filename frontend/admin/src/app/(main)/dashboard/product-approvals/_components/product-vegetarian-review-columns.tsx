"use client";
"use no memo";

import type { ColumnDef } from "@tanstack/react-table";

import { Badge } from "@/components/ui/badge";
import type { ProductVegetarianRequestItem } from "@/feature/product/domain";
import { VEGETARIAN_TYPE_LABEL } from "@/feature/product/message";

import {
  ApprovalActionsCell,
  ApprovalStatusBadge,
  type ProductApprovalsTableMeta,
} from "./product-image-review-columns";

export const productVegetarianReviewColumns: ColumnDef<ProductVegetarianRequestItem>[] = [
  {
    accessorKey: "id",
    header: "요청 ID",
    cell: ({ row }) => <span className="tabular-nums">{row.original.id}</span>,
    enableSorting: false,
    size: 90,
    minSize: 80,
    maxSize: 100,
  },
  {
    accessorKey: "shopId",
    header: "가게 ID",
    cell: ({ row }) => <span className="tabular-nums">{row.original.shopId}</span>,
    enableSorting: false,
    size: 90,
    minSize: 80,
    maxSize: 100,
  },
  {
    accessorKey: "productName",
    header: "메뉴명",
    cell: ({ row }) => <span className="line-clamp-2 break-all">{row.original.productName}</span>,
    enableSorting: false,
    size: 160,
    minSize: 120,
    maxSize: 220,
  },
  {
    accessorKey: "vegetarianType",
    header: "채식 단계",
    cell: ({ row }) => <Badge variant="outline">{VEGETARIAN_TYPE_LABEL[row.original.vegetarianType]}</Badge>,
    enableSorting: false,
    size: 110,
    minSize: 100,
    maxSize: 130,
  },
  {
    accessorKey: "ingredients",
    header: "포함 재료",
    // 채식 승인의 유일한 근거이므로 줄임 없이 전문을 노출한다.
    cell: ({ row }) => <span className="whitespace-pre-wrap break-all">{row.original.ingredients}</span>,
    enableSorting: false,
    size: 260,
    minSize: 200,
    maxSize: 360,
  },
  {
    accessorKey: "description",
    header: "메뉴 설명",
    cell: ({ row }) => (
      <span className="line-clamp-3 break-all" title={row.original.description ?? undefined}>
        {row.original.description ?? "-"}
      </span>
    ),
    enableSorting: false,
    size: 200,
    minSize: 140,
    maxSize: 280,
  },
  {
    accessorKey: "status",
    header: "상태",
    cell: ({ row }) => <ApprovalStatusBadge status={row.original.status} />,
    enableSorting: false,
    size: 100,
    minSize: 90,
    maxSize: 120,
  },
  {
    accessorKey: "rejectReason",
    header: "반려 사유",
    cell: ({ row }) => (
      <span className="line-clamp-2 break-all" title={row.original.rejectReason ?? undefined}>
        {row.original.rejectReason ?? "-"}
      </span>
    ),
    enableSorting: false,
    size: 200,
    minSize: 160,
    maxSize: 280,
  },
  {
    id: "actions",
    header: () => <div className="text-right">처리</div>,
    cell: ({ row, table }) => (
      <ApprovalActionsCell
        request={row.original}
        meta={table.options.meta as ProductApprovalsTableMeta<ProductVegetarianRequestItem>}
      />
    ),
    enableSorting: false,
    enableHiding: false,
    size: 140,
    minSize: 140,
    maxSize: 140,
  },
];
