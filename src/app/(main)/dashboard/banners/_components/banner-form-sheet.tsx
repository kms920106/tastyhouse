"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
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
import { Switch } from "@/components/ui/switch";
import {
  createBannerAction,
  fetchBannerAction,
  updateBannerAction,
  uploadBannerImageAction,
} from "@/feature/banner/actions";
import type { BannerListItem } from "@/feature/banner/domain";
import { BANNER_MESSAGE } from "@/feature/banner/message";
import {
  BANNER_LINK_URL_MAX,
  BANNER_TITLE_MAX,
  type BannerFormValues,
  bannerFormSchema,
} from "@/feature/banner/schema";

interface BannerFormSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  banner?: Pick<BannerListItem, "id"> | null;
}

const EMPTY_VALUES: BannerFormValues = {
  type: "HOME",
  title: undefined,
  imageFileId: 0,
  linkUrl: undefined,
  startDate: undefined,
  endDate: undefined,
  sort: 0,
  visible: true,
};

/** "YYYY-MM-DDTHH:mm:ss" (LocalDateTime) -> "YYYY-MM-DDTHH:mm" (datetime-local) */
function toDateTimeLocal(value: string | null | undefined): string | undefined {
  if (!value) return undefined;
  return value.slice(0, 16);
}

export function BannerFormSheet({ open, onOpenChange, banner }: BannerFormSheetProps) {
  const isEdit = Boolean(banner);
  const [isPending, startTransition] = React.useTransition();
  const [isLoadingDetail, setIsLoadingDetail] = React.useState(false);
  const [isUploading, setIsUploading] = React.useState(false);
  const [previewUrl, setPreviewUrl] = React.useState<string | undefined>(undefined);

  const form = useForm<BannerFormValues>({
    resolver: zodResolver(bannerFormSchema),
    defaultValues: EMPTY_VALUES,
  });

  // 시트가 열릴 때마다 대상 값으로 초기화한다. 수정 모드는 상세를 조회해 image 를 확보한다.
  React.useEffect(() => {
    if (!open) return;

    if (!banner) {
      form.reset(EMPTY_VALUES);
      setPreviewUrl(undefined);
      return;
    }

    let active = true;
    setIsLoadingDetail(true);

    void fetchBannerAction(banner.id).then((result) => {
      if (!active) return;
      setIsLoadingDetail(false);

      if (!result.success || !result.data) {
        toast.error(result.message ?? BANNER_MESSAGE.DETAIL_LOAD_FAILED);
        onOpenChange(false);
        return;
      }

      const detail = result.data;
      form.reset({
        type: detail.type,
        title: detail.title ?? undefined,
        imageFileId: detail.image.id,
        linkUrl: detail.linkUrl ?? undefined,
        startDate: toDateTimeLocal(detail.startDate),
        endDate: toDateTimeLocal(detail.endDate),
        sort: detail.sort,
        visible: detail.visible,
      });
      setPreviewUrl(detail.image.url);
    });

    return () => {
      active = false;
    };
  }, [open, banner, form.reset, onOpenChange]);

  async function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) return;

    setIsUploading(true);
    const formData = new FormData();
    formData.append("file", file);

    const result = await uploadBannerImageAction(formData);
    setIsUploading(false);

    if (!result.success || !result.data) {
      toast.error(result.message ?? BANNER_MESSAGE.IMAGE_UPLOAD_FAILED);
      return;
    }

    form.setValue("imageFileId", result.data.id, { shouldValidate: true });
    setPreviewUrl(result.data.url);
  }

  const onSubmit = (values: BannerFormValues) => {
    startTransition(async () => {
      const { success, message } = banner
        ? await updateBannerAction(banner.id, values)
        : await createBannerAction(values);

      if (success) {
        toast.success(isEdit ? BANNER_MESSAGE.UPDATE_SUCCESS : BANNER_MESSAGE.CREATE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? BANNER_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  // biome-ignore lint/nursery/useNullishCoalescing: 모두 boolean 플래그이므로 || 가 의도된 동작
  const busy = isPending || isLoadingDetail || isUploading;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{isEdit ? "배너 수정" : "배너 등록"}</SheetTitle>
          <SheetDescription>{isEdit ? "배너 정보를 수정합니다." : "새로운 배너를 등록합니다."}</SheetDescription>
        </SheetHeader>

        {isLoadingDetail ? (
          <div className="flex-1 space-y-3 px-4">
            <Skeleton className="h-40 w-full" />
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
          </div>
        ) : (
          <form
            id="banner-form"
            noValidate
            onSubmit={form.handleSubmit(onSubmit)}
            className="flex-1 overflow-y-auto px-4"
          >
            <FieldGroup className="gap-4">
              <Controller
                control={form.control}
                name="type"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="banner-type">배너 유형</FieldLabel>
                    <Select value={field.value} onValueChange={field.onChange} disabled={busy}>
                      <SelectTrigger id="banner-type" className="w-full" aria-invalid={fieldState.invalid}>
                        <SelectValue placeholder="유형 선택" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectGroup>
                          <SelectItem value="HOME">홈</SelectItem>
                          <SelectItem value="SIDEBAR">사이드바</SelectItem>
                        </SelectGroup>
                      </SelectContent>
                    </Select>
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="title"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="banner-title">제목</FieldLabel>
                    <Input
                      {...field}
                      value={field.value ?? ""}
                      id="banner-title"
                      placeholder="배너 제목을 입력하세요"
                      maxLength={BANNER_TITLE_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="imageFileId"
                render={({ fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="banner-image">이미지</FieldLabel>
                    {previewUrl ? (
                      // biome-ignore lint/performance/noImgElement: 업로드 직후 blob/CDN URL 미리보기
                      <img
                        src={previewUrl}
                        alt="배너 이미지 미리보기"
                        className="h-32 w-full rounded-md border object-cover"
                      />
                    ) : null}
                    <Input
                      id="banner-image"
                      type="file"
                      accept="image/*"
                      onChange={handleFileChange}
                      disabled={busy}
                      aria-invalid={fieldState.invalid}
                    />
                    {isUploading && <p className="text-muted-foreground text-sm">업로드 중...</p>}
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="linkUrl"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="banner-link-url">링크 URL</FieldLabel>
                    <Input
                      {...field}
                      value={field.value ?? ""}
                      id="banner-link-url"
                      placeholder="https://example.com"
                      maxLength={BANNER_LINK_URL_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="startDate"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="banner-start-date">노출 시작일시</FieldLabel>
                    <Input
                      {...field}
                      value={field.value ?? ""}
                      id="banner-start-date"
                      type="datetime-local"
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="endDate"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="banner-end-date">노출 종료일시</FieldLabel>
                    <Input
                      {...field}
                      value={field.value ?? ""}
                      id="banner-end-date"
                      type="datetime-local"
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="sort"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="banner-sort">정렬 순서</FieldLabel>
                    <Input
                      id="banner-sort"
                      type="number"
                      value={field.value}
                      onChange={(e) => field.onChange(Number(e.target.value))}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="visible"
                render={({ field }) => (
                  <Field orientation="horizontal">
                    <FieldLabel htmlFor="banner-visible">노출 여부</FieldLabel>
                    <Switch
                      id="banner-visible"
                      checked={field.value}
                      onCheckedChange={field.onChange}
                      disabled={busy}
                    />
                  </Field>
                )}
              />
            </FieldGroup>
          </form>
        )}

        <SheetFooter>
          <Button type="submit" form="banner-form" disabled={busy}>
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
