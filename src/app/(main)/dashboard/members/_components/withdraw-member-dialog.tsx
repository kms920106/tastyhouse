"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
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
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { withdrawMemberAction } from "@/feature/member/actions";
import { WITHDRAWAL_REASON_OPTIONS } from "@/feature/member/constants";
import type { MemberListItem } from "@/feature/member/domain";
import { MEMBER_MESSAGE } from "@/feature/member/message";
import { type WithdrawalFormValues, withdrawalFormSchema } from "@/feature/member/schema";

interface WithdrawMemberDialogProps {
  member: MemberListItem | null;
  onOpenChange: (open: boolean) => void;
}

const DEFAULT_VALUES: WithdrawalFormValues = {
  reason: "OTHER",
  reasonDetail: undefined,
};

export function WithdrawMemberDialog({ member, onOpenChange }: WithdrawMemberDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<WithdrawalFormValues>({
    resolver: zodResolver(withdrawalFormSchema),
    defaultValues: DEFAULT_VALUES,
  });

  React.useEffect(() => {
    if (member) {
      reset(DEFAULT_VALUES);
    }
  }, [member, reset]);

  function onSubmit(values: WithdrawalFormValues) {
    if (!member) return;
    startTransition(async () => {
      const { success, message } = await withdrawMemberAction(member.id, values);
      if (success) {
        toast.success(MEMBER_MESSAGE.WITHDRAWAL_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? MEMBER_MESSAGE.WITHDRAWAL_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={member != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>회원을 강제 탈퇴시키겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {member ? `"${member.nickname}" 회원이 탈퇴 처리되며 이 작업은 되돌릴 수 없습니다.` : ""}
          </AlertDialogDescription>
        </AlertDialogHeader>

        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-2">
            <Label htmlFor="withdrawal-reason">탈퇴 사유</Label>
            <Controller
              control={control}
              name="reason"
              render={({ field }) => (
                <Select value={field.value ?? ""} onValueChange={field.onChange} disabled={isPending}>
                  <SelectTrigger id="withdrawal-reason" className="w-full">
                    <SelectValue placeholder="사유를 선택해 주세요" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                      {WITHDRAWAL_REASON_OPTIONS.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectGroup>
                  </SelectContent>
                </Select>
              )}
            />
            {errors.reason ? <p className="text-destructive text-sm">{errors.reason.message}</p> : null}
          </div>

          <div className="space-y-2">
            <Label htmlFor="withdrawal-reason-detail">사유 상세 (선택)</Label>
            <Controller
              control={control}
              name="reasonDetail"
              render={({ field }) => (
                <Textarea
                  id="withdrawal-reason-detail"
                  placeholder="관리자 메모용 상세 사유를 입력해 주세요."
                  value={field.value ?? ""}
                  onChange={field.onChange}
                  disabled={isPending}
                />
              )}
            />
            {errors.reasonDetail ? <p className="text-destructive text-sm">{errors.reasonDetail.message}</p> : null}
          </div>

          <AlertDialogFooter>
            <Button type="button" variant="outline" disabled={isPending} onClick={() => onOpenChange(false)}>
              취소
            </Button>
            <Button type="submit" variant="destructive" disabled={isPending}>
              {isPending ? "처리 중..." : "강제 탈퇴"}
            </Button>
          </AlertDialogFooter>
        </form>
      </AlertDialogContent>
    </AlertDialog>
  );
}
