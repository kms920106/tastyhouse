"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { MoreHorizontal, Plus } from "lucide-react";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
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
import { createFaqCategoryAction, deleteFaqCategoryAction, updateFaqCategoryAction } from "@/feature/faq/actions";
import type { FaqCategory } from "@/feature/faq/domain";
import { FAQ_CATEGORY_MESSAGE, FAQ_CATEGORY_PAGE_COPY } from "@/feature/faq/message";
import { FAQ_CATEGORY_NAME_MAX, type FaqCategoryFormValues, faqCategoryFormSchema } from "@/feature/faq/schema";

interface FaqCategoryManagerSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  categories: FaqCategory[];
}

const EMPTY_VALUES: FaqCategoryFormValues = {
  name: "",
  sort: 1,
  visible: true,
};

export function FaqCategoryManagerSheet({ open, onOpenChange, categories }: FaqCategoryManagerSheetProps) {
  const [editing, setEditing] = React.useState<FaqCategory | null>(null);
  const [deleting, setDeleting] = React.useState<FaqCategory | null>(null);
  const [isPending, startTransition] = React.useTransition();
  const [isDeleting, startDeleteTransition] = React.useTransition();

  const isEdit = Boolean(editing);

  const form = useForm<FaqCategoryFormValues>({
    resolver: zodResolver(faqCategoryFormSchema),
    defaultValues: EMPTY_VALUES,
  });

  React.useEffect(() => {
    if (!open) {
      setEditing(null);
      form.reset(EMPTY_VALUES);
    }
  }, [open, form]);

  function openCreate() {
    setEditing(null);
    form.reset(EMPTY_VALUES);
  }

  function openEdit(category: FaqCategory) {
    setEditing(category);
    form.reset({ name: category.name, sort: category.sort, visible: category.visible });
  }

  const onSubmit = (values: FaqCategoryFormValues) => {
    startTransition(async () => {
      const { success, message } = editing
        ? await updateFaqCategoryAction(editing.id, values)
        : await createFaqCategoryAction(values);

      if (success) {
        toast.success(isEdit ? FAQ_CATEGORY_MESSAGE.UPDATE_SUCCESS : FAQ_CATEGORY_MESSAGE.CREATE_SUCCESS);
        openCreate();
      } else {
        toast.error(message ?? FAQ_CATEGORY_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  function handleDelete() {
    if (!deleting) return;
    startDeleteTransition(async () => {
      const { success, message } = await deleteFaqCategoryAction(deleting.id);
      if (success) {
        toast.success(FAQ_CATEGORY_MESSAGE.DELETE_SUCCESS);
        setDeleting(null);
        if (editing?.id === deleting.id) openCreate();
      } else {
        toast.error(message ?? FAQ_CATEGORY_MESSAGE.DELETE_FAILED);
        setDeleting(null);
      }
    });
  }

  const sortedCategories = [...categories].sort((a, b) => a.sort - b.sort);

  return (
    <>
      <Sheet open={open} onOpenChange={onOpenChange}>
        <SheetContent className="flex w-full flex-col sm:max-w-lg">
          <SheetHeader>
            <SheetTitle>{FAQ_CATEGORY_PAGE_COPY.TITLE}</SheetTitle>
            <SheetDescription>{FAQ_CATEGORY_PAGE_COPY.DESCRIPTION}</SheetDescription>
          </SheetHeader>

          <div className="flex-1 space-y-5 overflow-y-auto px-4">
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <h4 className="font-medium text-sm">카테고리 목록</h4>
                <Button size="sm" variant="outline" onClick={openCreate} disabled={isPending}>
                  <Plus className="size-4" />
                  신규
                </Button>
              </div>

              {sortedCategories.length === 0 ? (
                <p className="py-6 text-center text-muted-foreground text-sm">등록된 카테고리가 없습니다.</p>
              ) : (
                <ul className="divide-y rounded-md border">
                  {sortedCategories.map((category) => (
                    <li key={category.id} className="flex items-center justify-between gap-2 px-3 py-2">
                      <div className="flex min-w-0 items-center gap-2">
                        <span className="tabular-nums text-muted-foreground text-xs">{category.sort}</span>
                        <span className="line-clamp-1 text-sm">{category.name}</span>
                        <Badge variant={category.visible ? "default" : "secondary"} className="shrink-0">
                          {category.visible ? "노출" : "미노출"}
                        </Badge>
                      </div>
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="size-8 shrink-0"
                            aria-label="카테고리 작업 메뉴"
                          >
                            <MoreHorizontal className="size-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem onSelect={() => openEdit(category)}>수정</DropdownMenuItem>
                          <DropdownMenuSeparator />
                          <DropdownMenuItem variant="destructive" onSelect={() => setDeleting(category)}>
                            삭제
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </li>
                  ))}
                </ul>
              )}
            </div>

            <div className="space-y-3 border-t pt-4">
              <h4 className="font-medium text-sm">{isEdit ? "카테고리 수정" : "카테고리 등록"}</h4>
              <form id="faq-category-form" noValidate onSubmit={form.handleSubmit(onSubmit)}>
                <FieldGroup className="gap-4">
                  <Controller
                    control={form.control}
                    name="name"
                    render={({ field, fieldState }) => (
                      <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                        <FieldLabel htmlFor="faq-category-name">카테고리 이름</FieldLabel>
                        <Input
                          {...field}
                          id="faq-category-name"
                          placeholder="예: 결제"
                          maxLength={FAQ_CATEGORY_NAME_MAX}
                          aria-invalid={fieldState.invalid}
                          disabled={isPending}
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
                        <FieldLabel htmlFor="faq-category-sort">정렬 순서</FieldLabel>
                        <Input
                          id="faq-category-sort"
                          type="number"
                          value={field.value}
                          onChange={(e) => field.onChange(Number(e.target.value))}
                          aria-invalid={fieldState.invalid}
                          disabled={isPending}
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
                        <FieldLabel htmlFor="faq-category-visible">노출 여부</FieldLabel>
                        <Switch
                          id="faq-category-visible"
                          checked={field.value}
                          onCheckedChange={field.onChange}
                          disabled={isPending}
                        />
                      </Field>
                    )}
                  />
                </FieldGroup>
              </form>
              <div className="flex gap-2">
                <Button type="submit" form="faq-category-form" size="sm" disabled={isPending}>
                  {isPending ? "저장 중..." : isEdit ? "수정" : "등록"}
                </Button>
                {isEdit && (
                  <Button type="button" size="sm" variant="outline" onClick={openCreate} disabled={isPending}>
                    취소
                  </Button>
                )}
              </div>
            </div>
          </div>

          <SheetFooter>
            <SheetClose asChild>
              <Button variant="outline">닫기</Button>
            </SheetClose>
          </SheetFooter>
        </SheetContent>
      </Sheet>

      <AlertDialog open={deleting != null} onOpenChange={(next) => !next && setDeleting(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>카테고리를 삭제하시겠습니까?</AlertDialogTitle>
            <AlertDialogDescription>
              {deleting ? `"${deleting.name}" 카테고리가 영구적으로 삭제됩니다. 이 작업은 되돌릴 수 없습니다.` : ""}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isDeleting}>취소</AlertDialogCancel>
            <AlertDialogAction
              onClick={(event) => {
                event.preventDefault();
                handleDelete();
              }}
              disabled={isDeleting}
            >
              {isDeleting ? "삭제 중..." : "삭제"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
