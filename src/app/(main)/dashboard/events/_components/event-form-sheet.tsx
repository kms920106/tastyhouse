"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE_BYTES } from "@/api/file/file.dto";
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
import { Textarea } from "@/components/ui/textarea";
import {
  createEventAction,
  fetchEventAction,
  updateEventAction,
  uploadEventImageAction,
} from "@/feature/event/actions";
import { EVENT_NAME_MAX, EVENT_STATUS_OPTIONS, EVENT_SUBTITLE_MAX } from "@/feature/event/constants";
import type { EventListItem } from "@/feature/event/domain";
import { EVENT_MESSAGE } from "@/feature/event/message";
import { type EventFormValues, eventFormSchema } from "@/feature/event/schema";

interface EventFormSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  event?: Pick<EventListItem, "id"> | null;
}

const EMPTY_VALUES: EventFormValues = {
  name: "",
  description: undefined,
  subtitle: undefined,
  thumbnailImageFileId: undefined,
  bannerImageFileId: undefined,
  contentHtml: undefined,
  status: "SCHEDULED",
  startAt: "",
  endAt: "",
};

/** "YYYY-MM-DDTHH:mm:ss" (LocalDateTime) -> "YYYY-MM-DDTHH:mm" (datetime-local) */
function toDateTimeLocal(value: string | null | undefined): string {
  if (!value) return "";
  return value.slice(0, 16);
}

export function EventFormSheet({ open, onOpenChange, event }: EventFormSheetProps) {
  const isEdit = Boolean(event);
  const [isPending, startTransition] = React.useTransition();
  const [isLoadingDetail, setIsLoadingDetail] = React.useState(false);
  const [uploading, setUploading] = React.useState<"thumbnail" | "banner" | null>(null);
  const [thumbnailPreview, setThumbnailPreview] = React.useState<string | undefined>(undefined);
  const [bannerPreview, setBannerPreview] = React.useState<string | undefined>(undefined);

  const form = useForm<EventFormValues>({
    resolver: zodResolver(eventFormSchema),
    defaultValues: EMPTY_VALUES,
  });

  // 시트가 열릴 때마다 대상 값으로 초기화한다. 수정 모드는 상세를 조회해 값/이미지를 확보한다.
  React.useEffect(() => {
    if (!open) return;

    if (!event) {
      form.reset(EMPTY_VALUES);
      setThumbnailPreview(undefined);
      setBannerPreview(undefined);
      return;
    }

    let active = true;
    setIsLoadingDetail(true);

    void fetchEventAction(event.id).then((result) => {
      if (!active) return;
      setIsLoadingDetail(false);

      if (!result.success || !result.data) {
        toast.error(result.message ?? EVENT_MESSAGE.DETAIL_LOAD_FAILED);
        onOpenChange(false);
        return;
      }

      const detail = result.data;
      form.reset({
        name: detail.name,
        description: detail.description ?? undefined,
        subtitle: detail.subtitle ?? undefined,
        thumbnailImageFileId: detail.thumbnailFile?.id ?? undefined,
        bannerImageFileId: detail.bannerFile?.id ?? undefined,
        contentHtml: detail.contentHtml ?? undefined,
        status: detail.status,
        startAt: toDateTimeLocal(detail.startAt),
        endAt: toDateTimeLocal(detail.endAt),
      });
      setThumbnailPreview(detail.thumbnailFile?.url);
      setBannerPreview(detail.bannerFile?.url);
    });

    return () => {
      active = false;
    };
  }, [open, event, form.reset, onOpenChange]);

  // 로컬 미리보기용 objectURL 을 언마운트 시 해제해 메모리 누수를 방지한다.
  React.useEffect(() => {
    return () => {
      for (const setter of [setThumbnailPreview, setBannerPreview]) {
        setter((prev) => {
          if (prev?.startsWith("blob:")) URL.revokeObjectURL(prev);
          return undefined;
        });
      }
    };
  }, []);

  async function handleFileChange(
    event: React.ChangeEvent<HTMLInputElement>,
    field: "thumbnailImageFileId" | "bannerImageFileId",
    setPreview: React.Dispatch<React.SetStateAction<string | undefined>>,
    kind: "thumbnail" | "banner",
  ) {
    const file = event.target.files?.[0];
    if (!file) return;

    // 업로드 전 클라이언트에서 형식/크기를 검증해 불필요한 요청과 서버 400 을 방지한다.
    if (!(ALLOWED_IMAGE_TYPES as readonly string[]).includes(file.type)) {
      toast.error(EVENT_MESSAGE.IMAGE_TYPE_INVALID);
      event.target.value = "";
      return;
    }
    if (file.size > MAX_IMAGE_SIZE_BYTES) {
      toast.error(EVENT_MESSAGE.IMAGE_SIZE_EXCEEDED);
      event.target.value = "";
      return;
    }

    setUploading(kind);
    const formData = new FormData();
    formData.append("file", file);

    const result = await uploadEventImageAction(formData);
    setUploading(null);

    if (!result.success || result.fileId === undefined) {
      toast.error(result.message ?? EVENT_MESSAGE.IMAGE_UPLOAD_FAILED);
      return;
    }

    // 업로드 API 는 fileId 만 반환하므로, 미리보기는 선택한 파일의 로컬 objectURL 로 표시한다.
    form.setValue(field, result.fileId, { shouldValidate: true });
    setPreview((prev) => {
      if (prev?.startsWith("blob:")) URL.revokeObjectURL(prev);
      return URL.createObjectURL(file);
    });
  }

  const onSubmit = (values: EventFormValues) => {
    startTransition(async () => {
      const { success, message } = event ? await updateEventAction(event.id, values) : await createEventAction(values);

      if (success) {
        toast.success(isEdit ? EVENT_MESSAGE.UPDATE_SUCCESS : EVENT_MESSAGE.CREATE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? EVENT_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  // biome-ignore lint/nursery/useNullishCoalescing: 모두 boolean/상태 플래그이므로 || 가 의도된 동작
  const busy = isPending || isLoadingDetail || uploading !== null;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{isEdit ? "이벤트 수정" : "이벤트 등록"}</SheetTitle>
          <SheetDescription>{isEdit ? "이벤트 정보를 수정합니다." : "새로운 이벤트를 등록합니다."}</SheetDescription>
        </SheetHeader>

        {isLoadingDetail ? (
          <div className="flex-1 space-y-3 px-4">
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-32 w-full" />
          </div>
        ) : (
          <form
            id="event-form"
            noValidate
            onSubmit={form.handleSubmit(onSubmit)}
            className="flex-1 overflow-y-auto px-4"
          >
            <FieldGroup className="gap-4">
              <Controller
                control={form.control}
                name="name"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="event-name">이벤트명</FieldLabel>
                    <Input
                      {...field}
                      id="event-name"
                      placeholder="이벤트명을 입력하세요"
                      maxLength={EVENT_NAME_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="subtitle"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="event-subtitle">부제목</FieldLabel>
                    <Input
                      {...field}
                      value={field.value ?? ""}
                      id="event-subtitle"
                      placeholder="부제목을 입력하세요 (선택)"
                      maxLength={EVENT_SUBTITLE_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="description"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="event-description">설명</FieldLabel>
                    <Textarea
                      {...field}
                      value={field.value ?? ""}
                      id="event-description"
                      placeholder="이벤트 설명을 입력하세요 (선택)"
                      rows={2}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="status"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="event-status">상태</FieldLabel>
                    <Select value={field.value} onValueChange={field.onChange} disabled={busy}>
                      <SelectTrigger id="event-status" className="w-full" aria-invalid={fieldState.invalid}>
                        <SelectValue placeholder="상태 선택" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectGroup>
                          {EVENT_STATUS_OPTIONS.map((option) => (
                            <SelectItem key={option.value} value={option.value}>
                              {option.label}
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
                name="thumbnailImageFileId"
                render={({ fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="event-thumbnail">썸네일 이미지</FieldLabel>
                    {thumbnailPreview ? (
                      // biome-ignore lint/performance/noImgElement: 업로드 직후 blob/CDN URL 미리보기
                      <img
                        src={thumbnailPreview}
                        alt="썸네일 미리보기"
                        className="h-32 w-full rounded-md border object-cover"
                      />
                    ) : null}
                    <Input
                      id="event-thumbnail"
                      type="file"
                      accept="image/*"
                      onChange={(e) => handleFileChange(e, "thumbnailImageFileId", setThumbnailPreview, "thumbnail")}
                      disabled={busy}
                      aria-invalid={fieldState.invalid}
                    />
                    {uploading === "thumbnail" && <p className="text-muted-foreground text-sm">업로드 중...</p>}
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="bannerImageFileId"
                render={({ fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="event-banner">배너 이미지</FieldLabel>
                    {bannerPreview ? (
                      // biome-ignore lint/performance/noImgElement: 업로드 직후 blob/CDN URL 미리보기
                      <img
                        src={bannerPreview}
                        alt="배너 미리보기"
                        className="h-32 w-full rounded-md border object-cover"
                      />
                    ) : null}
                    <Input
                      id="event-banner"
                      type="file"
                      accept="image/*"
                      onChange={(e) => handleFileChange(e, "bannerImageFileId", setBannerPreview, "banner")}
                      disabled={busy}
                      aria-invalid={fieldState.invalid}
                    />
                    {uploading === "banner" && <p className="text-muted-foreground text-sm">업로드 중...</p>}
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="contentHtml"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="event-content-html">본문 HTML</FieldLabel>
                    <Textarea
                      {...field}
                      value={field.value ?? ""}
                      id="event-content-html"
                      placeholder="<p>내용</p> (선택)"
                      rows={5}
                      className="font-mono text-sm"
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="startAt"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="event-start-at">시작 일시</FieldLabel>
                    <Input
                      {...field}
                      value={field.value ?? ""}
                      id="event-start-at"
                      type="datetime-local"
                      step={1}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="endAt"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="event-end-at">종료 일시</FieldLabel>
                    <Input
                      {...field}
                      value={field.value ?? ""}
                      id="event-end-at"
                      type="datetime-local"
                      step={1}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />
            </FieldGroup>
          </form>
        )}

        <SheetFooter>
          <Button type="submit" form="event-form" disabled={busy}>
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
