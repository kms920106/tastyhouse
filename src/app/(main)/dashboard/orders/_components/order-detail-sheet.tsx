"use client";

import * as React from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { Skeleton } from "@/components/ui/skeleton";
import { fetchOrderAction } from "@/feature/order/actions";
import type { OrderDetail } from "@/feature/order/domain";
import {
  formatPoint,
  formatWon,
  orderMethodLabel,
  paymentStatusBadgeVariant,
  paymentStatusLabel,
} from "@/feature/order/format";
import { ORDER_MESSAGE } from "@/feature/order/message";
import { formatDateTime } from "@/lib/date";

interface OrderDetailSheetProps {
  /** 조회할 주문 ID. null 이면 닫힌 상태. */
  orderId: number | null;
  onOpenChange: (open: boolean) => void;
}

export function OrderDetailSheet({ orderId, onOpenChange }: OrderDetailSheetProps) {
  const [detail, setDetail] = React.useState<OrderDetail | null>(null);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    if (orderId == null) {
      return;
    }

    let active = true;
    setIsLoading(true);
    setError(null);
    setDetail(null);

    void fetchOrderAction(orderId).then((result) => {
      const { success, message, data } = result;

      if (!active) return;
      if (success && data) {
        setDetail(data);
      } else {
        setError(message ?? ORDER_MESSAGE.DETAIL_LOAD_FAILED);
      }
      setIsLoading(false);
    });

    return () => {
      active = false;
    };
  }, [orderId]);

  return (
    <Sheet open={orderId != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-2xl">
        <SheetHeader>
          <SheetTitle>주문 상세</SheetTitle>
          <SheetDescription>주문의 상세 정보를 확인합니다.</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-5 overflow-y-auto px-4">
          {isLoading ? (
            <div className="space-y-3">
              <Skeleton className="h-6 w-3/4" />
              <Skeleton className="h-4 w-1/3" />
              <Skeleton className="h-32 w-full" />
            </div>
          ) : error ? (
            <p className="text-destructive text-sm">{error}</p>
          ) : detail ? (
            <>
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h3 className="font-semibold text-lg leading-snug tabular-nums">{detail.orderNumber}</h3>
                  <p className="text-muted-foreground text-sm">
                    {orderMethodLabel(detail.orderMethod)} · {detail.shopName}
                  </p>
                </div>
                <Badge variant={paymentStatusBadgeVariant(detail.paymentStatus)}>
                  {paymentStatusLabel(detail.paymentStatus)}
                </Badge>
              </div>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">매장 전화번호</dt>
                <dd className="tabular-nums">{detail.shopPhoneNumber}</dd>
                <dt className="text-muted-foreground">주문자명</dt>
                <dd>{detail.ordererName}</dd>
                <dt className="text-muted-foreground">주문자 전화번호</dt>
                <dd className="tabular-nums">{detail.ordererPhone}</dd>
                <dt className="text-muted-foreground">주문자 이메일</dt>
                <dd>{detail.ordererEmail ?? "-"}</dd>
              </dl>

              <Separator />

              <div className="space-y-3">
                <h4 className="font-medium text-sm">주문 상품</h4>
                <div className="space-y-3">
                  {detail.orderProducts.map((product) => (
                    <div key={product.id} className="flex gap-3 rounded-md border p-3">
                      {/* biome-ignore lint/performance/noImgElement: CDN 상품 이미지 미리보기 */}
                      <img
                        src={product.imageUrl}
                        alt={product.name}
                        className="size-14 shrink-0 rounded object-cover"
                      />
                      <div className="min-w-0 flex-1 space-y-1">
                        <div className="flex items-center justify-between gap-2">
                          <span className="line-clamp-1 font-medium">{product.name}</span>
                          <span className="shrink-0 text-muted-foreground text-sm">x{product.quantity}</span>
                        </div>
                        {product.selectedOptions.length > 0 ? (
                          <ul className="space-y-0.5 text-muted-foreground text-xs">
                            {product.selectedOptions.map((option) => (
                              <li key={`${option.groupId}-${option.optionId}`}>
                                {option.groupName}: {option.optionName}
                                {option.additionalPrice > 0 ? ` (+${formatWon(option.additionalPrice)})` : ""}
                              </li>
                            ))}
                          </ul>
                        ) : null}
                        <div className="flex items-center justify-between text-sm">
                          <span className="text-muted-foreground">
                            {product.discountPrice != null ? (
                              <>
                                <span className="mr-1 line-through">{formatWon(product.originalPrice)}</span>
                                {formatWon(product.discountPrice)}
                              </>
                            ) : (
                              formatWon(product.originalPrice)
                            )}
                          </span>
                          <span className="font-medium tabular-nums">{formatWon(product.totalPrice)}</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">상품 금액</dt>
                <dd className="text-right tabular-nums">{formatWon(detail.totalProductAmount)}</dd>
                <dt className="text-muted-foreground">상품 할인</dt>
                <dd className="text-right tabular-nums">-{formatWon(detail.productDiscountAmount)}</dd>
                <dt className="text-muted-foreground">쿠폰 할인</dt>
                <dd className="text-right tabular-nums">-{formatWon(detail.couponDiscountAmount)}</dd>
                <dt className="text-muted-foreground">포인트 할인</dt>
                <dd className="text-right tabular-nums">-{formatWon(detail.pointDiscountAmount)}</dd>
                <dt className="font-medium">최종 결제 금액</dt>
                <dd className="text-right font-medium tabular-nums">{formatWon(detail.finalAmount)}</dd>
                <dt className="text-muted-foreground">사용 포인트</dt>
                <dd className="text-right tabular-nums">{formatPoint(detail.usedPoint)}</dd>
                <dt className="text-muted-foreground">적립 포인트</dt>
                <dd className="text-right tabular-nums">{formatPoint(detail.earnedPoint)}</dd>
              </dl>

              <Separator />

              <div className="space-y-2">
                <h4 className="font-medium text-sm">결제 정보</h4>
                {detail.payment ? (
                  <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                    <dt className="text-muted-foreground">결제 수단</dt>
                    <dd>{detail.payment.paymentMethod}</dd>
                    <dt className="text-muted-foreground">결제 상태</dt>
                    <dd>
                      <Badge variant={paymentStatusBadgeVariant(detail.payment.paymentStatus)}>
                        {paymentStatusLabel(detail.payment.paymentStatus)}
                      </Badge>
                    </dd>
                    <dt className="text-muted-foreground">결제 금액</dt>
                    <dd className="tabular-nums">{formatWon(detail.payment.amount)}</dd>
                    <dt className="text-muted-foreground">카드사</dt>
                    <dd>{detail.payment.cardCompany ?? "-"}</dd>
                    <dt className="text-muted-foreground">카드번호</dt>
                    <dd className="tabular-nums">{detail.payment.cardNumber ?? "-"}</dd>
                    <dt className="text-muted-foreground">승인 일시</dt>
                    <dd className="tabular-nums">{formatDateTime(detail.payment.approvedAt)}</dd>
                    {detail.payment.receiptUrl ? (
                      <>
                        <dt className="text-muted-foreground">영수증</dt>
                        <dd>
                          <a
                            href={detail.payment.receiptUrl}
                            target="_blank"
                            rel="noreferrer"
                            className="text-primary underline underline-offset-2"
                          >
                            영수증 보기
                          </a>
                        </dd>
                      </>
                    ) : null}
                  </dl>
                ) : (
                  <p className="text-muted-foreground text-sm">결제 정보 없음</p>
                )}
              </div>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">결제 승인 일시</dt>
                <dd className="tabular-nums">{formatDateTime(detail.approvedAt)}</dd>
                <dt className="text-muted-foreground">주문 생성 일시</dt>
                <dd className="tabular-nums">{formatDateTime(detail.createdAt)}</dd>
              </dl>
            </>
          ) : null}
        </div>

        <SheetFooter>
          <SheetClose asChild>
            <Button variant="outline">닫기</Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
