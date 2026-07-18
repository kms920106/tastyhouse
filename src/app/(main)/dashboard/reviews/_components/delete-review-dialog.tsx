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
import { deleteReviewAction } from "@/feature/review/actions";
import type { ReviewListItem } from "@/feature/review/domain";
import { REVIEW_MESSAGE } from "@/feature/review/message";

interface DeleteReviewDialogProps {
  review: ReviewListItem | null;
  onOpenChange: (open: boolean) => void;
}

export function DeleteReviewDialog({ review, onOpenChange }: DeleteReviewDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleDelete() {
    if (!review) return;
    startTransition(async () => {
      const { success, message } = await deleteReviewAction(review.id);
      if (success) {
        toast.success(REVIEW_MESSAGE.DELETE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? REVIEW_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={review != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>리뷰를 삭제하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {review
              ? `"${review.memberNickname}" 님의 리뷰가 첨부 이미지·태그와 함께 삭제되며, 이 작업은 되돌릴 수 없습니다.`
              : ""}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isPending}>취소</AlertDialogCancel>
          <AlertDialogAction
            onClick={(event) => {
              event.preventDefault();
              handleDelete();
            }}
            disabled={isPending}
          >
            {isPending ? "삭제 중..." : "삭제"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
