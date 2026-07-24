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
import { Textarea } from "@/components/ui/textarea";
import {
  createEditorChoiceAction,
  deleteEditorChoiceAction,
  fetchEditorChoicesAction,
  updateEditorChoiceAction,
} from "@/feature/shop/actions";
import { EDITOR_CHOICE_CONTENT_MAX, EDITOR_CHOICE_TITLE_MAX } from "@/feature/shop/constants";
import type { EditorChoice } from "@/feature/shop/domain";
import { SHOP_MESSAGE } from "@/feature/shop/message";
import { type EditorChoiceFormValues, editorChoiceSchema } from "@/feature/shop/schema";

interface EditorChoicesSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const EMPTY_VALUES: EditorChoiceFormValues = {
  shopId: undefined as unknown as number,
  title: "",
  content: "",
};

export function EditorChoicesSheet({ open, onOpenChange }: EditorChoicesSheetProps) {
  const [shopIdInput, setShopIdInput] = React.useState("");
  const [choices, setChoices] = React.useState<EditorChoice[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [loadedShopId, setLoadedShopId] = React.useState<number | null>(null);
  const [isPending, startTransition] = React.useTransition();
  const [editingId, setEditingId] = React.useState<number | null>(null);

  const form = useForm<EditorChoiceFormValues>({
    resolver: zodResolver(editorChoiceSchema),
    defaultValues: EMPTY_VALUES,
  });

  React.useEffect(() => {
    if (!open) return;
    setShopIdInput("");
    setChoices([]);
    setError(null);
    setLoadedShopId(null);
    setEditingId(null);
    form.reset(EMPTY_VALUES);
  }, [open, form.reset]);

  const loadChoices = React.useCallback((shopId: number) => {
    setIsLoading(true);
    setError(null);
    void fetchEditorChoicesAction(shopId).then((result) => {
      setIsLoading(false);
      if (result.success && result.data) {
        setChoices(result.data);
        setLoadedShopId(shopId);
      } else {
        setChoices([]);
        setError(result.message ?? SHOP_MESSAGE.EDITOR_CHOICES_LOAD_FAILED);
      }
    });
  }, []);

  function handleLoad() {
    const parsed = Number(shopIdInput);
    if (!Number.isInteger(parsed) || parsed <= 0) {
      toast.error("가게 ID는 양수여야 합니다.");
      return;
    }
    form.setValue("shopId", parsed);
    setEditingId(null);
    loadChoices(parsed);
  }

  function startEdit(choice: EditorChoice) {
    setEditingId(choice.id);
    form.reset({ shopId: choice.shopId, title: choice.title, content: choice.content });
  }

  function cancelEdit() {
    setEditingId(null);
    form.reset({ shopId: loadedShopId ?? (undefined as unknown as number), title: "", content: "" });
  }

  const onSubmit = (values: EditorChoiceFormValues) => {
    startTransition(async () => {
      const { success, message } = editingId
        ? await updateEditorChoiceAction(editingId, { title: values.title, content: values.content })
        : await createEditorChoiceAction(values);

      if (success) {
        toast.success(
          editingId ? SHOP_MESSAGE.EDITOR_CHOICE_UPDATE_SUCCESS : SHOP_MESSAGE.EDITOR_CHOICE_CREATE_SUCCESS,
        );
        cancelEdit();
        loadChoices(values.shopId);
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  function handleDelete(choiceId: number) {
    startTransition(async () => {
      const { success, message } = await deleteEditorChoiceAction(choiceId);
      if (success) {
        toast.success(SHOP_MESSAGE.EDITOR_CHOICE_DELETE_SUCCESS);
        if (loadedShopId != null) loadChoices(loadedShopId);
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>테하 초이스 관리</SheetTitle>
          <SheetDescription>가게 ID로 테하 초이스를 조회하고 등록·수정·삭제합니다.</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-6 overflow-y-auto px-4">
          <div className="flex items-end gap-2">
            <Field className="flex-1 gap-1.5">
              <FieldLabel htmlFor="editor-choice-shop-id">가게 ID</FieldLabel>
              <Input
                id="editor-choice-shop-id"
                type="number"
                min={1}
                placeholder="가게 ID"
                value={shopIdInput}
                onChange={(e) => setShopIdInput(e.target.value)}
                disabled={isPending}
              />
            </Field>
            <Button type="button" size="sm" onClick={handleLoad} disabled={isLoading || isPending}>
              조회
            </Button>
          </div>

          <div className="space-y-2">
            <h4 className="font-medium text-sm">테하 초이스 목록</h4>
            {error ? (
              <p className="text-destructive text-sm">{error}</p>
            ) : isLoading ? (
              <div className="space-y-2">
                <Skeleton className="h-16 w-full" />
                <Skeleton className="h-16 w-full" />
              </div>
            ) : loadedShopId == null ? (
              <p className="text-muted-foreground text-sm">가게 ID를 입력하고 조회하세요.</p>
            ) : choices.length ? (
              <ul className="space-y-2">
                {choices.map((choice) => (
                  <li key={choice.id} className="space-y-1 rounded-md border px-3 py-2 text-sm">
                    <div className="flex items-center justify-between gap-2">
                      <span className="font-medium">{choice.title}</span>
                      <div className="flex gap-1">
                        <Button type="button" size="sm" variant="outline" onClick={() => startEdit(choice)}>
                          수정
                        </Button>
                        <Button
                          type="button"
                          size="sm"
                          variant="ghost"
                          className="text-destructive"
                          disabled={isPending}
                          onClick={() => handleDelete(choice.id)}
                        >
                          삭제
                        </Button>
                      </div>
                    </div>
                    <p className="line-clamp-2 text-muted-foreground text-xs">{choice.content}</p>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-muted-foreground text-sm">등록된 테하 초이스가 없습니다.</p>
            )}
          </div>

          <Separator />

          <div className="space-y-3">
            <h4 className="font-medium text-sm">{editingId ? "테하 초이스 수정" : "테하 초이스 추가"}</h4>
            <form id="editor-choice-form" noValidate onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
              <Controller
                control={form.control}
                name="title"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="editor-choice-title">제목</FieldLabel>
                    <Input
                      {...field}
                      id="editor-choice-title"
                      placeholder="예: 이번 주 추천 맛집"
                      maxLength={EDITOR_CHOICE_TITLE_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={isPending}
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
                    <FieldLabel htmlFor="editor-choice-content">내용</FieldLabel>
                    <Textarea
                      {...field}
                      id="editor-choice-content"
                      placeholder="상세 설명 내용을 입력하세요"
                      maxLength={EDITOR_CHOICE_CONTENT_MAX}
                      rows={4}
                      aria-invalid={fieldState.invalid}
                      disabled={isPending}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />
              <p className="text-muted-foreground text-xs">위에서 조회한 가게 ID로 등록됩니다.</p>
              <div className="flex gap-2">
                <Button type="submit" size="sm" disabled={isPending || loadedShopId == null}>
                  {isPending ? "저장 중..." : editingId ? "수정" : "테하 초이스 추가"}
                </Button>
                {editingId ? (
                  <Button type="button" size="sm" variant="outline" onClick={cancelEdit} disabled={isPending}>
                    취소
                  </Button>
                ) : null}
              </div>
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
