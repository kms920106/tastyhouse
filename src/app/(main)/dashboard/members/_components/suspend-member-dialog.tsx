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
import { suspendMemberAction } from "@/feature/member/actions";
import type { MemberListItem } from "@/feature/member/domain";
import { MEMBER_MESSAGE } from "@/feature/member/message";

interface SuspendMemberDialogProps {
  member: MemberListItem | null;
  onOpenChange: (open: boolean) => void;
}

export function SuspendMemberDialog({ member, onOpenChange }: SuspendMemberDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleSuspend() {
    if (!member) return;
    startTransition(async () => {
      const { success, message } = await suspendMemberAction(member.id);
      if (success) {
        toast.success(MEMBER_MESSAGE.SUSPEND_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? MEMBER_MESSAGE.SUSPEND_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={member != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>회원을 정지하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {member ? `"${member.nickname}" 회원이 정지되어 서비스 이용이 제한됩니다.` : ""}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isPending}>취소</AlertDialogCancel>
          <AlertDialogAction
            onClick={(event) => {
              event.preventDefault();
              handleSuspend();
            }}
            disabled={isPending}
          >
            {isPending ? "처리 중..." : "정지"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
