"use client";

import * as React from "react";

import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { requestThumbnailChangeAction } from "@/feature/shop/actions";
import { APPROVAL_STATUS_LABEL } from "@/feature/shop/constants";
import type { ShopImageStatus } from "@/feature/shop/domain";
import { SHOP_BASIC_COPY, SHOP_MESSAGE } from "@/feature/shop/message";

import { ShopImagePreview } from "./shop-image-preview";
import { useImageFileSelect } from "./use-image-file-select";

interface ThumbnailImageCardProps {
  shopId: number;
  thumbnailImageUrl: string | null;
  thumbnailStatus: ShopImageStatus;
}

export function ThumbnailImageCard({ shopId, thumbnailImageUrl, thumbnailStatus }: ThumbnailImageCardProps) {
  const inputRef = React.useRef<HTMLInputElement>(null);
  const [isPending, startTransition] = React.useTransition();
  const { select, isValidating } = useImageFileSelect("thumbnail");

  const isBusy = isPending || isValidating;
  const hasPendingRequest = thumbnailStatus.requests.some((request) => request.status === "PENDING");
  const rejectedRequests = thumbnailStatus.requests.filter(
    (request) => request.status === "REJECTED" && request.rejectReason,
  );

  // 승인 완료된 현재 이미지가 있으면 그것을, 없으면 가게 상세의 URL 을 쓴다.
  const currentUrl = thumbnailStatus.currentImageUrl ?? thumbnailImageUrl;

  function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file) return;

    startTransition(async () => {
      const selected = await select(file);
      if ("error" in selected) {
        toast.error(selected.error);
        return;
      }

      const formData = new FormData();
      formData.append("file", selected.file);
      const { success, message } = await requestThumbnailChangeAction(shopId, formData);
      if (success) {
        toast.success(SHOP_MESSAGE.THUMBNAIL_REQUEST_SUCCESS);
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  }

  return (
    <div className="flex items-start justify-between gap-4 border-b py-4">
      <div className="flex min-w-0 flex-1 gap-4">
        <div className="w-32 shrink-0">
          <ShopImagePreview src={currentUrl} alt={SHOP_BASIC_COPY.THUMBNAIL_TITLE} />
        </div>
        <div className="flex min-w-0 flex-1 flex-col gap-1">
          <div className="flex flex-wrap items-center gap-2">
            <span className="font-medium text-sm">{SHOP_BASIC_COPY.THUMBNAIL_TITLE}</span>
            {hasPendingRequest && <Badge variant="secondary">{SHOP_BASIC_COPY.IMAGE_PENDING_BADGE}</Badge>}
          </div>
          <span className="text-muted-foreground text-xs leading-snug">{SHOP_BASIC_COPY.THUMBNAIL_DESCRIPTION}</span>
          {rejectedRequests.length > 0 && (
            <ul className="mt-1 space-y-1">
              {rejectedRequests.map((request) => (
                <li key={request.id} className="text-destructive text-xs">
                  {APPROVAL_STATUS_LABEL.REJECTED} · {request.rejectReason}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      <input ref={inputRef} type="file" accept="image/jpeg,image/png" className="hidden" onChange={handleFileChange} />
      <Button type="button" size="sm" variant="outline" disabled={isBusy} onClick={() => inputRef.current?.click()}>
        {isBusy ? "업로드 중..." : SHOP_BASIC_COPY.CHANGE}
      </Button>
    </div>
  );
}
