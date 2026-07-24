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
import { Textarea } from "@/components/ui/textarea";
import { createFaqAction, fetchFaqAction, updateFaqAction } from "@/feature/faq/actions";
import type { FaqCategory, FaqListItem } from "@/feature/faq/domain";
import { FAQ_MESSAGE } from "@/feature/faq/message";
import { FAQ_QUESTION_MAX, type FaqFormValues, faqFormSchema } from "@/feature/faq/schema";

interface FaqFormSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  faq?: Pick<FaqListItem, "id"> | null;
  categories: FaqCategory[];
}

const EMPTY_VALUES: FaqFormValues = {
  faqCategoryId: 0,
  question: "",
  answer: "",
  sort: 1,
  visible: true,
};

export function FaqFormSheet({ open, onOpenChange, faq, categories }: FaqFormSheetProps) {
  const isEdit = Boolean(faq);
  const [isPending, startTransition] = React.useTransition();
  const [isLoadingDetail, setIsLoadingDetail] = React.useState(false);

  const form = useForm<FaqFormValues>({
    resolver: zodResolver(faqFormSchema),
    defaultValues: EMPTY_VALUES,
  });

  // 시트가 열릴 때마다 대상 값으로 초기화한다. 수정 모드는 상세를 조회해 값을 확보한다.
  React.useEffect(() => {
    if (!open) return;

    if (!faq) {
      form.reset({ ...EMPTY_VALUES, faqCategoryId: categories[0]?.id ?? 0 });
      return;
    }

    let active = true;
    setIsLoadingDetail(true);

    void fetchFaqAction(faq.id).then((result) => {
      if (!active) return;
      setIsLoadingDetail(false);

      if (!result.success || !result.data) {
        toast.error(result.message ?? FAQ_MESSAGE.DETAIL_LOAD_FAILED);
        onOpenChange(false);
        return;
      }

      const detail = result.data;
      form.reset({
        faqCategoryId: detail.faqCategoryId,
        question: detail.question,
        answer: detail.answer,
        sort: detail.sort,
        visible: detail.visible,
      });
    });

    return () => {
      active = false;
    };
  }, [open, faq, form.reset, onOpenChange, categories]);

  const onSubmit = (values: FaqFormValues) => {
    startTransition(async () => {
      const { success, message } = faq ? await updateFaqAction(faq.id, values) : await createFaqAction(values);

      if (success) {
        toast.success(isEdit ? FAQ_MESSAGE.UPDATE_SUCCESS : FAQ_MESSAGE.CREATE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? FAQ_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  const busy = isPending || isLoadingDetail;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{isEdit ? "FAQ 수정" : "FAQ 등록"}</SheetTitle>
          <SheetDescription>{isEdit ? "FAQ 항목을 수정합니다." : "새로운 FAQ 항목을 등록합니다."}</SheetDescription>
        </SheetHeader>

        {isLoadingDetail ? (
          <div className="flex-1 space-y-3 px-4">
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-32 w-full" />
          </div>
        ) : (
          <form id="faq-form" noValidate onSubmit={form.handleSubmit(onSubmit)} className="flex-1 overflow-y-auto px-4">
            <FieldGroup className="gap-4">
              <Controller
                control={form.control}
                name="faqCategoryId"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="faq-category">카테고리</FieldLabel>
                    <Select
                      value={field.value ? String(field.value) : ""}
                      onValueChange={(value) => field.onChange(Number(value))}
                      disabled={busy}
                    >
                      <SelectTrigger id="faq-category" className="w-full" aria-invalid={fieldState.invalid}>
                        <SelectValue placeholder="카테고리 선택" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectGroup>
                          {categories.map((category) => (
                            <SelectItem key={category.id} value={String(category.id)}>
                              {category.name}
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
                name="question"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="faq-question">질문</FieldLabel>
                    <Input
                      {...field}
                      id="faq-question"
                      placeholder="질문을 입력하세요"
                      maxLength={FAQ_QUESTION_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="answer"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="faq-answer">답변</FieldLabel>
                    <Textarea
                      {...field}
                      id="faq-answer"
                      placeholder="답변을 입력하세요"
                      rows={8}
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
                    <FieldLabel htmlFor="faq-sort">정렬 순서</FieldLabel>
                    <Input
                      id="faq-sort"
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
                    <FieldLabel htmlFor="faq-visible">노출 여부</FieldLabel>
                    <Switch id="faq-visible" checked={field.value} onCheckedChange={field.onChange} disabled={busy} />
                  </Field>
                )}
              />
            </FieldGroup>
          </form>
        )}

        <SheetFooter>
          <Button type="submit" form="faq-form" disabled={busy}>
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
