"use client";
"use no memo";

import * as React from "react";

import type { ColumnDef } from "@tanstack/react-table";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { formatPrice } from "@/feature/product/format";
import { PRODUCT_APPROVAL_COPY } from "@/feature/product/message";
import { fetchStorePriceVerificationRequestDetailAction } from "@/feature/shop/actions";
import type {
  StorePriceVerificationRequestDetail,
  StorePriceVerificationRequestItem,
  StorePriceVerificationRequestTargetItem,
} from "@/feature/shop/domain";
import { cn } from "@/lib/utils";

import {
  ApprovalActionsCell,
  ApprovalStatusBadge,
  type ProductApprovalsTableMeta,
} from "./product-image-review-columns";

/**
 * 매장 가격표 이미지 썸네일. 다른 검수 탭의 `ReviewThumbnail`류와 동작은 같지만
 * 이쪽은 `StorePriceVerificationRequestItem`에 묶여 있어 공유할 수 없다.
 *
 * <p>이 이미지가 이 검수 탭의 유일한 1차 근거다 — 점주가 요청한 매장가가 실제로 매장에
 * 걸린 가격표와 일치하는지는 이 사진을 봐야만 확인할 수 있고, 나머지 항목(배달가·매장가 대조)은
 * 어디까지나 "앱 가격이 매장보다 비싸지 않은지"라는 보조 판단일 뿐이다. 그래서 이미지가 없거나
 * 로드에 실패하면 다른 탭과 동일하게 승인 버튼을 잠근다.
 */
function StorePriceListThumbnail({
  request,
  onLoadError,
}: {
  request: StorePriceVerificationRequestItem;
  onLoadError?: (requestId: number) => void;
}) {
  const [failed, setFailed] = React.useState(false);
  const { id, priceListImageUrl, shopName } = request;

  if (!priceListImageUrl || failed) {
    return <span className="text-muted-foreground text-xs">{PRODUCT_APPROVAL_COPY.IMAGE_MISSING}</span>;
  }

  return (
    <a href={priceListImageUrl} target="_blank" rel="noreferrer" className="block size-12" title={shopName}>
      {/* biome-ignore lint/performance/noImgElement: 검수 목록 썸네일 — 외부 스토리지 URL 이라 최적화 대상이 아니다 */}
      <img
        src={priceListImageUrl}
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

/**
 * 대상 메뉴 목록 — 메뉴명·배달가·요청 매장가를 한 행에 나란히 둔다.
 *
 * <p>이 탭의 핵심 반려 사유("매장보다 앱 가격이 높은 메뉴")는 배달가와 요청 매장가를
 * 비교해야만 판단할 수 있다. 두 값을 별도 컬럼으로 떼어 놓으면 검수자가 스크롤하며
 * 머릿속으로 대조해야 하므로, 반드시 같은 행·같은 화면에 붙여 렌더링한다.
 * 배달가가 매장가보다 높은(=반려 신호) 행은 색으로 표시해 훑어보기만 해도 걸리게 한다.
 */
function StorePriceTargetItemList({ items }: { items: StorePriceVerificationRequestTargetItem[] }) {
  if (items.length === 0) {
    return <span className="text-muted-foreground text-xs">-</span>;
  }

  return (
    // 한 요청에 메뉴가 수십 건 달릴 수 있어 다이얼로그가 화면을 넘기지 않도록 목록만 스크롤한다.
    <ul className="flex max-h-[60vh] flex-col gap-1.5 overflow-y-auto">
      {items.map((item) => {
        // 반려 신호: 배달가(앱 가격)가 요청 매장가보다 높으면 "매장보다 앱이 비싼" 상황이다.
        const isDeliveryHigherThanStore = item.deliveryPrice > item.storePrice;
        return (
          <li
            // 한 메뉴가 가격명마다 별도 항목으로 오므로 productId 만으로는 유일하지 않다.
            key={`${item.productId}-${item.priceName ?? ""}`}
            className={cn(
              "flex flex-wrap items-baseline gap-x-2 rounded-sm px-1.5 py-1 text-xs",
              isDeliveryHigherThanStore && "bg-destructive/10",
            )}
          >
            <span className="line-clamp-1 break-all font-medium">
              {item.productName}
              {item.priceName != null && <span className="text-muted-foreground"> ({item.priceName})</span>}
            </span>
            <span className="text-muted-foreground">배달가 {formatPrice(item.deliveryPrice)}</span>
            <span
              className={cn(
                "tabular-nums",
                isDeliveryHigherThanStore ? "font-semibold text-destructive" : "text-foreground",
              )}
            >
              → 요청 매장가 {formatPrice(item.storePrice)}
            </span>
            {item.applyPickupSamePrice && <span className="text-muted-foreground">(픽업가 동일 설정)</span>}
          </li>
        );
      })}
    </ul>
  );
}

/**
 * 대상 메뉴 대조표를 여는 셀.
 *
 * <p><b>목록 응답에는 대상 메뉴가 개수로만 담긴다.</b> 요청 1건에 메뉴가 N건 달려 있어 목록에 펼치면
 * 페이징이 깨지므로, 서버가 목록/상세를 나눠 두었다. 그래서 대조표는 이 셀에서 상세 조회를 눌러 받는다
 * — 목록 렌더링 시점에 요청마다 상세를 미리 당겨오면 한 페이지에 조회가 페이지 크기만큼 발생한다.
 */
function StorePriceTargetItemsCell({ request }: { request: StorePriceVerificationRequestItem }) {
  const [open, setOpen] = React.useState(false);
  const [detail, setDetail] = React.useState<StorePriceVerificationRequestDetail | null>(null);
  const [errorMessage, setErrorMessage] = React.useState<string | null>(null);
  const [isLoading, setIsLoading] = React.useState(false);

  function handleOpenChange(next: boolean) {
    setOpen(next);
    // 이미 받아 둔 대조표는 다시 받지 않는다 — 승인·반려 전까지 내용이 바뀌지 않는다.
    if (!next || detail !== null || isLoading) return;

    setIsLoading(true);
    setErrorMessage(null);
    fetchStorePriceVerificationRequestDetailAction(request.id)
      .then((result) => {
        if (result.success && result.data !== undefined) setDetail(result.data);
        else setErrorMessage(result.message ?? PRODUCT_APPROVAL_COPY.STORE_PRICE_ITEMS_LOAD_FAILED);
      })
      .finally(() => setIsLoading(false));
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        <Button variant="outline" size="sm" disabled={request.itemCount === 0}>
          {request.itemCount === 0 ? "-" : `${request.itemCount}건 보기`}
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>{`${request.shopName} — 대상 메뉴 ${request.itemCount}건`}</DialogTitle>
          <DialogDescription>{PRODUCT_APPROVAL_COPY.STORE_PRICE_ITEMS_DESCRIPTION}</DialogDescription>
        </DialogHeader>
        {isLoading && <span className="text-muted-foreground text-sm">불러오는 중…</span>}
        {!isLoading && errorMessage !== null && <span className="text-destructive text-sm">{errorMessage}</span>}
        {!isLoading && errorMessage === null && detail !== null && <StorePriceTargetItemList items={detail.items} />}
      </DialogContent>
    </Dialog>
  );
}

export const storePriceReviewColumns: ColumnDef<StorePriceVerificationRequestItem>[] = [
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
    // 어느 가게의 요청인지 바로 알 수 있도록 가게명과 ID 를 함께 둔다(다른 검수 탭과 동일 패턴).
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
    id: "priceListImageUrl",
    header: "가격표 이미지",
    cell: ({ row, table }) => (
      <StorePriceListThumbnail
        request={row.original}
        onLoadError={
          (table.options.meta as ProductApprovalsTableMeta<StorePriceVerificationRequestItem>).onImageLoadError
        }
      />
    ),
    enableSorting: false,
    size: 120,
    minSize: 100,
    maxSize: 140,
  },
  {
    id: "items",
    header: "대상 메뉴",
    // 목록에는 개수만 오므로 셀은 대조표를 여는 버튼이고, 실제 표는 다이얼로그가 폭을 갖는다.
    cell: ({ row }) => <StorePriceTargetItemsCell request={row.original} />,
    enableSorting: false,
    size: 140,
    minSize: 120,
    maxSize: 180,
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
      const meta = table.options.meta as ProductApprovalsTableMeta<StorePriceVerificationRequestItem>;
      return (
        <ApprovalActionsCell
          request={row.original}
          meta={meta}
          // 가격표 이미지를 보지 못하면 요청 내용과 실제 매장 가격이 일치하는지 확인할 방법이 없으므로
          // 다른 검수 탭과 동일하게 이미지 부재·로드 실패 시 승인만 잠근다(반려는 그대로 허용).
          canApprove={row.original.priceListImageUrl != null && !meta.imageLoadFailedIds?.has(row.original.id)}
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
