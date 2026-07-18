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
import { setReviewHiddenAction } from "@/feature/review/actions";
import type { ReviewListItem } from "@/feature/review/domain";
import { REVIEW_MESSAGE } from "@/feature/review/message";

interface ReviewHiddenDialogProps {
  review: ReviewListItem | null;
  onOpenChange: (open: boolean) => void;
}

export function ReviewHiddenDialog({ review, onOpenChange }: ReviewHiddenDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  const nextHidden = review ? !review.hidden : false;

  function handleToggle() {
    if (!review) return;
    startTransition(async () => {
      const { success, message } = await setReviewHiddenAction(review.id, nextHidden);
      if (success) {
        toast.success(nextHidden ? REVIEW_MESSAGE.HIDDEN_SUCCESS : REVIEW_MESSAGE.VISIBLE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? REVIEW_MESSAGE.HIDDEN_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={review != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>
            {nextHidden ? "리뷰를 숨김 처리하시겠습니까?" : "리뷰를 노출 처리하시겠습니까?"}
          </AlertDialogTitle>
          <AlertDialogDescription>
            {review
              ? nextHidden
                ? `"${review.memberNickname}" 님의 리뷰가 숨김 처리되어 사용자에게 노출되지 않습니다.`
                : `"${review.memberNickname}" 님의 리뷰가 다시 노출됩니다.`
              : ""}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isPending}>취소</AlertDialogCancel>
          <AlertDialogAction
            onClick={(event) => {
              event.preventDefault();
              handleToggle();
            }}
            disabled={isPending}
          >
            {isPending ? "처리 중..." : nextHidden ? "숨김 처리" : "노출 처리"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
