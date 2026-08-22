"use client";
"use no memo";

import * as React from "react";

import type { ColumnDef } from "@tanstack/react-table";

import { PRODUCT_APPROVAL_COPY } from "@/feature/product/message";
import type { MenuCollectionImageRequestItem } from "@/feature/shop/domain";

import {
  ApprovalActionsCell,
  ApprovalStatusBadge,
  type ProductApprovalsTableMeta,
} from "./product-image-review-columns";

/**
 * 메뉴모음컷 썸네일. 이미지 검수 탭의 `ReviewThumbnail` 과 같은 동작이지만
 * 그쪽은 `ProductImageChangeRequestItem`(메뉴명 보유)에 묶여 있어 공유할 수 없다 —
 * 메뉴모음컷은 가게 단위 배너라 메뉴명이 없고 title 을 가게명으로 채운다.
 */
function MenuCollectionThumbnail({
  request,
  onLoadError,
}: {
  request: MenuCollectionImageRequestItem;
  onLoadError?: (requestId: number) => void;
}) {
  const [failed, setFailed] = React.useState(false);
  const { id, imageUrl, shopName } = request;

  if (!imageUrl || failed) {
    return <span className="text-muted-foreground text-xs">{PRODUCT_APPROVAL_COPY.IMAGE_MISSING}</span>;
  }

  return (
    <a href={imageUrl} target="_blank" rel="noreferrer" className="block size-12" title={shopName}>
      {/* biome-ignore lint/performance/noImgElement: 검수 목록 썸네일 — 외부 스토리지 URL 이라 최적화 대상이 아니다 */}
      <img
        src={imageUrl}
        alt={shopName}
        className="size-12 rounded-md border object-cover"
        onError={() => {
          setFailed(true);
          onLoadError?.(id);
        }}
      />
    </a>
  );
}

export const menuCollectionReviewColumns: ColumnDef<MenuCollectionImageRequestItem>[] = [
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
    accessorKey: "shopName",
    header: "가게",
    // 메뉴모음컷은 가게 배너라 메뉴명이 없다. 대신 가게명을 함께 보여 어느 가게 건인지 바로 알게 한다.
    cell: ({ row }) => (
      <div className="flex flex-col">
        <span className="line-clamp-2 break-all">{row.original.shopName}</span>
        <span className="text-muted-foreground text-xs tabular-nums">#{row.original.shopId}</span>
      </div>
    ),
    enableSorting: false,
    size: 180,
    minSize: 140,
    maxSize: 240,
  },
  {
    id: "imageUrl",
    header: "이미지",
    cell: ({ row, table }) => (
      <MenuCollectionThumbnail
        request={row.original}
        onLoadError={(table.options.meta as ProductApprovalsTableMeta<MenuCollectionImageRequestItem>).onImageLoadError}
      />
    ),
    enableSorting: false,
    size: 110,
    minSize: 90,
    maxSize: 130,
  },
  {
    accessorKey: "sort",
    header: "순서",
    // 배너는 sort 순으로 노출되므로 몇 번째 자리에 걸릴 이미지인지 함께 본다.
    cell: ({ row }) => <span className="tabular-nums">{row.original.sort}</span>,
    enableSorting: false,
    size: 80,
    minSize: 70,
    maxSize: 90,
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
    size: 220,
    minSize: 160,
    maxSize: 300,
  },
  {
    id: "actions",
    header: () => <div className="text-right">처리</div>,
    cell: ({ row, table }) => {
      const meta = table.options.meta as ProductApprovalsTableMeta<MenuCollectionImageRequestItem>;
      return (
        <ApprovalActionsCell
          request={row.original}
          meta={meta}
          canApprove={row.original.imageUrl != null && !meta.imageLoadFailedIds?.has(row.original.id)}
        />
      );
    },
    enableSorting: false,
    enableHiding: false,
    size: 140,
    minSize: 140,
    maxSize: 140,
  },
];
