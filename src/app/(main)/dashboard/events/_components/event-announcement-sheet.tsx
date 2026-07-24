"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

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
import { Textarea } from "@/components/ui/textarea";
import {
  createEventAnnouncementAction,
  fetchEventAnnouncementAction,
  updateEventAnnouncementAction,
} from "@/feature/event/actions";
import { ANNOUNCEMENT_CONTENT_MAX, ANNOUNCEMENT_NAME_MAX } from "@/feature/event/constants";
import type { EventListItem } from "@/feature/event/domain";
import { EVENT_MESSAGE } from "@/feature/event/message";
import { type AnnouncementFormValues, announcementFormSchema } from "@/feature/event/schema";

interface EventAnnouncementSheetProps {
  /** 공지 관리 대상 이벤트. null 이면 닫힌 상태. */
  event: Pick<EventListItem, "id" | "name"> | null;
  onOpenChange: (open: boolean) => void;
}

const EMPTY_VALUES: AnnouncementFormValues = {
  name: "",
  content: "",
  announcedAt: "",
};

/** "YYYY-MM-DDTHH:mm:ss" (LocalDateTime) -> "YYYY-MM-DDTHH:mm" (datetime-local) */
function toDateTimeLocal(value: string | null | undefined): string {
  if (!value) return "";
  return value.slice(0, 16);
}

export function EventAnnouncementSheet({ event, onOpenChange }: EventAnnouncementSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const [isLoading, setIsLoading] = React.useState(false);
  // 공지가 이미 존재하면 수정 모드, 없으면(404) 등록 모드.
  const [hasAnnouncement, setHasAnnouncement] = React.useState(false);

  const form = useForm<AnnouncementFormValues>({
    resolver: zodResolver(announcementFormSchema),
    defaultValues: EMPTY_VALUES,
  });

  const eventId = event?.id ?? null;

  React.useEffect(() => {
    if (eventId == null) return;

    let active = true;
    setIsLoading(true);
    form.reset(EMPTY_VALUES);
    setHasAnnouncement(false);

    void fetchEventAnnouncementAction(eventId).then((result) => {
      if (!active) return;
      setIsLoading(false);

      if (result.success && result.data) {
        setHasAnnouncement(true);
        form.reset({
          name: result.data.name,
          content: result.data.content,
          announcedAt: toDateTimeLocal(result.data.announcedAt),
        });
      }
      // 404(공지 없음)는 등록 모드로 처리 — 에러 토스트를 띄우지 않는다.
    });

    return () => {
      active = false;
    };
  }, [eventId, form.reset]);

  const onSubmit = (values: AnnouncementFormValues) => {
    if (eventId == null) return;
    startTransition(async () => {
      const { success, message } = hasAnnouncement
        ? await updateEventAnnouncementAction(eventId, values)
        : await createEventAnnouncementAction(eventId, values);

      if (success) {
        toast.success(
          hasAnnouncement ? EVENT_MESSAGE.ANNOUNCEMENT_UPDATE_SUCCESS : EVENT_MESSAGE.ANNOUNCEMENT_CREATE_SUCCESS,
        );
        setHasAnnouncement(true);
        onOpenChange(false);
      } else {
        toast.error(message ?? EVENT_MESSAGE.ANNOUNCEMENT_SAVE_FAILED);
      }
    });
  };

  const busy = isPending || isLoading;

  return (
    <Sheet open={event != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>당첨자 발표 공지</SheetTitle>
          <SheetDescription>{event ? `"${event.name}" 이벤트의 당첨자 발표 공지를 관리합니다.` : ""}</SheetDescription>
        </SheetHeader>

        {isLoading ? (
          <div className="flex-1 space-y-3 px-4">
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-32 w-full" />
            <Skeleton className="h-8 w-full" />
          </div>
        ) : (
          <form
            id="announcement-form"
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
                    <FieldLabel htmlFor="announcement-name">발표 제목</FieldLabel>
                    <Input
                      {...field}
                      id="announcement-name"
                      placeholder="발표 제목을 입력하세요"
                      maxLength={ANNOUNCEMENT_NAME_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="content"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="announcement-content">발표 내용</FieldLabel>
                    <Textarea
                      {...field}
                      id="announcement-content"
                      placeholder="발표 내용을 입력하세요"
                      maxLength={ANNOUNCEMENT_CONTENT_MAX}
                      rows={5}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="announcedAt"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="announcement-announced-at">발표 일시</FieldLabel>
                    <Input
                      {...field}
                      value={field.value ?? ""}
                      id="announcement-announced-at"
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
          <Button type="submit" form="announcement-form" disabled={busy}>
            {isPending ? "저장 중..." : hasAnnouncement ? "수정" : "등록"}
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
