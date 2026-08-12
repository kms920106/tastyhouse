"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { StatusBadge } from "@/components/status-badge";
import { Button } from "@/components/ui/button";
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { Textarea } from "@/components/ui/textarea";
import { fetchDeliveryAreaAdjustmentsAction, requestDeliveryAreaAdjustmentAction } from "@/feature/shop/actions";
import {
  ADJUSTMENT_NAME_MAX,
  ADJUSTMENT_REASON_MAX,
  ALLOWED_CONSENT_TYPES,
  BUSINESS_NUMBER_LENGTH,
  DELIVERY_AREA_ADJUSTMENT_OPEN_STATUSES,
  DELIVERY_AREA_ADJUSTMENT_STATUS_LABEL,
} from "@/feature/shop/constants";
import type { DeliveryAreaAdjustmentRequest } from "@/feature/shop/domain";
import { SHOP_MESSAGE, SHOP_OPERATION_COPY } from "@/feature/shop/message";
import { type DeliveryAreaAdjustmentFormValues, deliveryAreaAdjustmentSchema } from "@/feature/shop/schema";

import { validateConsentFile } from "./use-image-file-select";

interface DeliveryAreaAdjustmentSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
}

const EMPTY_VALUES: DeliveryAreaAdjustmentFormValues = {
  counterpartShopName: "",
  counterpartBusinessNumber: "",
  franchiseName: "",
  reason: "",
};

export function DeliveryAreaAdjustmentSheet({ open, onOpenChange, shopId }: DeliveryAreaAdjustmentSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const fileInputRef = React.useRef<HTMLInputElement>(null);
  const [selectedFile, setSelectedFile] = React.useState<File | null>(null);
  const [requests, setRequests] = React.useState<DeliveryAreaAdjustmentRequest[]>([]);

  const form = useForm<DeliveryAreaAdjustmentFormValues>({
    resolver: zodResolver(deliveryAreaAdjustmentSchema),
    defaultValues: EMPTY_VALUES,
  });

  const reload = React.useCallback(() => {
    startTransition(async () => {
      const { success, message, data } = await fetchDeliveryAreaAdjustmentsAction(shopId);
      if (success) {
        setRequests(data ?? []);
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELIVERY_AREA_ADJUSTMENTS_LOAD_FAILED);
      }
    });
  }, [shopId]);

  React.useEffect(() => {
    if (!open) return;
    form.reset(EMPTY_VALUES);
    setSelectedFile(null);
    reload();
  }, [open, form, reload]);

  // 진행 중인 신청이 있으면 서버가 409 로 막지만, UI 에서 미리 알린다.
  const hasOpenRequest = requests.some((request) => DELIVERY_AREA_ADJUSTMENT_OPEN_STATUSES.includes(request.status));

  function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file) return;

    const error = validateConsentFile(file);
    if (error) {
      toast.error(error);
      return;
    }
    setSelectedFile(file);
  }

  const onSubmit = (values: DeliveryAreaAdjustmentFormValues) => {
    if (!selectedFile) {
      toast.error(SHOP_MESSAGE.CONSENT_FILE_REQUIRED);
      return;
    }

    startTransition(async () => {
      const formData = new FormData();
      formData.append("counterpartShopName", values.counterpartShopName);
      formData.append("counterpartBusinessNumber", values.counterpartBusinessNumber);
      formData.append("franchiseName", values.franchiseName);
      formData.append("reason", values.reason);
      formData.append("file", selectedFile);

      const { success, message } = await requestDeliveryAreaAdjustmentAction(shopId, formData);
      if (success) {
        toast.success(SHOP_MESSAGE.DELIVERY_AREA_ADJUSTMENT_REQUEST_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-lg">
        <SheetHeader>
          <SheetTitle>{SHOP_OPERATION_COPY.ADJUSTMENT_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_OPERATION_COPY.ADJUSTMENT_GUIDE}</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-6 overflow-y-auto px-4">
          <p className="rounded-md border border-dashed p-3 text-muted-foreground text-xs leading-snug">
            {SHOP_OPERATION_COPY.ADJUSTMENT_NOTICE}
          </p>

          {hasOpenRequest && (
            <p className="text-destructive text-xs leading-snug">{SHOP_OPERATION_COPY.ADJUSTMENT_PENDING_GUIDE}</p>
          )}

          {/* ===== 1. 신청 폼 ===== */}
          <form id="delivery-area-adjustment-form" noValidate onSubmit={form.handleSubmit(onSubmit)}>
            <FieldGroup className="gap-4">
              <Controller
                control={form.control}
                name="counterpartShopName"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="adjustment-counterpart-shop-name">
                      {SHOP_OPERATION_COPY.ADJUSTMENT_COUNTERPART_SHOP_NAME_LABEL}
                    </FieldLabel>
                    <Input
                      {...field}
                      id="adjustment-counterpart-shop-name"
                      maxLength={ADJUSTMENT_NAME_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={isPending || hasOpenRequest}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="counterpartBusinessNumber"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="adjustment-counterpart-business-number">
                      {SHOP_OPERATION_COPY.ADJUSTMENT_COUNTERPART_BUSINESS_NUMBER_LABEL}
                    </FieldLabel>
                    <Input
                      {...field}
                      id="adjustment-counterpart-business-number"
                      inputMode="numeric"
                      maxLength={BUSINESS_NUMBER_LENGTH}
                      aria-invalid={fieldState.invalid}
                      disabled={isPending || hasOpenRequest}
                    />
                    <FieldDescription>
                      {SHOP_OPERATION_COPY.ADJUSTMENT_COUNTERPART_BUSINESS_NUMBER_GUIDE}
                    </FieldDescription>
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="franchiseName"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="adjustment-franchise-name">
                      {SHOP_OPERATION_COPY.ADJUSTMENT_FRANCHISE_NAME_LABEL}
                    </FieldLabel>
                    <Input
                      {...field}
                      id="adjustment-franchise-name"
                      maxLength={ADJUSTMENT_NAME_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={isPending || hasOpenRequest}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="reason"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="adjustment-reason">{SHOP_OPERATION_COPY.ADJUSTMENT_REASON_LABEL}</FieldLabel>
                    <Textarea
                      {...field}
                      id="adjustment-reason"
                      rows={4}
                      maxLength={ADJUSTMENT_REASON_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={isPending || hasOpenRequest}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              {/* 동의서는 PDF 일 수 있어 미리보기가 불가능하므로 파일명만 표시한다. */}
              <Field className="gap-1.5">
                <FieldLabel htmlFor="adjustment-consent-file">
                  {SHOP_OPERATION_COPY.ADJUSTMENT_CONSENT_FILE_LABEL}
                </FieldLabel>
                <div className="flex items-center gap-2">
                  <Button
                    type="button"
                    size="sm"
                    variant="outline"
                    disabled={isPending || hasOpenRequest}
                    onClick={() => fileInputRef.current?.click()}
                  >
                    파일 선택
                  </Button>
                  <span className="min-w-0 flex-1 truncate text-muted-foreground text-sm">
                    {selectedFile?.name ?? SHOP_OPERATION_COPY.ADJUSTMENT_CONSENT_FILE_UNSELECTED}
                  </span>
                </div>
                <input
                  ref={fileInputRef}
                  id="adjustment-consent-file"
                  type="file"
                  accept={ALLOWED_CONSENT_TYPES.join(",")}
                  className="hidden"
                  onChange={handleFileChange}
                />
                <FieldDescription>{SHOP_OPERATION_COPY.ADJUSTMENT_CONSENT_FILE_GUIDE}</FieldDescription>
              </Field>
            </FieldGroup>
          </form>

          <Separator />

          {/* ===== 2. 신청 이력 ===== */}
          <section className="space-y-3">
            <span className="font-medium text-sm">{SHOP_OPERATION_COPY.ADJUSTMENT_HISTORY_LEGEND}</span>

            {requests.length > 0 ? (
              <ul className="space-y-2">
                {requests.map((request) => (
                  <li key={request.id} className="flex flex-col gap-1 rounded-md border p-3">
                    <div className="flex items-center justify-between gap-2">
                      <span className="min-w-0 flex-1 truncate text-sm">{request.counterpartShopName}</span>
                      <StatusBadge
                        status={request.status}
                        label={DELIVERY_AREA_ADJUSTMENT_STATUS_LABEL[request.status]}
                      />
                    </div>
                    <span className="text-muted-foreground text-xs">{request.franchiseName}</span>
                    {request.status === "REJECTED" && request.rejectReason && (
                      <span className="text-destructive text-xs">
                        {DELIVERY_AREA_ADJUSTMENT_STATUS_LABEL.REJECTED} · {request.rejectReason}
                      </span>
                    )}
                  </li>
                ))}
              </ul>
            ) : (
              <p className="rounded-md border border-dashed p-4 text-center text-muted-foreground text-sm">
                {SHOP_OPERATION_COPY.ADJUSTMENT_HISTORY_EMPTY}
              </p>
            )}
          </section>
        </div>

        <SheetFooter>
          <Button type="submit" form="delivery-area-adjustment-form" disabled={isPending || hasOpenRequest}>
            {isPending ? "접수 중..." : "신청"}
          </Button>
          <SheetClose asChild>
            <Button variant="outline" disabled={isPending}>
              닫기
            </Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
