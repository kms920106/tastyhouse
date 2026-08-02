"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
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
import {
  createContentBoardAction,
  deleteContentBoardAction,
  fetchContentBoardsAction,
  updateContentBoardAction,
} from "@/feature/shop/actions";
import {
  CONTENT_BOARD_DESCRIPTION_MAX,
  CONTENT_BOARD_MAX_COUNT,
  CONTENT_BOARD_TOPIC_LABEL,
  CONTENT_BOARD_TOPIC_OPTIONS,
  CONTENT_BOARD_TYPE_LABEL,
  CONTENT_BOARD_TYPE_OPTIONS,
  type ContentBoardTopicOption,
  type ContentBoardTypeOption,
} from "@/feature/shop/constants";
import type { ContentBoardItem } from "@/feature/shop/domain";
import { SHOP_BASIC_COPY, SHOP_MESSAGE } from "@/feature/shop/message";
import { type ContentBoardFormValues, contentBoardSchema } from "@/feature/shop/schema";

import { validateImageFile } from "./use-image-file-select";

interface ContentBoardSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  contentBoards: ContentBoardItem[];
}

const EMPTY_CONTENT_BOARD: ContentBoardFormValues = {
  contentType: "IMAGE",
  topic: "EXTERIOR",
  youtubeUrl: undefined,
  description: "",
  hasExistingFile: false,
};

export function ContentBoardSheet({ open, onOpenChange, shopId, contentBoards }: ContentBoardSheetProps) {
  const inputRef = React.useRef<HTMLInputElement>(null);
  const [items, setItems] = React.useState<ContentBoardItem[]>(contentBoards);
  const [isPending, startTransition] = React.useTransition();
  const [isValidating, setIsValidating] = React.useState(false);
  // 스펙이 multipart 직접 전송으로 바뀌어 선업로드(fileId) 없이 원본 File 을 폼 제출까지 들고 간다.
  const [attachedFile, setAttachedFile] = React.useState<File | null>(null);
  // 수정 모드에서 편집 중인 항목 id — null 이면 신규 등록 모드.
  const [editingId, setEditingId] = React.useState<number | null>(null);

  const form = useForm<ContentBoardFormValues>({
    resolver: zodResolver(contentBoardSchema),
    defaultValues: EMPTY_CONTENT_BOARD,
  });

  const resetForm = React.useCallback(() => {
    form.reset(EMPTY_CONTENT_BOARD);
    setAttachedFile(null);
    setEditingId(null);
  }, [form]);

  React.useEffect(() => {
    if (open) {
      setItems(contentBoards);
      resetForm();
    }
  }, [open, contentBoards, resetForm]);

  const contentType = form.watch("contentType");
  const isVideo = contentType === "VIDEO";
  // 수정 모드에서는 기존 항목을 대체하므로 최대 건수 제한을 적용하지 않는다.
  const isMaxReached = editingId === null && items.length >= CONTENT_BOARD_MAX_COUNT;
  const isBusy = isPending || isValidating;

  const reload = React.useCallback(() => {
    startTransition(async () => {
      const { success, data } = await fetchContentBoardsAction(shopId);
      if (success && data) setItems(data);
    });
  }, [shopId]);

  function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file) return;

    startTransition(async () => {
      setIsValidating(true);
      const error = await validateImageFile(file, contentType === "GIF" ? "contentBoardGif" : "contentBoardImage");
      setIsValidating(false);
      if (error) {
        toast.error(error);
        return;
      }
      setAttachedFile(file);
      form.setValue("hasExistingFile", true, { shouldValidate: true });
    });
  }

  const onSubmit = (values: ContentBoardFormValues) => {
    if (isMaxReached) {
      toast.error(SHOP_MESSAGE.CONTENT_BOARD_MAX_REACHED);
      return;
    }

    // 액션은 파싱된 값이 아닌 FormData 를 받으므로 폼 값과 원본 File 을 합쳐 직접 구성한다.
    const formData = new FormData();
    formData.append("contentType", values.contentType);
    formData.append("topic", values.topic);
    formData.append("description", values.description);
    if (values.youtubeUrl) formData.append("youtubeUrl", values.youtubeUrl);
    if (attachedFile) formData.append("file", attachedFile);
    // 수정 시 파일을 다시 첨부하지 않으면 기존 이미지를 유지하므로 검증만 통과시킨다.
    if (values.hasExistingFile) formData.append("hasExistingFile", "true");

    startTransition(async () => {
      const { success, message } =
        editingId === null
          ? await createContentBoardAction(shopId, formData)
          : await updateContentBoardAction(shopId, editingId, formData);

      if (success) {
        toast.success(
          editingId === null ? SHOP_MESSAGE.CONTENT_BOARD_CREATE_SUCCESS : SHOP_MESSAGE.CONTENT_BOARD_UPDATE_SUCCESS,
        );
        resetForm();
        reload();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  function handleEdit(target: ContentBoardItem) {
    setEditingId(target.id);
    setAttachedFile(null);
    form.reset({
      contentType: target.contentType,
      topic: target.topic,
      youtubeUrl: target.youtubeUrl ?? undefined,
      description: target.description,
      // 기존 이미지가 있으면 재첨부 없이도 통과해야 한다.
      hasExistingFile: target.imageUrl !== null,
    });
  }

  function handleDelete(target: ContentBoardItem) {
    startTransition(async () => {
      const { success, message } = await deleteContentBoardAction(shopId, target.id);
      if (success) {
        toast.success(SHOP_MESSAGE.CONTENT_BOARD_DELETE_SUCCESS);
        if (editingId === target.id) resetForm();
        reload();
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{SHOP_BASIC_COPY.CONTENT_BOARD_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_BASIC_COPY.CONTENT_BOARD_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-4 overflow-y-auto px-4">
          {items.length > 0 ? (
            <ul className="space-y-2">
              {items.map((item) => (
                <li key={item.id} className="flex items-start gap-3 rounded-md border px-3 py-2">
                  <div className="flex min-w-0 flex-1 flex-col gap-1">
                    <div className="flex flex-wrap items-center gap-1">
                      <Badge variant="outline">{CONTENT_BOARD_TYPE_LABEL[item.contentType]}</Badge>
                      <Badge variant="secondary">{CONTENT_BOARD_TOPIC_LABEL[item.topic]}</Badge>
                      {item.hidden && <Badge variant="destructive">노출중지</Badge>}
                    </div>
                    <span className="truncate text-sm">{item.description || item.youtubeUrl || "-"}</span>
                  </div>
                  <Button type="button" size="sm" variant="ghost" disabled={isPending} onClick={() => handleEdit(item)}>
                    수정
                  </Button>
                  <Button
                    type="button"
                    size="sm"
                    variant="ghost"
                    className="text-destructive"
                    disabled={isPending}
                    onClick={() => handleDelete(item)}
                  >
                    삭제
                  </Button>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-muted-foreground text-sm">등록된 콘텐츠보드가 없습니다.</p>
          )}

          <Separator />

          <form id="content-board-form" noValidate onSubmit={form.handleSubmit(onSubmit)}>
            <FieldGroup className="gap-4">
              <Controller
                control={form.control}
                name="contentType"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="content-board-type">형태</FieldLabel>
                    <Select
                      value={field.value ?? ""}
                      onValueChange={(value) => {
                        field.onChange(value as ContentBoardTypeOption);
                        // 형태를 바꾸면 이전 형태에서 채운 첨부/URL 을 비운다.
                        setAttachedFile(null);
                        form.setValue("hasExistingFile", false);
                        form.setValue("youtubeUrl", undefined);
                      }}
                      disabled={isBusy || isMaxReached}
                    >
                      <SelectTrigger id="content-board-type" className="w-full">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectGroup>
                          {CONTENT_BOARD_TYPE_OPTIONS.map((option) => (
                            <SelectItem key={option} value={option}>
                              {CONTENT_BOARD_TYPE_LABEL[option]}
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
                name="topic"
                render={({ field }) => (
                  <Field className="gap-1.5">
                    <FieldLabel htmlFor="content-board-topic">주제</FieldLabel>
                    <Select
                      value={field.value ?? ""}
                      onValueChange={(value) => field.onChange(value as ContentBoardTopicOption)}
                      disabled={isBusy || isMaxReached}
                    >
                      <SelectTrigger id="content-board-topic" className="w-full">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectGroup>
                          {CONTENT_BOARD_TOPIC_OPTIONS.map((option) => (
                            <SelectItem key={option} value={option}>
                              {CONTENT_BOARD_TOPIC_LABEL[option]}
                            </SelectItem>
                          ))}
                        </SelectGroup>
                      </SelectContent>
                    </Select>
                  </Field>
                )}
              />

              {isVideo ? (
                <Controller
                  control={form.control}
                  name="youtubeUrl"
                  render={({ field, fieldState }) => (
                    <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor="content-board-youtube-url">YouTube 주소</FieldLabel>
                      <Input
                        id="content-board-youtube-url"
                        value={field.value ?? ""}
                        onChange={field.onChange}
                        onBlur={field.onBlur}
                        placeholder="https://www.youtube.com/watch?v=..."
                        disabled={isBusy || isMaxReached}
                        aria-invalid={fieldState.invalid}
                      />
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                  )}
                />
              ) : (
                <Field className="gap-1.5">
                  <FieldLabel>{contentType === "GIF" ? "GIF 첨부" : "이미지 첨부"}</FieldLabel>
                  <input
                    ref={inputRef}
                    type="file"
                    accept={contentType === "GIF" ? "image/gif" : "image/jpeg,image/png"}
                    className="hidden"
                    onChange={handleFileChange}
                  />
                  <div className="flex items-center gap-2">
                    <Button
                      type="button"
                      size="sm"
                      variant="outline"
                      disabled={isBusy || isMaxReached}
                      onClick={() => inputRef.current?.click()}
                    >
                      {isValidating ? "업로드 중..." : "파일 선택"}
                    </Button>
                    {attachedFile && (
                      <span className="truncate text-muted-foreground text-xs">{attachedFile.name}</span>
                    )}
                  </div>
                </Field>
              )}

              <Controller
                control={form.control}
                name="description"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="content-board-description">설명</FieldLabel>
                    <Textarea
                      {...field}
                      id="content-board-description"
                      placeholder="콘텐츠에 대한 설명을 입력하세요"
                      maxLength={CONTENT_BOARD_DESCRIPTION_MAX}
                      rows={3}
                      disabled={isBusy || isMaxReached}
                      aria-invalid={fieldState.invalid}
                    />
                    <span className="text-muted-foreground text-xs">
                      {field.value.length} / {CONTENT_BOARD_DESCRIPTION_MAX}
                    </span>
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              {isMaxReached && <p className="text-destructive text-sm">{SHOP_MESSAGE.CONTENT_BOARD_MAX_REACHED}</p>}
            </FieldGroup>
          </form>
        </div>

        <SheetFooter>
          <Button type="submit" form="content-board-form" disabled={isBusy || isMaxReached}>
            {isPending ? "처리 중..." : editingId === null ? "적용" : "수정"}
          </Button>
          {editingId !== null && (
            <Button type="button" variant="outline" disabled={isBusy} onClick={resetForm}>
              수정 취소
            </Button>
          )}
          <SheetClose asChild>
            <Button variant="outline" disabled={isBusy}>
              닫기
            </Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
