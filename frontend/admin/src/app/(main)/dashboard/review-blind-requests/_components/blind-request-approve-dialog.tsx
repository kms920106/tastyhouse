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
import { approveBlindRequestAction } from "@/feature/review-blind-request/actions";
import type { ReviewBlindRequestListItem } from "@/feature/review-blind-request/domain";
import { REVIEW_BLIND_REQUEST_DIALOG_COPY, REVIEW_BLIND_REQUEST_MESSAGE } from "@/feature/review-blind-request/message";

interface BlindRequestApproveDialogProps {
  blindRequest: ReviewBlindRequestListItem | null;
  onOpenChange: (open: boolean) => void;
  onSettled: () => void;
}

export function BlindRequestApproveDialog({ blindRequest, onOpenChange, onSettled }: BlindRequestApproveDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleApprove() {
    if (!blindRequest) return;
    startTransition(async () => {
      const { success, message } = await approveBlindRequestAction(blindRequest.id);
      if (success) {
        toast.success(REVIEW_BLIND_REQUEST_MESSAGE.APPROVE_SUCCESS);
      } else {
        toast.error(message ?? REVIEW_BLIND_REQUEST_MESSAGE.APPROVE_FAILED);
      }
      onOpenChange(false);
      // 실패가 "이미 처리된 요청"일 수 있어 성공·실패 모두 목록을 갱신한다.
      onSettled();
    });
  }

  return (
    <AlertDialog open={blindRequest != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{REVIEW_BLIND_REQUEST_DIALOG_COPY.APPROVE_TITLE}</AlertDialogTitle>
          <AlertDialogDescription>{REVIEW_BLIND_REQUEST_DIALOG_COPY.APPROVE_DESCRIPTION}</AlertDialogDescription>
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
