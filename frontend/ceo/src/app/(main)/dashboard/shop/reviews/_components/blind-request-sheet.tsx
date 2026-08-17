"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Field, FieldDescription, FieldError, FieldLabel } from "@/components/ui/field";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { Textarea } from "@/components/ui/textarea";
import { createBlindRequestAction } from "@/feature/shop-review/actions";
import {
  BLIND_ATTACHMENT_ACCEPT,
  BLIND_ATTACHMENT_MAX_COUNT,
  BLIND_DETAIL_REASON_MAX_LENGTH,
  BLIND_REASON_ETC,
} from "@/feature/shop-review/constants";
import type { ReviewBlindReasonOption } from "@/feature/shop-review/domain";
import { formatFileSize } from "@/feature/shop-review/format";
import { SHOP_REVIEW_COPY } from "@/feature/shop-review/message";
import { type BlindRequestFormValues, blindRequestSchema } from "@/feature/shop-review/schema";

import { validateConsentFile } from "../../_components/use-image-file-select";

interface BlindRequestSheetProps {
  shopId: number;
  reviewId: number;
  /** 서버 카탈로그. 비어 있으면 사유를 고를 수 없어 안내만 띄운다 */
  blindReasons: ReviewBlindReasonOption[];
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

/**
 * 게시중단 요청 폼.
 *
 * 사유가 `ETC` 면 상세 사유가 필수다(`blindRequestSchema` 의 `superRefine`). 서버도 같은 규칙으로
 * `REVIEW_BLIND_DETAIL_REASON_REQUIRED` 를 내므로, 여기서 막는 것은 왕복을 아끼기 위한 1차 방어다.
 */
export function BlindRequestSheet({ shopId, reviewId, blindReasons, open, onOpenChange }: BlindRequestSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const fileInputRef = React.useRef<HTMLInputElement>(null);
  // 파일 원본은 폼 값이 아니라 별도 state 로 든다 — 업로드(fileId 발급)는 서버 액션의 몫이라
  // 이 컴포넌트는 File 을 FormData 로 넘기기만 한다(`fileRepository` 는 server-only).
  const [attachments, setAttachments] = React.useState<File[]>([]);

  const form = useForm<BlindRequestFormValues>({
    resolver: zodResolver(blindRequestSchema),
    defaultValues: { reason: undefined, detailReason: "" },
  });

  // 시트를 다시 열 때 이전 입력·첨부가 남아 있지 않게 한다.
  React.useEffect(() => {
    if (!open) return;
    form.reset({ reason: undefined, detailReason: "" });
    setAttachments([]);
  }, [open, form.reset]);

  const selectedReason = form.watch("reason");
  const isDetailRequired = selectedReason === BLIND_REASON_ETC;
  const isLimitReached = attachments.length >= BLIND_ATTACHMENT_MAX_COUNT;

  /**
   * 선택한 파일을 검증해 목록에 더한다.
   *
   * `validateConsentFile()` 은 치수 검증 없이 형식·용량만 보므로 PDF 가 섞인 증빙 서류에 그대로 맞는다.
   * 상한을 넘는 분은 잘라내고 안내만 띄운다 — 고른 것을 전부 버리면 다시 고르게 만들어 번거롭다.
   */
  function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const selected = Array.from(event.target.files ?? []);
    // 같은 파일을 다시 골라도 change 가 발생하도록 값을 비운다.
    event.target.value = "";
    if (selected.length === 0) return;

    const accepted: File[] = [];
    for (const file of selected) {
      const error = validateConsentFile(file);
      if (error) {
        toast.error(error);
        continue;
      }
      accepted.push(file);
    }
    if (accepted.length === 0) return;

    setAttachments((previous) => {
      const remaining = BLIND_ATTACHMENT_MAX_COUNT - previous.length;
      if (accepted.length > remaining) toast.error(SHOP_REVIEW_COPY.BLIND_ATTACHMENT_LIMIT_REACHED);
      return [...previous, ...accepted.slice(0, remaining)];
    });
  }

  function handleRemoveAttachment(index: number) {
    setAttachments((previous) => previous.filter((_, position) => position !== index));
  }

  const onSubmit = (values: BlindRequestFormValues) => {
    // 첨부를 함께 보내야 하므로 FormData 로 넘긴다 — 액션이 업로드 후 fileId 배열로 바꿔 신청한다.
    const formData = new FormData();
    formData.append("reason", values.reason);
    if (values.detailReason) formData.append("detailReason", values.detailReason);
    for (const file of attachments) formData.append("attachments", file);

    startTransition(async () => {
      const { success, message } = await createBlindRequestAction(shopId, reviewId, formData);
      if (success) {
        toast.success(SHOP_REVIEW_COPY.BLIND_REQUEST_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? SHOP_REVIEW_COPY.BLIND_REQUEST_FAILED);
      }
    });
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex flex-col gap-0 overflow-y-auto">
        <SheetHeader>
          <SheetTitle>{SHOP_REVIEW_COPY.BLIND_REQUEST_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_REVIEW_COPY.BLIND_REQUEST_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex flex-col gap-4 px-4 pb-6">
          {blindReasons.length === 0 ? (
            <p className="text-destructive text-sm">{SHOP_REVIEW_COPY.BLIND_REASON_LOAD_FAILED}</p>
          ) : (
            <form className="flex flex-col gap-4" noValidate onSubmit={form.handleSubmit(onSubmit)}>
              <Controller
                control={form.control}
                name="reason"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="blind-request-reason">
                      {SHOP_REVIEW_COPY.BLIND_REQUEST_REASON_LABEL}
                    </FieldLabel>
                    {/* Radix Select 의 value 는 항상 안정 문자열이어야 한다(undefined 금지) */}
                    <Select value={field.value ?? ""} onValueChange={field.onChange} disabled={isPending}>
                      <SelectTrigger id="blind-request-reason" className="w-full" aria-invalid={fieldState.invalid}>
                        <SelectValue placeholder={SHOP_REVIEW_COPY.BLIND_REQUEST_REASON_PLACEHOLDER} />
                      </SelectTrigger>
                      <SelectContent position="popper" align="start">
                        <SelectGroup>
                          {blindReasons.map((reason) => (
                            <SelectItem key={reason.code} value={reason.code}>
                              {reason.description}
                            </SelectItem>
                          ))}
                        </SelectGroup>
                      </SelectContent>
                    </Select>
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="detailReason"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="blind-request-detail-reason">
                      {SHOP_REVIEW_COPY.BLIND_REQUEST_DETAIL_LABEL}
                    </FieldLabel>
                    <Textarea
                      {...field}
                      value={field.value ?? ""}
                      id="blind-request-detail-reason"
                      placeholder={SHOP_REVIEW_COPY.BLIND_REQUEST_DETAIL_PLACEHOLDER}
                      maxLength={BLIND_DETAIL_REASON_MAX_LENGTH}
                      aria-invalid={fieldState.invalid}
                      aria-required={isDetailRequired}
                      disabled={isPending}
                      rows={4}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              {/* 증빙 서류 — PDF 가 섞여 있어 썸네일 대신 파일명·용량 텍스트로 보여준다 */}
              <Field className="gap-1.5">
                <FieldLabel htmlFor="blind-request-attachments">{SHOP_REVIEW_COPY.BLIND_ATTACHMENT_LABEL}</FieldLabel>
                <FieldDescription>{SHOP_REVIEW_COPY.BLIND_ATTACHMENT_GUIDE}</FieldDescription>

                {attachments.length === 0 ? (
                  <p className="text-muted-foreground text-sm">{SHOP_REVIEW_COPY.BLIND_ATTACHMENT_EMPTY}</p>
                ) : (
                  <ul className="flex flex-col gap-1.5">
                    {attachments.map((file, index) => (
                      // 같은 이름의 파일을 두 번 고를 수 있어 이름만으로는 키가 겹친다.
                      <li
                        key={`${file.name}-${file.size}-${file.lastModified}`}
                        className="flex items-center gap-2 rounded-md border px-3 py-2"
                      >
                        <span className="min-w-0 flex-1 truncate text-sm">{file.name}</span>
                        <span className="shrink-0 text-muted-foreground text-xs tabular-nums">
                          {formatFileSize(file.size)}
                        </span>
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          disabled={isPending}
                          onClick={() => handleRemoveAttachment(index)}
                        >
                          {SHOP_REVIEW_COPY.BLIND_ATTACHMENT_REMOVE}
                        </Button>
                      </li>
                    ))}
                  </ul>
                )}

                <input
                  ref={fileInputRef}
                  id="blind-request-attachments"
                  type="file"
                  multiple
                  accept={BLIND_ATTACHMENT_ACCEPT}
                  className="hidden"
                  onChange={handleFileChange}
                />
                <Button
                  type="button"
                  size="sm"
                  variant="outline"
                  className="w-fit"
                  // 상한에 도달하면 서버 400 을 맞기 전에 막는다.
                  disabled={isPending || isLimitReached}
                  onClick={() => fileInputRef.current?.click()}
                >
                  {SHOP_REVIEW_COPY.BLIND_ATTACHMENT_ADD}
                </Button>
                {isLimitReached && (
                  <FieldDescription>{SHOP_REVIEW_COPY.BLIND_ATTACHMENT_LIMIT_REACHED}</FieldDescription>
                )}
              </Field>

              {/* 업로드가 액션 안에서 순차로 일어나므로 제출 중에는 버튼을 잠근다 */}
              <Button type="submit" className="self-end" disabled={isPending}>
                {isPending && attachments.length > 0
                  ? SHOP_REVIEW_COPY.BLIND_ATTACHMENT_UPLOADING
                  : SHOP_REVIEW_COPY.BLIND_REQUEST_SUBMIT}
              </Button>
            </form>
          )}
        </div>
      </SheetContent>
    </Sheet>
  );
}
