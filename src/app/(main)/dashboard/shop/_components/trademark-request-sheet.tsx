"use client";

import * as React from "react";

import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Field, FieldDescription, FieldGroup, FieldLabel } from "@/components/ui/field";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { requestTrademarkChangeAction } from "@/feature/shop/actions";
import { APPROVAL_STATUS_LABEL } from "@/feature/shop/constants";
import type { ShopImageStatus } from "@/feature/shop/domain";
import { resolveFileUrl } from "@/feature/shop/image";
import { SHOP_BASIC_COPY, SHOP_MESSAGE } from "@/feature/shop/message";

import { ShopImagePreview } from "./shop-image-preview";
import { useImageFileSelect } from "./use-image-file-select";

interface TrademarkRequestSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  trademarkImageFileId: number | null;
  trademarkStatus: ShopImageStatus;
}

export function TrademarkRequestSheet({
  open,
  onOpenChange,
  shopId,
  trademarkImageFileId,
  trademarkStatus,
}: TrademarkRequestSheetProps) {
  const inputRef = React.useRef<HTMLInputElement>(null);
  const [isPending, startTransition] = React.useTransition();
  const { select, isValidating } = useImageFileSelect("trademark");
  const [selected, setSelected] = React.useState<{ file: File; previewUrl: string } | null>(null);

  React.useEffect(() => {
    if (!open) setSelected(null);
  }, [open]);

  // blob: URL 은 명시적으로 해제해야 하므로 선택이 교체·해제될 때 정리한다.
  React.useEffect(() => {
    if (!selected) return;
    return () => URL.revokeObjectURL(selected.previewUrl);
  }, [selected]);

  const isBusy = isPending || isValidating;
  const hasPendingRequest = trademarkStatus.requests.some((request) => request.status === "PENDING");
  const currentUrl = resolveFileUrl(trademarkStatus.currentImageFileId ?? trademarkImageFileId);
  const previewUrl = selected?.previewUrl ?? currentUrl;

  function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file) return;

    startTransition(async () => {
      const result = await select(file);
      if ("error" in result) {
        toast.error(result.error);
        return;
      }
      setSelected({ file: result.file, previewUrl: URL.createObjectURL(result.file) });
    });
  }

  function handleSubmit() {
    if (!selected) {
      toast.error(SHOP_MESSAGE.IMAGE_REQUIRED);
      return;
    }

    startTransition(async () => {
      const formData = new FormData();
      formData.append("file", selected.file);
      const { success, message } = await requestTrademarkChangeAction(shopId, formData);
      if (success) {
        toast.success(SHOP_MESSAGE.TRADEMARK_REQUEST_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{SHOP_BASIC_COPY.TRADEMARK_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_BASIC_COPY.TRADEMARK_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex-1 overflow-y-auto px-4">
          <FieldGroup className="gap-4">
            {trademarkStatus.requests.length > 0 && (
              <Field className="gap-1.5">
                <FieldLabel>{SHOP_BASIC_COPY.IMAGE_REQUEST_HISTORY}</FieldLabel>
                <ul className="space-y-1.5">
                  {trademarkStatus.requests.map((request) => (
                    <li key={request.id} className="flex flex-col gap-0.5">
                      <Badge variant={request.status === "REJECTED" ? "destructive" : "secondary"} className="w-fit">
                        {APPROVAL_STATUS_LABEL[request.status]}
                      </Badge>
                      {request.status === "REJECTED" && request.rejectReason && (
                        <FieldDescription className="text-destructive">{request.rejectReason}</FieldDescription>
                      )}
                    </li>
                  ))}
                </ul>
              </Field>
            )}

            <Field className="gap-1.5">
              <FieldLabel>{SHOP_BASIC_COPY.TRADEMARK_TITLE}</FieldLabel>
              {hasPendingRequest && (
                <Badge variant="secondary" className="w-fit">
                  {SHOP_BASIC_COPY.IMAGE_PENDING_BADGE}
                </Badge>
              )}
              <div className="w-40">
                <ShopImagePreview src={previewUrl} alt={SHOP_BASIC_COPY.TRADEMARK_TITLE} fit="contain" />
              </div>
              <input ref={inputRef} type="file" accept="image/jpeg" className="hidden" onChange={handleFileChange} />
              <Button
                type="button"
                size="sm"
                variant="outline"
                className="w-fit"
                disabled={isBusy}
                onClick={() => inputRef.current?.click()}
              >
                {isValidating ? "업로드 중..." : "이미지 첨부"}
              </Button>
            </Field>
          </FieldGroup>
        </div>

        <SheetFooter>
          <Button type="button" onClick={handleSubmit} disabled={isBusy || !selected}>
            {isPending ? "요청 중..." : "변경 요청"}
          </Button>
          <SheetClose asChild>
            <Button variant="outline" disabled={isBusy}>
              취소
            </Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
