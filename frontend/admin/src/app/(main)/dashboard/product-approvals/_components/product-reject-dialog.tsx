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
import {
  rejectProductImageChangeAction,
  rejectProductRepresentativeAction,
  rejectProductVegetarianAction,
} from "@/feature/product/actions";
import { PRODUCT_APPROVAL_COPY, PRODUCT_APPROVAL_MESSAGE } from "@/feature/product/message";
import { PRODUCT_REJECT_REASON_MAX, type ProductRejectFormValues, productRejectSchema } from "@/feature/product/schema";
import {
  rejectMenuCollectionImageRequestAction,
  rejectStorePriceVerificationRequestAction,
} from "@/feature/shop/actions";

import type { ProductApprovalKind } from "./product-approve-dialog";

/**
 * 종류마다 호출 API 가 다르므로 한 곳에 모아 분기를 중복시키지 않는다.
 *
 * <p>메뉴모음컷·매장가격 인증은 shop 리소스이지만 반려 사유 필드명(`rejectReason`)과
 * 길이 제한(500)이 같아 같은 폼 스키마·다이얼로그를 그대로 쓴다.
 */
const REJECT_ACTION_BY_KIND: Record<
  ProductApprovalKind,
  (requestId: number, values: ProductRejectFormValues) => Promise<{ success: boolean; message?: string }>
> = {
  image: rejectProductImageChangeAction,
  vegetarian: rejectProductVegetarianAction,
  menuCollection: rejectMenuCollectionImageRequestAction,
  representative: rejectProductRepresentativeAction,
  storePrice: rejectStorePriceVerificationRequestAction,
};

interface ProductRejectDialogProps {
  kind: ProductApprovalKind;
  /** 반려 대상 요청 ID. null 이면 다이얼로그를 닫는다 */
  requestId: number | null;
  productName?: string;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

const EMPTY_VALUES: ProductRejectFormValues = { rejectReason: "" };

export function ProductRejectDialog({
  kind,
  requestId,
  productName,
  onOpenChange,
  onSuccess,
}: ProductRejectDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  const form = useForm<ProductRejectFormValues>({
    resolver: zodResolver(productRejectSchema),
    defaultValues: EMPTY_VALUES,
  });

  React.useEffect(() => {
    if (requestId != null) form.reset(EMPTY_VALUES);
  }, [requestId, form.reset]);

  const onSubmit = (values: ProductRejectFormValues) => {
    if (requestId == null) return;
    startTransition(async () => {
      const { success, message } = await REJECT_ACTION_BY_KIND[kind](requestId, values);

      if (success) {
        toast.success(PRODUCT_APPROVAL_MESSAGE.REJECT_SUCCESS);
        onOpenChange(false);
        // 처리 결과는 서버 응답으로만 확정한다(낙관적 UI 금지).
        onSuccess();
      } else {
        toast.error(message ?? PRODUCT_APPROVAL_MESSAGE.REJECT_FAILED);
        onSuccess();
      }
    });
  };

  return (
    <Dialog open={requestId != null} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{PRODUCT_APPROVAL_COPY.REJECT_DIALOG_TITLE}</DialogTitle>
          <DialogDescription>
            {productName ? `${productName} — ` : ""}
            {PRODUCT_APPROVAL_COPY.REJECT_DIALOG_DESCRIPTION}
          </DialogDescription>
        </DialogHeader>
        <form id="product-reject-form" noValidate onSubmit={form.handleSubmit(onSubmit)}>
          <Controller
            control={form.control}
            name="rejectReason"
            render={({ field, fieldState }) => (
              <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                <FieldLabel htmlFor="product-reject-reason">{PRODUCT_APPROVAL_COPY.REJECT_REASON_LABEL}</FieldLabel>
                <Textarea
                  {...field}
                  id="product-reject-reason"
                  placeholder={PRODUCT_APPROVAL_COPY.REJECT_REASON_PLACEHOLDER}
                  maxLength={PRODUCT_REJECT_REASON_MAX}
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
          <Button type="submit" form="product-reject-form" variant="destructive" disabled={isPending}>
            {isPending ? PRODUCT_APPROVAL_COPY.PROCESSING : PRODUCT_APPROVAL_COPY.REJECT}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
