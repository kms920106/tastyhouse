"use client";
"use no memo";

import * as React from "react";

import type { ColumnDef } from "@tanstack/react-table";

import type { ProductRepresentativeRequestItem } from "@/feature/product/domain";
import { PRODUCT_APPROVAL_COPY } from "@/feature/product/message";

import {
  ApprovalActionsCell,
  ApprovalStatusBadge,
  type ProductApprovalsTableMeta,
} from "./product-image-review-columns";

/**
 * 메뉴 이미지 썸네일. 이미지 검수 탭의 `ReviewThumbnail` 과 동작은 같지만 그쪽이
 * `ProductImageChangeRequestItem` 에 묶여 있어 공유할 수 없다.
 *
 * <p>사장님 추천 검수 기준("메뉴명과 메뉴 이미지가 일치")은 이미지를 봐야 판단할 수 있으므로,
 * 이미지가 없거나 로드에 실패하면 셸에 보고해 그 행의 승인 버튼을 잠근다.
 */
function RepresentativeThumbnail({
  request,
  onLoadError,
}: {
  request: ProductRepresentativeRequestItem;
  onLoadError?: (requestId: number) => void;
}) {
  const [failed, setFailed] = React.useState(false);
  const { id, imageUrl, productName } = request;

  if (!imageUrl || failed) {
    return <span className="text-muted-foreground text-xs">{PRODUCT_APPROVAL_COPY.IMAGE_MISSING}</span>;
  }

  return (
    <a href={imageUrl} target="_blank" rel="noreferrer" className="block size-12" title={productName}>
      {/* biome-ignore lint/performance/noImgElement: 검수 목록 썸네일 — 외부 스토리지 URL 이라 최적화 대상이 아니다 */}
      <img
        src={imageUrl}
        alt={productName}
        className="size-12 rounded-md border object-cover"
        onError={() => {
          setFailed(true);
          onLoadError?.(id);
        }}
      />
    </a>
  );
}

export const representativeReviewColumns: ColumnDef<ProductRepresentativeRequestItem>[] = [
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
    // 추천 기준 1번("가게 카테고리와 일치")을 판단하려면 가게가 어디인지 알아야 하므로 가게명을 함께 노출한다.
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
    accessorKey: "productName",
    header: "메뉴명",
    cell: ({ row }) => <span className="line-clamp-2 break-all">{row.original.productName}</span>,
    enableSorting: false,
    size: 180,
    minSize: 140,
    maxSize: 240,
  },
  {
    id: "imageUrl",
    header: "메뉴 이미지",
    cell: ({ row, table }) => (
      <RepresentativeThumbnail
        request={row.original}
        onLoadError={
          (table.options.meta as ProductApprovalsTableMeta<ProductRepresentativeRequestItem>).onImageLoadError
        }
      />
    ),
    enableSorting: false,
    size: 120,
    minSize: 100,
    maxSize: 140,
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
    cell: ({ row, table }) => {
      const meta = table.options.meta as ProductApprovalsTableMeta<ProductRepresentativeRequestItem>;
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
