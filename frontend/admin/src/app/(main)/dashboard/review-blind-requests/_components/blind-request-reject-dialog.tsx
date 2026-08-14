"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Field, FieldError, FieldLabel } from "@/components/ui/field";
import { Textarea } from "@/components/ui/textarea";
import { rejectBlindRequestAction } from "@/feature/review-blind-request/actions";
import { REJECT_REASON_MAX } from "@/feature/review-blind-request/constants";
import type { ReviewBlindRequestListItem } from "@/feature/review-blind-request/domain";
import { REVIEW_BLIND_REQUEST_DIALOG_COPY, REVIEW_BLIND_REQUEST_MESSAGE } from "@/feature/review-blind-request/message";
import { type RejectFormValues, rejectSchema } from "@/feature/review-blind-request/schema";

interface BlindRequestRejectDialogProps {
  blindRequest: ReviewBlindRequestListItem | null;
  onOpenChange: (open: boolean) => void;
  onSettled: () => void;
}

const EMPTY_VALUES: RejectFormValues = { rejectReason: "" };

export function BlindRequestRejectDialog({ blindRequest, onOpenChange, onSettled }: BlindRequestRejectDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  const form = useForm<RejectFormValues>({
    resolver: zodResolver(rejectSchema),
    defaultValues: EMPTY_VALUES,
  });

  React.useEffect(() => {
    if (blindRequest) form.reset(EMPTY_VALUES);
  }, [blindRequest, form.reset]);

  const onSubmit = (values: RejectFormValues) => {
    if (!blindRequest) return;
    startTransition(async () => {
      const { success, message } = await rejectBlindRequestAction(blindRequest.id, values);
      if (success) {
        toast.success(REVIEW_BLIND_REQUEST_MESSAGE.REJECT_SUCCESS);
      } else {
        toast.error(message ?? REVIEW_BLIND_REQUEST_MESSAGE.REJECT_FAILED);
      }
      onOpenChange(false);
      // 실패가 "이미 처리된 요청"일 수 있어 성공·실패 모두 목록을 갱신한다.
      onSettled();
    });
  };

  return (
    <Dialog open={blindRequest != null} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{REVIEW_BLIND_REQUEST_DIALOG_COPY.REJECT_TITLE}</DialogTitle>
          <DialogDescription>{REVIEW_BLIND_REQUEST_DIALOG_COPY.REJECT_DESCRIPTION}</DialogDescription>
        </DialogHeader>
        <form id="blind-request-reject-form" noValidate onSubmit={form.handleSubmit(onSubmit)}>
          <Controller
            control={form.control}
            name="rejectReason"
            render={({ field, fieldState }) => (
              <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                <FieldLabel htmlFor="blind-request-reject-reason">
                  {REVIEW_BLIND_REQUEST_DIALOG_COPY.REJECT_REASON_LABEL}
                </FieldLabel>
                <Textarea
                  {...field}
                  id="blind-request-reject-reason"
                  placeholder={REVIEW_BLIND_REQUEST_DIALOG_COPY.REJECT_REASON_PLACEHOLDER}
                  maxLength={REJECT_REASON_MAX}
                  aria-invalid={fieldState.invalid}
                  disabled={isPending}
                  rows={4}
                />
                {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
              </Field>
            )}
          />
        </form>
        <DialogFooter>
          <Button type="submit" form="blind-request-reject-form" disabled={isPending}>
            {isPending ? "처리 중..." : "반려"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
