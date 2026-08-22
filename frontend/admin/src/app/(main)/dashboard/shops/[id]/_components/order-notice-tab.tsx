"use client";

import * as React from "react";

import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { fetchOrderNoticeAction, unhideOrderNoticeAction } from "@/feature/shop/actions";
import type { ShopOrderNotice } from "@/feature/shop/domain";
import { SHOP_MESSAGE } from "@/feature/shop/message";

import { OrderNoticeHideDialog } from "./order-notice-hide-dialog";

interface OrderNoticeTabProps {
  shopId: number;
}

export function OrderNoticeTab({ shopId }: OrderNoticeTabProps) {
  const [notice, setNotice] = React.useState<ShopOrderNotice | null>(null);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [isPending, startTransition] = React.useTransition();
  const [hideDialogShopId, setHideDialogShopId] = React.useState<number | null>(null);

  const load = React.useCallback(() => {
    setIsLoading(true);
    setError(null);
    void fetchOrderNoticeAction(shopId).then((result) => {
      setIsLoading(false);
      if (result.success && result.data) {
        setNotice(result.data);
      } else {
        setError(result.message ?? SHOP_MESSAGE.ORDER_NOTICE_LOAD_FAILED);
      }
    });
  }, [shopId]);

  React.useEffect(() => {
    load();
  }, [load]);

  function handleUnhide() {
    startTransition(async () => {
      const { success, message } = await unhideOrderNoticeAction(shopId);
      if (success) {
        toast.success(SHOP_MESSAGE.ORDER_NOTICE_UNHIDE_SUCCESS);
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  }

  if (error) return <p className="text-destructive text-sm">{error}</p>;
  if (isLoading) return <Skeleton className="h-20 w-full" />;

  return (
    <div className="space-y-3">
      <h4 className="font-medium text-sm">주문안내</h4>

      {notice?.content == null ? (
        <p className="text-muted-foreground text-sm">등록된 주문안내가 없습니다.</p>
      ) : (
        <div className="space-y-2 rounded-md border px-3 py-2 text-sm">
          <div className="flex items-center gap-2">
            <Badge variant={notice.hidden ? "destructive" : "default"}>
              {notice.hidden ? "게시중단" : "게시중"}
            </Badge>
          </div>
          <p className="whitespace-pre-wrap break-words">{notice.content}</p>
          {notice.hidden && notice.hiddenReason != null && (
            <p className="text-muted-foreground text-xs">게시중단 사유: {notice.hiddenReason}</p>
          )}
          <div>
            {notice.hidden ? (
              <Button type="button" size="sm" variant="outline" disabled={isPending} onClick={handleUnhide}>
                게시중단 해제
              </Button>
            ) : (
              <Button
                type="button"
                size="sm"
                variant="destructive"
                disabled={isPending}
                onClick={() => setHideDialogShopId(shopId)}
              >
                게시중단
              </Button>
            )}
          </div>
        </div>
      )}

      <OrderNoticeHideDialog
        shopId={hideDialogShopId}
        onOpenChange={(open) => {
          if (!open) setHideDialogShopId(null);
        }}
        onSuccess={() => {
          setHideDialogShopId(null);
          load();
        }}
      />
    </div>
  );
}
