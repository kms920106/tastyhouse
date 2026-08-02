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
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { earnPointAction } from "@/feature/point/actions";
import { POINT_MESSAGE } from "@/feature/point/message";
import { type PointEarnFormInput, type PointEarnFormValues, pointEarnFormSchema } from "@/feature/point/schema";

interface EarnPointDialogProps {
  memberId: number | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

const DEFAULT_VALUES: PointEarnFormInput = {
  amount: "",
  reason: "",
};

export function EarnPointDialog({ memberId, open, onOpenChange, onSuccess }: EarnPointDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<PointEarnFormInput, unknown, PointEarnFormValues>({
    resolver: zodResolver(pointEarnFormSchema),
    defaultValues: DEFAULT_VALUES,
  });

  React.useEffect(() => {
    if (open) {
      reset(DEFAULT_VALUES);
    }
  }, [open, reset]);

  function onSubmit(values: PointEarnFormValues) {
    if (memberId == null) return;
    startTransition(async () => {
      const { success, message } = await earnPointAction(memberId, values);
      if (success) {
        toast.success(POINT_MESSAGE.EARN_SUCCESS);
        onSuccess();
        onOpenChange(false);
      } else {
        toast.error(message ?? POINT_MESSAGE.EARN_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>포인트 적립</AlertDialogTitle>
          <AlertDialogDescription>적립할 포인트 금액과 사유를 입력해 주세요.</AlertDialogDescription>
        </AlertDialogHeader>

        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-2">
            <Label htmlFor="earn-amount">적립 금액</Label>
            <Controller
              control={control}
              name="amount"
              render={({ field }) => (
                <Input
                  id="earn-amount"
                  type="number"
                  min={1}
                  placeholder="적립할 포인트를 입력해 주세요."
                  value={field.value ?? ""}
                  onChange={(e) => field.onChange(e.target.value)}
                  onBlur={field.onBlur}
                  disabled={isPending}
                />
              )}
            />
            {errors.amount ? <p className="text-destructive text-sm">{errors.amount.message}</p> : null}
          </div>

          <div className="space-y-2">
            <Label htmlFor="earn-reason">사유</Label>
            <Controller
              control={control}
              name="reason"
              render={({ field }) => (
                <Textarea
                  id="earn-reason"
                  placeholder="적립 사유를 입력해 주세요."
                  value={field.value ?? ""}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                  disabled={isPending}
                />
              )}
            />
            {errors.reason ? <p className="text-destructive text-sm">{errors.reason.message}</p> : null}
          </div>

          <AlertDialogFooter>
            <Button type="button" variant="outline" disabled={isPending} onClick={() => onOpenChange(false)}>
              취소
            </Button>
            <Button type="submit" disabled={isPending}>
              {isPending ? "처리 중..." : "적립"}
            </Button>
          </AlertDialogFooter>
        </form>
      </AlertDialogContent>
    </AlertDialog>
  );
}
