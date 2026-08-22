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
import { approveProductImageChangeAction, approveProductVegetarianAction } from "@/feature/product/actions";
import { PRODUCT_APPROVAL_COPY, PRODUCT_APPROVAL_MESSAGE } from "@/feature/product/message";

/** 검수 종류 — 승인·반려 대상 API 와 안내 문구를 가른다. */
export type ProductApprovalKind = "image" | "vegetarian";

interface ProductApproveDialogProps {
  kind: ProductApprovalKind;
  /** 승인 대상 요청 ID. null 이면 다이얼로그를 닫는다 */
  requestId: number | null;
  productName?: string;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

export function ProductApproveDialog({
  kind,
  requestId,
  productName,
  onOpenChange,
  onSuccess,
}: ProductApproveDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleApprove() {
    if (requestId == null) return;
    startTransition(async () => {
      const { success, message } =
        kind === "image"
          ? await approveProductImageChangeAction(requestId)
          : await approveProductVegetarianAction(requestId);

      if (success) {
        toast.success(PRODUCT_APPROVAL_MESSAGE.APPROVE_SUCCESS);
        onOpenChange(false);
        // 처리 결과는 서버 응답으로만 확정한다(낙관적 UI 금지) — 다른 관리자가 먼저 처리했을 수 있다.
        onSuccess();
      } else {
        toast.error(message ?? PRODUCT_APPROVAL_MESSAGE.APPROVE_FAILED);
        onSuccess();
      }
    });
  }

  return (
    <AlertDialog open={requestId != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{PRODUCT_APPROVAL_COPY.APPROVE_CONFIRM_TITLE}</AlertDialogTitle>
          <AlertDialogDescription>
            {productName ? `${productName} — ` : ""}
            {kind === "image"
              ? PRODUCT_APPROVAL_COPY.APPROVE_IMAGE_CONFIRM_BODY
              : PRODUCT_APPROVAL_COPY.APPROVE_VEGETARIAN_CONFIRM_BODY}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isPending}>{PRODUCT_APPROVAL_COPY.CANCEL}</AlertDialogCancel>
          <AlertDialogAction
            onClick={(event) => {
              event.preventDefault();
              handleApprove();
            }}
            disabled={isPending}
          >
            {isPending ? PRODUCT_APPROVAL_COPY.PROCESSING : PRODUCT_APPROVAL_COPY.APPROVE}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
