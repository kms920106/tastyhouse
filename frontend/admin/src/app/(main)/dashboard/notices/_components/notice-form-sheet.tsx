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
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import { createNoticeAction, updateNoticeAction } from "@/feature/notice/actions";
import type { NoticeListItem } from "@/feature/notice/domain";
import { NOTICE_MESSAGE } from "@/feature/notice/message";
import { NOTICE_CONTENT_MAX, NOTICE_TITLE_MAX, type NoticeFormValues, noticeFormSchema } from "@/feature/notice/schema";

interface NoticeFormSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  notice?: Pick<NoticeListItem, "id" | "title" | "content" | "visible"> | null;
}

export function NoticeFormSheet({ open, onOpenChange, notice }: NoticeFormSheetProps) {
  const isEdit = Boolean(notice);
  const [isPending, startTransition] = React.useTransition();

  const form = useForm<NoticeFormValues>({
    resolver: zodResolver(noticeFormSchema),
    defaultValues: { title: "", content: "", visible: true },
  });

  // 시트가 열릴 때마다 대상 값으로 초기화한다.
  React.useEffect(() => {
    if (open) {
      form.reset({
        title: notice?.title ?? "",
        content: notice?.content ?? "",
        visible: notice?.visible ?? true,
      });
    }
  }, [open, notice, form]);

  const onSubmit = (values: NoticeFormValues) => {
    startTransition(async () => {
      const { success, message } = notice
        ? await updateNoticeAction(notice.id, values)
        : await createNoticeAction(values);

      if (success) {
        toast.success(isEdit ? NOTICE_MESSAGE.UPDATE_SUCCESS : NOTICE_MESSAGE.CREATE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? NOTICE_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{isEdit ? "공지사항 수정" : "공지사항 등록"}</SheetTitle>
          <SheetDescription>
            {isEdit ? "공지사항의 제목과 내용을 수정합니다." : "새로운 공지사항을 등록합니다."}
          </SheetDescription>
        </SheetHeader>

        <form
          id="notice-form"
          noValidate
          onSubmit={form.handleSubmit(onSubmit)}
          className="flex-1 overflow-y-auto px-4"
        >
          <FieldGroup className="gap-4">
            <Controller
              control={form.control}
              name="title"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="notice-title">제목</FieldLabel>
                  <Input
                    {...field}
                    id="notice-title"
                    placeholder="공지 제목을 입력하세요"
                    maxLength={NOTICE_TITLE_MAX}
                    aria-invalid={fieldState.invalid}
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
                  <FieldLabel htmlFor="notice-content">내용</FieldLabel>
                  <Textarea
                    {...field}
                    id="notice-content"
                    placeholder="공지 내용을 입력하세요"
                    maxLength={NOTICE_CONTENT_MAX}
                    rows={10}
                    aria-invalid={fieldState.invalid}
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
                  <FieldLabel htmlFor="notice-visible">노출 여부</FieldLabel>
                  <Switch id="notice-visible" checked={field.value} onCheckedChange={field.onChange} />
                </Field>
              )}
            />
          </FieldGroup>
        </form>

        <SheetFooter>
          <Button type="submit" form="notice-form" disabled={isPending}>
            {isPending ? "저장 중..." : isEdit ? "수정" : "등록"}
          </Button>
          <SheetClose asChild>
            <Button variant="outline" disabled={isPending}>
              취소
            </Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
