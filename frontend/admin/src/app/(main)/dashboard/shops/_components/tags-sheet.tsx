"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldLabel } from "@/components/ui/field";
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
import { Skeleton } from "@/components/ui/skeleton";
import { createTagAction, deleteTagAction, fetchTagsAction } from "@/feature/shop/actions";
import { TAG_NAME_MAX } from "@/feature/shop/constants";
import type { Tag } from "@/feature/shop/domain";
import { SHOP_MESSAGE } from "@/feature/shop/message";
import { type TagFormValues, tagSchema } from "@/feature/shop/schema";

interface TagsSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const EMPTY_VALUES: TagFormValues = { tagName: "" };

export function TagsSheet({ open, onOpenChange }: TagsSheetProps) {
  const [tags, setTags] = React.useState<Tag[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [isPending, startTransition] = React.useTransition();
  const [deletingId, setDeletingId] = React.useState<number | null>(null);

  const form = useForm<TagFormValues>({
    resolver: zodResolver(tagSchema),
    defaultValues: EMPTY_VALUES,
  });

  const loadTags = React.useCallback(() => {
    let active = true;
    setIsLoading(true);
    setError(null);

    void fetchTagsAction().then((result) => {
      if (!active) return;
      setIsLoading(false);
      if (result.success && result.data) {
        setTags(result.data);
      } else {
        setError(result.message ?? SHOP_MESSAGE.TAGS_LOAD_FAILED);
      }
    });

    return () => {
      active = false;
    };
  }, []);

  React.useEffect(() => {
    if (!open) return;
    form.reset(EMPTY_VALUES);
    const cleanup = loadTags();
    return cleanup;
  }, [open, form.reset, loadTags]);

  const onSubmit = (values: TagFormValues) => {
    startTransition(async () => {
      const { success, message } = await createTagAction(values);
      if (success) {
        toast.success(SHOP_MESSAGE.TAG_CREATE_SUCCESS);
        form.reset(EMPTY_VALUES);
        loadTags();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  function handleDelete(tagId: number) {
    setDeletingId(tagId);
    startTransition(async () => {
      const { success, message } = await deleteTagAction(tagId);
      setDeletingId(null);
      if (success) {
        toast.success(SHOP_MESSAGE.TAG_DELETE_SUCCESS);
        loadTags();
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>태그 관리</SheetTitle>
          <SheetDescription>전역 태그를 등록하고 삭제합니다.</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-6 overflow-y-auto px-4">
          <div className="space-y-2">
            <h4 className="font-medium text-sm">태그 목록</h4>
            {error ? (
              <p className="text-destructive text-sm">{error}</p>
            ) : isLoading ? (
              <div className="space-y-2">
                <Skeleton className="h-8 w-full" />
                <Skeleton className="h-8 w-full" />
              </div>
            ) : tags.length ? (
              <ul className="space-y-1">
                {tags.map((tag) => (
                  <li key={tag.id} className="flex items-center justify-between rounded-md border px-3 py-2 text-sm">
                    <span>{tag.tagName}</span>
                    <Button
                      type="button"
                      size="sm"
                      variant="ghost"
                      className="text-destructive"
                      disabled={isPending && deletingId === tag.id}
                      onClick={() => handleDelete(tag.id)}
                    >
                      {isPending && deletingId === tag.id ? "삭제 중..." : "삭제"}
                    </Button>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-muted-foreground text-sm">등록된 태그가 없습니다.</p>
            )}
          </div>

          <Separator />

          <div className="space-y-3">
            <h4 className="font-medium text-sm">태그 추가</h4>
            <form id="tag-form" noValidate onSubmit={form.handleSubmit(onSubmit)} className="flex items-end gap-2">
              <Controller
                control={form.control}
                name="tagName"
                render={({ field, fieldState }) => (
                  <Field className="flex-1 gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="tag-name">태그명</FieldLabel>
                    <Input
                      {...field}
                      id="tag-name"
                      placeholder="예: 혼밥"
                      maxLength={TAG_NAME_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={isPending}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />
              <Button type="submit" size="sm" disabled={isPending}>
                {isPending && deletingId === null ? "추가 중..." : "태그 추가"}
              </Button>
            </form>
          </div>
        </div>

        <SheetFooter>
          <SheetClose asChild>
            <Button variant="outline">닫기</Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
