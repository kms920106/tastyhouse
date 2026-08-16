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
import type { ReviewListItem } from "@/feature/review/domain";
import { REVIEW_PAGE_COPY, REVIEW_VISIBILITY_COPY } from "@/feature/review/message";

import { DeleteReviewDialog } from "./delete-review-dialog";
import { ReviewCommentsSheet } from "./review-comments-sheet";
import { ReviewDetailSheet } from "./review-detail-sheet";
import { ReviewHiddenDialog } from "./review-hidden-dialog";
import { type ReviewsTableMeta, reviewsColumns } from "./reviews-columns";
import { ReviewsTable } from "./reviews-table";

interface Props {
  reviews: ReviewListItem[];
  pagination: ApiPagination;
  initialShopId?: number;
  initialProductId?: number;
  initialMemberId?: number;
  initialHidden?: boolean;
  initialOwnerOnly?: boolean;
  initialContent?: string;
  initialMinRating?: number;
  initialMaxRating?: number;
}

export function Reviews({
  reviews,
  pagination,
  initialShopId,
  initialProductId,
  initialMemberId,
  initialHidden,
  initialOwnerOnly,
  initialContent,
  initialMinRating,
  initialMaxRating,
}: Props) {
  const router = useRouter();

  const searchParams = useSearchParams();

  const [detailId, setDetailId] = React.useState<number | null>(null);
  const [commentsTarget, setCommentsTarget] = React.useState<ReviewListItem | null>(null);
  const [hiddenTarget, setHiddenTarget] = React.useState<ReviewListItem | null>(null);
  const [deleteTarget, setDeleteTarget] = React.useState<ReviewListItem | null>(null);
  const [isPending, startTransition] = React.useTransition();

  const [shopIdInput, setShopIdInput] = React.useState(initialShopId === undefined ? "" : String(initialShopId));
  const [productIdInput, setProductIdInput] = React.useState(
    initialProductId === undefined ? "" : String(initialProductId),
  );
  const [memberIdInput, setMemberIdInput] = React.useState(
    initialMemberId === undefined ? "" : String(initialMemberId),
  );
  const [contentInput, setContentInput] = React.useState(initialContent ?? "");
  const [hiddenInput, setHiddenInput] = React.useState(initialHidden === undefined ? "all" : String(initialHidden));
  const [ownerOnlyInput, setOwnerOnlyInput] = React.useState(
    initialOwnerOnly === undefined ? "all" : String(initialOwnerOnly),
  );
  const [minRatingInput, setMinRatingInput] = React.useState(
    initialMinRating === undefined ? "" : String(initialMinRating),
  );
  const [maxRatingInput, setMaxRatingInput] = React.useState(
    initialMaxRating === undefined ? "" : String(initialMaxRating),
  );

  function pushParams(next: {
    page?: number;
    size?: number;
    shopId?: string;
    productId?: string;
    memberId?: string;
    content?: string;
    hidden?: string;
    ownerOnly?: string;
    minRating?: string;
    maxRating?: string;
  }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.page !== undefined) params.set("page", String(next.page));
    if (next.size !== undefined) params.set("size", String(next.size));
    for (const key of [
      "shopId",
      "productId",
      "memberId",
      "content",
      "hidden",
      "ownerOnly",
      "minRating",
      "maxRating",
    ] as const) {
      if (next[key] === undefined) continue;
      const v = next[key];
      if (!v || v === "all") params.delete(key);
      else params.set(key, v);
    }
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  function handleSearch(override?: { hidden?: string; ownerOnly?: string }) {
    pushParams({
      page: 0,
      shopId: shopIdInput,
      productId: productIdInput,
      memberId: memberIdInput,
      content: contentInput,
      hidden: override?.hidden ?? hiddenInput,
      ownerOnly: override?.ownerOnly ?? ownerOnlyInput,
      minRating: minRatingInput,
      maxRating: maxRatingInput,
    });
  }

  function handleReset() {
    setShopIdInput("");
    setProductIdInput("");
    setMemberIdInput("");
    setContentInput("");
    setHiddenInput("all");
    setOwnerOnlyInput("all");
    setMinRatingInput("");
    setMaxRatingInput("");
    pushParams({
      page: 0,
      shopId: "",
      productId: "",
      memberId: "",
      content: "",
      hidden: "all",
      ownerOnly: "all",
      minRating: "",
      maxRating: "",
    });
  }

  const table = useReactTable({
    data: reviews,
    columns: reviewsColumns,
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
      onView: (review) => setDetailId(review.id),
      onManageComments: (review) => setCommentsTarget(review),
      onToggleHidden: (review) => setHiddenTarget(review),
      onDelete: (review) => setDeleteTarget(review),
    } satisfies ReviewsTableMeta,
  });

  return (
    <Card>
      <CardHeader className="border-b">
        <CardTitle className="text-xl leading-none">{REVIEW_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{REVIEW_PAGE_COPY.DESCRIPTION}</CardDescription>
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
            className="w-24"
            type="number"
            min={1}
            placeholder="매장 ID"
            value={shopIdInput}
            onChange={(e) => setShopIdInput(e.target.value)}
            disabled={isPending}
          />
          <Input
            className="w-24"
            type="number"
            min={1}
            placeholder="상품 ID"
            value={productIdInput}
            onChange={(e) => setProductIdInput(e.target.value)}
            disabled={isPending}
          />
          <Input
            className="w-24"
            type="number"
            min={1}
            placeholder="회원 ID"
            value={memberIdInput}
            onChange={(e) => setMemberIdInput(e.target.value)}
            disabled={isPending}
          />
          <Input
            className="w-48"
            placeholder="리뷰 내용 검색"
            value={contentInput}
            onChange={(e) => setContentInput(e.target.value)}
            disabled={isPending}
          />
          <Input
            className="w-20"
            type="number"
            min={0}
            max={5}
            step={0.1}
            placeholder="최소 평점"
            value={minRatingInput}
            onChange={(e) => setMinRatingInput(e.target.value)}
            disabled={isPending}
          />
          <Input
            className="w-20"
            type="number"
            min={0}
            max={5}
            step={0.1}
            placeholder="최대 평점"
            value={maxRatingInput}
            onChange={(e) => setMaxRatingInput(e.target.value)}
            disabled={isPending}
          />
          <Select
            value={hiddenInput}
            onValueChange={(value) => {
              setHiddenInput(value);
              handleSearch({ hidden: value });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">숨김 여부:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value="all">전체</SelectItem>
                <SelectItem value="true">숨김</SelectItem>
                <SelectItem value="false">노출</SelectItem>
              </SelectGroup>
            </SelectContent>
          </Select>
          <Select
            value={ownerOnlyInput}
            onValueChange={(value) => {
              setOwnerOnlyInput(value);
              handleSearch({ ownerOnly: value });
            }}
            disabled={isPending}
          >
            <SelectTrigger size="sm">
              <span className="text-muted-foreground">{REVIEW_VISIBILITY_COPY.LABEL}:</span>
              <SelectValue />
            </SelectTrigger>
            <SelectContent position="popper" align="start">
              <SelectGroup>
                <SelectItem value="all">{REVIEW_VISIBILITY_COPY.FILTER_ALL}</SelectItem>
                <SelectItem value="true">{REVIEW_VISIBILITY_COPY.OWNER_ONLY}</SelectItem>
                <SelectItem value="false">{REVIEW_VISIBILITY_COPY.PUBLIC}</SelectItem>
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
        <ReviewsTable table={table} isPending={isPending} />
      </CardContent>
      <ReviewDetailSheet reviewId={detailId} onOpenChange={(open) => !open && setDetailId(null)} />
      <ReviewCommentsSheet review={commentsTarget} onOpenChange={(open) => !open && setCommentsTarget(null)} />
      <ReviewHiddenDialog review={hiddenTarget} onOpenChange={(open) => !open && setHiddenTarget(null)} />
      <DeleteReviewDialog review={deleteTarget} onOpenChange={(open) => !open && setDeleteTarget(null)} />
    </Card>
  );
}
