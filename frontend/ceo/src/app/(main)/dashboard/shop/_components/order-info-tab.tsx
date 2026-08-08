"use client";

import { Store } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty";
import { Separator } from "@/components/ui/separator";
import type { ShopOrderAvailability } from "@/feature/shop/domain";
import { SHOP_ORDER_COPY } from "@/feature/shop/message";

interface OrderInfoTabProps {
  /** 주문가능 상태. 조회 실패 시 undefined 로 넘어와 이 탭만 실패 문구를 보여준다 */
  orderAvailability?: ShopOrderAvailability;
}

/** 정상 = secondary, 이상 = destructive — shop-status-overview 와 동일한 배지 컨벤션 */
function OrderableBadge({ orderable }: { orderable: boolean }) {
  return (
    <Badge variant={orderable ? "secondary" : "destructive"}>
      {orderable ? SHOP_ORDER_COPY.AVAILABLE_BADGE : SHOP_ORDER_COPY.UNAVAILABLE_BADGE}
    </Badge>
  );
}

export function OrderInfoTab({ orderAvailability }: OrderInfoTabProps) {
  // 주문가능 상태 조회만 실패한 경우 — 다른 탭을 막지 않고 이 탭에서만 실패를 알린다
  if (!orderAvailability) {
    return (
      <div className="py-4">
        <span className="text-destructive text-sm">{SHOP_ORDER_COPY.ORDER_AVAILABILITY_LOAD_FAILED}</span>
      </div>
    );
  }

  const { orderable, unavailableReasonName, orderMethods } = orderAvailability;

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-col gap-1 py-4">
        <div className="flex flex-wrap items-center gap-2">
          <span className="font-medium text-sm">{SHOP_ORDER_COPY.SHOP_STATUS_TITLE}</span>
          <OrderableBadge orderable={orderable} />
          {/* 사유 문구는 서버가 완성해 내려주므로 그대로 렌더한다 */}
          {!orderable && unavailableReasonName && (
            <span className="text-muted-foreground text-sm">{unavailableReasonName}</span>
          )}
        </div>
        <span className="text-muted-foreground text-xs leading-snug">{SHOP_ORDER_COPY.SHOP_STATUS_DESCRIPTION}</span>
      </div>

      <Separator />

      <div className="flex flex-col">
        <div className="flex flex-col gap-1 pb-2">
          <span className="font-medium text-sm">{SHOP_ORDER_COPY.ORDER_METHOD_TITLE}</span>
          <span className="text-muted-foreground text-xs leading-snug">{SHOP_ORDER_COPY.ORDER_METHOD_DESCRIPTION}</span>
        </div>

        {orderMethods.length === 0 ? (
          <Empty>
            <EmptyHeader>
              <EmptyMedia variant="icon">
                <Store />
              </EmptyMedia>
              <EmptyTitle>{SHOP_ORDER_COPY.ORDER_METHOD_EMPTY_TITLE}</EmptyTitle>
              <EmptyDescription>{SHOP_ORDER_COPY.ORDER_METHOD_EMPTY_DESCRIPTION}</EmptyDescription>
            </EmptyHeader>
          </Empty>
        ) : (
          orderMethods.map((item) => (
            // 조회 전용 탭이므로 setting-row 의 액션 버튼을 쓰지 않고 같은 행 레이아웃만 따른다
            <div
              key={item.orderMethod}
              className="flex items-start justify-between gap-4 border-b py-4 last:border-b-0"
            >
              <div className="flex min-w-0 flex-1 flex-col gap-1">
                <span className="font-medium text-sm">{item.orderMethodName}</span>
                {!item.orderable && item.unavailableReasonName && (
                  <span className="text-muted-foreground text-xs leading-snug">{item.unavailableReasonName}</span>
                )}
              </div>
              <OrderableBadge orderable={item.orderable} />
            </div>
          ))
        )}
      </div>
    </div>
  );
}
