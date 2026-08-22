"use client";
"use no memo";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { getCoreRowModel, type PaginationState, useReactTable } from "@tanstack/react-table";

import type { ApiPagination } from "@/api/shared/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import type {
  ApprovalStatus,
  ProductImageChangeRequestItem,
  ProductRepresentativeRequestItem,
  ProductVegetarianRequestItem,
} from "@/feature/product/domain";
import { APPROVAL_STATUS_LABEL, APPROVAL_STATUS_OPTIONS, PRODUCT_APPROVAL_COPY } from "@/feature/product/message";
import type { MenuCollectionImageRequestItem, StorePriceVerificationRequestItem } from "@/feature/shop/domain";

import { menuCollectionReviewColumns } from "./menu-collection-review-columns";
import { ProductApprovalsTable } from "./product-approvals-table";
import { ProductApproveDialog } from "./product-approve-dialog";
import { type ProductApprovalsTableMeta, productImageReviewColumns } from "./product-image-review-columns";
import { ProductRejectDialog } from "./product-reject-dialog";
import { productVegetarianReviewColumns } from "./product-vegetarian-review-columns";
import { representativeReviewColumns } from "./representative-review-columns";
import { storePriceReviewColumns } from "./store-price-review-columns";

/** 탭마다 조회 API·컬럼이 다르므로 props 를 판별 유니온으로 받는다. */
type Props = { pagination: ApiPagination; initialStatus?: ApprovalStatus } & (
  | { tab: "image"; requests: ProductImageChangeRequestItem[] }
  | { tab: "vegetarian"; requests: ProductVegetarianRequestItem[] }
  | { tab: "menuCollection"; requests: MenuCollectionImageRequestItem[] }
  | { tab: "representative"; requests: ProductRepresentativeRequestItem[] }
  | { tab: "storePrice"; requests: StorePriceVerificationRequestItem[] }
);

/**
 * 승인·반려 다이얼로그가 요청 ID 와 이름만 필요하므로 탭 공용 최소 형태로 줄여 보관한다.
 *
 * <p>메뉴모음컷은 가게 단위 배너라 메뉴명이 없으므로 이름을 optional 로 둔다 — 그 탭에서는 가게명을 넣는다.
 */
type ApprovalTarget = { id: number; productName?: string };

const ALL_STATUS = "all";

export function ProductApprovals(props: Props) {
  const { pagination, initialStatus, tab } = props;
  const router = useRouter();
  const searchParams = useSearchParams();

  const [approveTarget, setApproveTarget] = React.useState<ApprovalTarget | null>(null);
  const [rejectTarget, setRejectTarget] = React.useState<ApprovalTarget | null>(null);
  const [imageLoadFailedIds, setImageLoadFailedIds] = React.useState<ReadonlySet<number>>(() => new Set());
  const [isPending, startTransition] = React.useTransition();

  const statusValue = initialStatus ?? ALL_STATUS;

  function pushParams(next: { tab?: Props["tab"]; page?: number; size?: number; status?: string }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.tab !== undefined) params.set("tab", next.tab);
    if (next.page !== undefined) params.set("page", String(next.page));
    if (next.size !== undefined) params.set("size", String(next.size));
    if (next.status !== undefined) {
      if (!next.status || next.status === ALL_STATUS) params.delete("status");
      else params.set("status", next.status);
    }
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  function handleRefresh() {
    // 로드 실패 기록은 목록이 갱신되면 의미가 없으므로 함께 비운다.
    setImageLoadFailedIds(new Set());
    router.refresh();
  }

  const handleImageLoadError = React.useCallback((requestId: number) => {
    setImageLoadFailedIds((previous) => {
      if (previous.has(requestId)) return previous;
      const next = new Set(previous);
      next.add(requestId);
      return next;
    });
  }, []);

  /** 페이징은 URL 로만 구동한다(클라이언트 페이징 상태 금지). */
  function handlePaginationChange(updater: PaginationState | ((previous: PaginationState) => PaginationState)) {
    const previous: PaginationState = { pageIndex: pagination.page, pageSize: pagination.size };
    const next = typeof updater === "function" ? updater(previous) : updater;
    if (next.pageSize !== previous.pageSize) {
      pushParams({ page: 0, size: next.pageSize });
    } else if (next.pageIndex !== previous.pageIndex) {
      pushParams({ page: next.pageIndex });
    }
  }

  const sharedTableOptions = {
    state: { pagination: { pageIndex: pagination.page, pageSize: pagination.size } },
    manualPagination: true as const,
    pageCount: Math.max(pagination.totalPages, 1),
    autoResetPageIndex: false as const,
    getCoreRowModel: getCoreRowModel(),
    onPaginationChange: handlePaginationChange,
  };

  const sharedMeta = {
    totalElements: pagination.totalElements,
    onApprove: (request: ApprovalTarget) => setApproveTarget({ id: request.id, productName: request.productName }),
    onReject: (request: ApprovalTarget) => setRejectTarget({ id: request.id, productName: request.productName }),
  };

  /** 메뉴모음컷은 메뉴명이 없어 다이얼로그 제목에 넣을 이름을 가게명으로 대체한다. */
  const menuCollectionMeta = {
    totalElements: pagination.totalElements,
    onApprove: (request: MenuCollectionImageRequestItem) =>
      setApproveTarget({ id: request.id, productName: request.shopName }),
    onReject: (request: MenuCollectionImageRequestItem) =>
      setRejectTarget({ id: request.id, productName: request.shopName }),
    imageLoadFailedIds,
    onImageLoadError: handleImageLoadError,
  };

  // 두 테이블 인스턴스를 항상 만들어 두는 대신, 렌더 중인 탭의 것만 만든다.
  // useReactTable 은 훅이므로 조건 분기 없이 둘 다 호출하고 사용만 가른다.
  const imageTable = useReactTable<ProductImageChangeRequestItem>({
    ...sharedTableOptions,
    data: props.tab === "image" ? props.requests : [],
    columns: productImageReviewColumns,
    getRowId: (row) => String(row.id),
    meta: {
      ...sharedMeta,
      imageLoadFailedIds,
      onImageLoadError: handleImageLoadError,
    } satisfies ProductApprovalsTableMeta<ProductImageChangeRequestItem>,
  });

  const vegetarianTable = useReactTable<ProductVegetarianRequestItem>({
    ...sharedTableOptions,
    data: props.tab === "vegetarian" ? props.requests : [],
    columns: productVegetarianReviewColumns,
    getRowId: (row) => String(row.id),
    meta: sharedMeta satisfies ProductApprovalsTableMeta<ProductVegetarianRequestItem>,
  });

  const menuCollectionTable = useReactTable<MenuCollectionImageRequestItem>({
    ...sharedTableOptions,
    data: props.tab === "menuCollection" ? props.requests : [],
    columns: menuCollectionReviewColumns,
    getRowId: (row) => String(row.id),
    meta: menuCollectionMeta satisfies ProductApprovalsTableMeta<MenuCollectionImageRequestItem>,
  });

  const representativeTable = useReactTable<ProductRepresentativeRequestItem>({
    ...sharedTableOptions,
    data: props.tab === "representative" ? props.requests : [],
    columns: representativeReviewColumns,
    getRowId: (row) => String(row.id),
    meta: {
      ...sharedMeta,
      imageLoadFailedIds,
      onImageLoadError: handleImageLoadError,
    } satisfies ProductApprovalsTableMeta<ProductRepresentativeRequestItem>,
  });

  const storePriceTable = useReactTable<StorePriceVerificationRequestItem>({
    ...sharedTableOptions,
    data: props.tab === "storePrice" ? props.requests : [],
    columns: storePriceReviewColumns,
    getRowId: (row) => String(row.id),
    // 매장가격 인증은 메뉴 여러 개가 한 요청에 묶여 있어 메뉴명 하나로 대표할 수 없으므로,
    // 메뉴모음컷과 마찬가지로 다이얼로그 제목에는 가게명을 넣는다.
    meta: {
      ...sharedMeta,
      onApprove: (request: StorePriceVerificationRequestItem) =>
        setApproveTarget({ id: request.id, productName: request.shopName }),
      onReject: (request: StorePriceVerificationRequestItem) =>
        setRejectTarget({ id: request.id, productName: request.shopName }),
      imageLoadFailedIds,
      onImageLoadError: handleImageLoadError,
    } satisfies ProductApprovalsTableMeta<StorePriceVerificationRequestItem>,
  });

  return (
    <Card>
      <CardHeader className="border-b">
        <CardTitle className="text-xl leading-none">{PRODUCT_APPROVAL_COPY.PAGE_TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{PRODUCT_APPROVAL_COPY.PAGE_DESCRIPTION}</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4 px-0">
        <div className="flex flex-wrap items-center justify-between gap-2 px-4 pt-2">
          <Tabs
            value={tab}
            onValueChange={(value) => pushParams({ tab: value as Props["tab"], page: 0 })}
            className="w-auto"
          >
            <TabsList>
              <TabsTrigger value="image" disabled={isPending}>
                {PRODUCT_APPROVAL_COPY.TAB_IMAGE}
              </TabsTrigger>
              <TabsTrigger value="vegetarian" disabled={isPending}>
                {PRODUCT_APPROVAL_COPY.TAB_VEGETARIAN}
              </TabsTrigger>
              <TabsTrigger value="menuCollection" disabled={isPending}>
                {PRODUCT_APPROVAL_COPY.TAB_MENU_COLLECTION}
              </TabsTrigger>
              <TabsTrigger value="representative" disabled={isPending}>
                {PRODUCT_APPROVAL_COPY.TAB_REPRESENTATIVE}
              </TabsTrigger>
              <TabsTrigger value="storePrice" disabled={isPending}>
                {PRODUCT_APPROVAL_COPY.TAB_STORE_PRICE}
              </TabsTrigger>
            </TabsList>
          </Tabs>

          <div className="flex flex-wrap items-center gap-2">
            {/* 검수의 주 용도는 미처리 건 처리이므로 빠른 필터를 두되, 기본값은 전체로 남겨 이력 조회도 같은 화면에서 되게 한다. */}
            <Button
              size="sm"
              variant={statusValue === "PENDING" ? "default" : "outline"}
              disabled={isPending}
              onClick={() => pushParams({ page: 0, status: "PENDING" })}
            >
              {PRODUCT_APPROVAL_COPY.PENDING_ONLY_FILTER}
            </Button>
            <Select
              value={statusValue}
              onValueChange={(value) => pushParams({ page: 0, status: value })}
              disabled={isPending}
            >
              <SelectTrigger size="sm">
                <span className="text-muted-foreground">상태:</span>
                <SelectValue />
              </SelectTrigger>
              <SelectContent position="popper" align="end">
                <SelectGroup>
                  <SelectItem value={ALL_STATUS}>전체</SelectItem>
                  {APPROVAL_STATUS_OPTIONS.map((option) => (
                    <SelectItem key={option} value={option}>
                      {APPROVAL_STATUS_LABEL[option]}
                    </SelectItem>
                  ))}
                </SelectGroup>
              </SelectContent>
            </Select>
          </div>
        </div>

        {tab === "image" && <ProductApprovalsTable table={imageTable} isPending={isPending} />}
        {tab === "vegetarian" && <ProductApprovalsTable table={vegetarianTable} isPending={isPending} />}
        {tab === "menuCollection" && <ProductApprovalsTable table={menuCollectionTable} isPending={isPending} />}
        {tab === "representative" && <ProductApprovalsTable table={representativeTable} isPending={isPending} />}
        {tab === "storePrice" && <ProductApprovalsTable table={storePriceTable} isPending={isPending} />}
      </CardContent>

      <ProductApproveDialog
        kind={tab}
        requestId={approveTarget?.id ?? null}
        productName={approveTarget?.productName}
        onOpenChange={(open) => !open && setApproveTarget(null)}
        onSuccess={handleRefresh}
      />
      <ProductRejectDialog
        kind={tab}
        requestId={rejectTarget?.id ?? null}
        productName={rejectTarget?.productName}
        onOpenChange={(open) => !open && setRejectTarget(null)}
        onSuccess={handleRefresh}
      />
    </Card>
  );
}
