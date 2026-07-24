"use client";

import * as React from "react";

import { toast } from "sonner";

import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { updatePartnershipRequestStatusAction } from "@/feature/partnership-request/actions";
import { PARTNERSHIP_STATUS_OPTIONS } from "@/feature/partnership-request/constants";
import type { PartnershipRequestListItem, PartnershipStatus } from "@/feature/partnership-request/domain";
import { PARTNERSHIP_MESSAGE } from "@/feature/partnership-request/message";

interface PartnershipRequestStatusDialogProps {
  partnershipRequest: PartnershipRequestListItem | null;
  onOpenChange: (open: boolean) => void;
}

export function PartnershipRequestStatusDialog({
  partnershipRequest,
  onOpenChange,
}: PartnershipRequestStatusDialogProps) {
  const [isPending, startTransition] = React.useTransition();
  const [status, setStatus] = React.useState<PartnershipStatus>(partnershipRequest?.status ?? "PENDING");

  React.useEffect(() => {
    if (partnershipRequest) {
      setStatus(partnershipRequest.status);
    }
  }, [partnershipRequest]);

  function handleSave() {
    if (!partnershipRequest) return;
    startTransition(async () => {
      const { success, message } = await updatePartnershipRequestStatusAction(partnershipRequest.id, { status });
      if (success) {
        toast.success(PARTNERSHIP_MESSAGE.STATUS_UPDATE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? PARTNERSHIP_MESSAGE.STATUS_UPDATE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={partnershipRequest != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>처리 상태를 변경하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {partnershipRequest ? `"${partnershipRequest.businessName}" 제휴 신청의 처리 상태를 변경합니다.` : ""}
          </AlertDialogDescription>
        </AlertDialogHeader>

        <Select value={status} onValueChange={(value) => setStatus(value as PartnershipStatus)} disabled={isPending}>
          <SelectTrigger className="w-full">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectGroup>
              {PARTNERSHIP_STATUS_OPTIONS.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectGroup>
          </SelectContent>
        </Select>

        <AlertDialogFooter>
          <Button type="button" variant="outline" disabled={isPending} onClick={() => onOpenChange(false)}>
            취소
          </Button>
          <Button type="button" onClick={handleSave} disabled={isPending}>
            {isPending ? "처리 중..." : "저장"}
          </Button>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
