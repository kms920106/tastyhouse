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
import { activateMemberAction } from "@/feature/member/actions";
import type { MemberListItem } from "@/feature/member/domain";
import { MEMBER_MESSAGE } from "@/feature/member/message";

interface ActivateMemberDialogProps {
  member: MemberListItem | null;
  onOpenChange: (open: boolean) => void;
}

export function ActivateMemberDialog({ member, onOpenChange }: ActivateMemberDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleActivate() {
    if (!member) return;
    startTransition(async () => {
      const { success, message } = await activateMemberAction(member.id);
      if (success) {
        toast.success(MEMBER_MESSAGE.ACTIVATE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? MEMBER_MESSAGE.ACTIVATE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={member != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>회원 정지를 해제하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {member ? `"${member.nickname}" 회원의 정지가 해제되어 서비스를 다시 이용할 수 있습니다.` : ""}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isPending}>취소</AlertDialogCancel>
          <AlertDialogAction
            onClick={(event) => {
              event.preventDefault();
              handleActivate();
            }}
            disabled={isPending}
          >
            {isPending ? "처리 중..." : "정지 해제"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
