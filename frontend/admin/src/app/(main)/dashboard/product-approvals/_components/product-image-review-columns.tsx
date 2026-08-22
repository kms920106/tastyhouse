"use client";
"use no memo";

import * as React from "react";

import type { ColumnDef } from "@tanstack/react-table";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import type { ProductImageChangeRequestItem } from "@/feature/product/domain";
import { APPROVAL_STATUS_LABEL, PRODUCT_APPROVAL_COPY } from "@/feature/product/message";

/** 승인·반려는 각각 다른 다이얼로그를 여는 별 행동이므로 테이블 meta 로 셸에 올려 보낸다. */
export interface ProductApprovalsTableMeta<TRequest> {
  totalElements: number;
  onApprove: (request: TRequest) => void;
  onReject: (request: TRequest) => void;
  /** 이미지 로드에 실패한 요청 ID 집합 — 근거를 볼 수 없으므로 승인 버튼을 잠근다 */
  imageLoadFailedIds?: ReadonlySet<number>;
  onImageLoadError?: (requestId: number) => void;
}

export function ApprovalStatusBadge({ status }: { status: ProductImageChangeRequestItem["status"] }) {
  return (
    <Badge variant={status === "APPROVED" ? "default" : status === "REJECTED" ? "destructive" : "secondary"}>
      {APPROVAL_STATUS_LABEL[status]}
    </Badge>
  );
}

/**
 * 승인·반려 버튼 셀. 드롭다운으로 감싸지 않는 이유는 검수가 한 화면에서 반복되는 작업이라
 * 클릭 한 번에 처리되어야 하기 때문이다.
 *
 * <p>이미지 검수에서 `canApprove`가 false 인 경우(이미지가 없거나 로드 실패) 승인만 막는다 —
 * 근거를 못 본 상태로 승인할 수는 없지만 반려는 오히려 그 상황의 정상 처리다.
 */
export function ApprovalActionsCell<TRequest extends { status: ProductImageChangeRequestItem["status"] }>({
  request,
  meta,
  canApprove = true,
}: {
  request: TRequest;
  meta: ProductApprovalsTableMeta<TRequest>;
  canApprove?: boolean;
}) {
  if (request.status !== "PENDING") return <div className="text-right text-muted-foreground">-</div>;

  return (
    <div className="flex justify-end gap-1">
      <Button size="sm" variant="outline" disabled={!canApprove} onClick={() => meta.onApprove(request)}>
        {PRODUCT_APPROVAL_COPY.APPROVE}
      </Button>
      <Button size="sm" variant="ghost" className="text-destructive" onClick={() => meta.onReject(request)}>
        {PRODUCT_APPROVAL_COPY.REJECT}
      </Button>
    </div>
  );
}

/**
 * 검수 썸네일. 클릭하면 원본을 새 탭에서 연다 — 썸네일만으로는 해상도·내용을 판단할 수 없다.
 *
 * <p>로드 실패를 셸에 보고해 그 행의 승인 버튼을 잠근다. 깨진 이미지를 보고 승인하는 것이
 * 검수 자체를 무력화하기 때문이다.
 */
function ReviewThumbnail({
  request,
  onLoadError,
}: {
  request: ProductImageChangeRequestItem;
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

export const productImageReviewColumns: ColumnDef<ProductImageChangeRequestItem>[] = [
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
    size: 180,
    minSize: 140,
    maxSize: 240,
  },
  {
    id: "imageUrl",
    header: "이미지",
    cell: ({ row, table }) => (
      <ReviewThumbnail
        request={row.original}
        onLoadError={(table.options.meta as ProductApprovalsTableMeta<ProductImageChangeRequestItem>).onImageLoadError}
      />
    ),
    enableSorting: false,
    size: 110,
    minSize: 90,
    maxSize: 130,
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
      const meta = table.options.meta as ProductApprovalsTableMeta<ProductImageChangeRequestItem>;
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
