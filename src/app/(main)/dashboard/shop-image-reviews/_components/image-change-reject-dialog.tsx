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
import { rejectImageChangeRequestAction } from "@/feature/shop/actions";
import { REJECT_REASON_MAX } from "@/feature/shop/constants";
import type { ShopImageChangeRequest } from "@/feature/shop/domain";
import { SHOP_MESSAGE } from "@/feature/shop/message";
import { type ImageChangeRejectFormValues, imageChangeRejectSchema } from "@/feature/shop/schema";

interface ImageChangeRejectDialogProps {
  request: ShopImageChangeRequest | null;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

const EMPTY_VALUES: ImageChangeRejectFormValues = { reason: "" };

export function ImageChangeRejectDialog({ request, onOpenChange, onSuccess }: ImageChangeRejectDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  const form = useForm<ImageChangeRejectFormValues>({
    resolver: zodResolver(imageChangeRejectSchema),
    defaultValues: EMPTY_VALUES,
  });

  React.useEffect(() => {
    if (request) form.reset(EMPTY_VALUES);
  }, [request, form.reset]);

  const onSubmit = (values: ImageChangeRejectFormValues) => {
    if (!request) return;
    startTransition(async () => {
      const { success, message } = await rejectImageChangeRequestAction(request.id, values);
      if (success) {
        toast.success(SHOP_MESSAGE.IMAGE_CHANGE_REJECT_SUCCESS);
        onOpenChange(false);
        onSuccess();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  return (
    <Dialog open={request != null} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>이미지 변경요청 반려</DialogTitle>
          <DialogDescription>반려 사유를 입력해 주세요. 점주에게 그대로 노출됩니다.</DialogDescription>
        </DialogHeader>
        <form id="image-change-reject-form" noValidate onSubmit={form.handleSubmit(onSubmit)}>
          <Controller
            control={form.control}
            name="reason"
            render={({ field, fieldState }) => (
              <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                <FieldLabel htmlFor="image-change-reject-reason">반려 사유</FieldLabel>
                <Textarea
                  {...field}
                  id="image-change-reject-reason"
                  placeholder="반려 사유를 입력하세요"
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
          <Button type="submit" form="image-change-reject-form" disabled={isPending}>
            {isPending ? "처리 중..." : "반려"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
