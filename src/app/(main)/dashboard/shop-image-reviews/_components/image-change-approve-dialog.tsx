"use client";

import * as React from "react";

import { toast } from "sonner";

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { approveImageChangeRequestAction } from "@/feature/shop/actions";
import { SHOP_IMAGE_TYPE_LABEL } from "@/feature/shop/constants";
import type { ShopImageChangeRequest } from "@/feature/shop/domain";
import { SHOP_MESSAGE } from "@/feature/shop/message";

interface ImageChangeApproveDialogProps {
  request: ShopImageChangeRequest | null;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

export function ImageChangeApproveDialog({ request, onOpenChange, onSuccess }: ImageChangeApproveDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleApprove() {
    if (!request) return;
    startTransition(async () => {
      const { success, message } = await approveImageChangeRequestAction(request.id);
      if (success) {
        toast.success(SHOP_MESSAGE.IMAGE_CHANGE_APPROVE_SUCCESS);
        onOpenChange(false);
        onSuccess();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={request != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>이미지 변경요청을 승인하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {request
              ? `가게 ID ${request.shopId}의 ${SHOP_IMAGE_TYPE_LABEL[request.imageType]} 변경이 즉시 반영됩니다.`
              : ""}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isPending}>취소</AlertDialogCancel>
          <AlertDialogAction
            onClick={(event) => {
              event.preventDefault();
              handleApprove();
            }}
            disabled={isPending}
          >
            {isPending ? "처리 중..." : "승인"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
