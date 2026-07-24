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
import { deletePartnershipRequestAction } from "@/feature/partnership-request/actions";
import type { PartnershipRequestListItem } from "@/feature/partnership-request/domain";
import { PARTNERSHIP_MESSAGE } from "@/feature/partnership-request/message";

interface DeletePartnershipRequestDialogProps {
  partnershipRequest: PartnershipRequestListItem | null;
  onOpenChange: (open: boolean) => void;
}

export function DeletePartnershipRequestDialog({
  partnershipRequest,
  onOpenChange,
}: DeletePartnershipRequestDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleDelete() {
    if (!partnershipRequest) return;
    startTransition(async () => {
      const { success, message } = await deletePartnershipRequestAction(partnershipRequest.id);
      if (success) {
        toast.success(PARTNERSHIP_MESSAGE.DELETE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? PARTNERSHIP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={partnershipRequest != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>제휴 신청을 삭제하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {partnershipRequest
              ? `"${partnershipRequest.businessName}" 제휴 신청이 삭제되어 목록에서 제외됩니다. 이 작업은 되돌릴 수 없습니다.`
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
