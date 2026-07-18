"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE_BYTES } from "@/api/file/file.dto";
import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { Skeleton } from "@/components/ui/skeleton";
import { createPrizeAction, fetchPrizeAction, updatePrizeAction, uploadPrizeImageAction } from "@/feature/rank/actions";
import { PRIZE_BRAND_MAX, PRIZE_NAME_MAX } from "@/feature/rank/constants";
import type { RankPrize } from "@/feature/rank/domain";
import { RANK_MESSAGE } from "@/feature/rank/message";
import { type PrizeFormValues, prizeSchema } from "@/feature/rank/schema";

interface PrizeFormSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  periodId: number | null;
  prize?: Pick<RankPrize, "id"> | null;
  /** 등수 중복 사전검증용 — 같은 기간에 이미 등록된 경품 목록(수정 대상은 포함되어도 무방, 자기 자신은 별도 제외 처리) */
  existingPrizes: RankPrize[];
  onSaved: () => void;
}

const EMPTY_VALUES: PrizeFormValues = {
  prizeRank: undefined as unknown as number,
  name: "",
  brand: "",
  imageFileId: undefined,
};

function parseOptionalNumber(value: string): number | undefined {
  return value.trim() === "" ? undefined : Number(value);
}

export function PrizeFormSheet({ open, onOpenChange, periodId, prize, existingPrizes, onSaved }: PrizeFormSheetProps) {
  const isEdit = Boolean(prize);
  const [isPending, startTransition] = React.useTransition();
  const [isLoadingDetail, setIsLoadingDetail] = React.useState(false);
  const [isUploading, setIsUploading] = React.useState(false);
  const [previewUrl, setPreviewUrl] = React.useState<string | undefined>(undefined);

  const form = useForm<PrizeFormValues>({
    resolver: zodResolver(prizeSchema),
    defaultValues: EMPTY_VALUES,
  });

  const resetPreview = React.useCallback(() => {
    setPreviewUrl((prev) => {
      if (prev?.startsWith("blob:")) URL.revokeObjectURL(prev);
      return undefined;
    });
  }, []);

  React.useEffect(() => {
    if (!open) return;

    if (!prize) {
      form.reset(EMPTY_VALUES);
      resetPreview();
      return;
    }

    let active = true;
    setIsLoadingDetail(true);

    void fetchPrizeAction(prize.id).then((result) => {
      if (!active) return;
      setIsLoadingDetail(false);

      if (!result.success || !result.data) {
        toast.error(result.message ?? RANK_MESSAGE.PRIZES_LOAD_FAILED);
        onOpenChange(false);
        return;
      }

      const detail = result.data;
      form.reset({
        prizeRank: detail.prizeRank,
        name: detail.name,
        brand: detail.brand,
        imageFileId: detail.image?.id,
      });
      setPreviewUrl(detail.image?.url);
    });

    return () => {
      active = false;
    };
  }, [open, prize, form.reset, onOpenChange, resetPreview]);

  React.useEffect(() => {
    return () => {
      setPreviewUrl((prev) => {
        if (prev?.startsWith("blob:")) URL.revokeObjectURL(prev);
        return undefined;
      });
    };
  }, []);

  async function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) return;

    if (!(ALLOWED_IMAGE_TYPES as readonly string[]).includes(file.type)) {
      toast.error(RANK_MESSAGE.IMAGE_TYPE_INVALID);
      event.target.value = "";
      return;
    }
    if (file.size > MAX_IMAGE_SIZE_BYTES) {
      toast.error(RANK_MESSAGE.IMAGE_SIZE_EXCEEDED);
      event.target.value = "";
      return;
    }

    setIsUploading(true);
    const formData = new FormData();
    formData.append("file", file);

    const result = await uploadPrizeImageAction(formData);
    setIsUploading(false);
    event.target.value = "";

    if (!result.success || result.fileId === undefined) {
      toast.error(result.message ?? RANK_MESSAGE.IMAGE_UPLOAD_FAILED);
      return;
    }

    form.setValue("imageFileId", result.fileId);
    resetPreview();
    setPreviewUrl(URL.createObjectURL(file));
  }

  const onSubmit = (values: PrizeFormValues) => {
    if (periodId == null) return;

    // 클라 사전검증: 같은 기간 내 등수 중복 (수정 시 자기 자신 제외)
    const duplicated = existingPrizes.some((item) => item.prizeRank === values.prizeRank && item.id !== prize?.id);
    if (duplicated) {
      form.setError("prizeRank", { type: "manual", message: RANK_MESSAGE.PRIZE_RANK_DUPLICATED });
      return;
    }

    startTransition(async () => {
      const { success, message } = prize
        ? await updatePrizeAction(prize.id, values)
        : await createPrizeAction(periodId, values);

      if (success) {
        toast.success(isEdit ? RANK_MESSAGE.PRIZE_UPDATE_SUCCESS : RANK_MESSAGE.PRIZE_CREATE_SUCCESS);
        onOpenChange(false);
        onSaved();
      } else {
        toast.error(message ?? RANK_MESSAGE.PRIZE_CREATE_UPDATE_FAILED);
      }
    });
  };

  const isSaving = isPending || isLoadingDetail;
  const busy = isSaving || isUploading;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{isEdit ? "경품 수정" : "경품 등록"}</SheetTitle>
          <SheetDescription>{isEdit ? "경품 정보를 수정합니다." : "새로운 경품을 등록합니다."}</SheetDescription>
        </SheetHeader>

        {isLoadingDetail ? (
          <div className="flex-1 space-y-3 px-4">
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
          </div>
        ) : (
          <form
            id="prize-form"
            noValidate
            onSubmit={form.handleSubmit(onSubmit)}
            className="flex-1 overflow-y-auto px-4"
          >
            <FieldGroup className="gap-4">
              <Controller
                control={form.control}
                name="prizeRank"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="prize-rank">등수</FieldLabel>
                    <Input
                      id="prize-rank"
                      type="number"
                      min={1}
                      value={field.value ?? ""}
                      onChange={(e) => field.onChange(parseOptionalNumber(e.target.value))}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="name"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="prize-name">경품 이름</FieldLabel>
                    <Input
                      {...field}
                      id="prize-name"
                      placeholder="경품 이름을 입력하세요"
                      maxLength={PRIZE_NAME_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="brand"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="prize-brand">브랜드</FieldLabel>
                    <Input
                      {...field}
                      id="prize-brand"
                      placeholder="브랜드를 입력하세요"
                      maxLength={PRIZE_BRAND_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Field className="gap-1.5">
                <FieldLabel htmlFor="prize-image-file">이미지 (선택)</FieldLabel>
                {previewUrl ? (
                  // biome-ignore lint/performance/noImgElement: 업로드/CDN 이미지 미리보기
                  <img
                    src={previewUrl}
                    alt="경품 이미지 미리보기"
                    className="h-32 w-full rounded-md border object-cover"
                  />
                ) : null}
                <Input id="prize-image-file" type="file" accept="image/*" onChange={handleFileChange} disabled={busy} />
                {isUploading && <p className="text-muted-foreground text-sm">업로드 중...</p>}
              </Field>
            </FieldGroup>
          </form>
        )}

        <SheetFooter>
          <Button type="submit" form="prize-form" disabled={busy}>
            {isPending ? "저장 중..." : isEdit ? "수정" : "등록"}
          </Button>
          <SheetClose asChild>
            <Button variant="outline" disabled={busy}>
              취소
            </Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
