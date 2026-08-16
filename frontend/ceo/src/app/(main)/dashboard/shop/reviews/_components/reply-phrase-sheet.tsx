"use client";

import * as React from "react";

import { useRouter } from "next/navigation";

import { zodResolver } from "@hookform/resolvers/zod";
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
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { Sheet, SheetContent, SheetDescription, SheetFooter, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { Textarea } from "@/components/ui/textarea";
import {
  createReplyPhraseAction,
  deleteReplyPhraseAction,
  fetchReplyPhrasesAction,
  updateReplyPhraseAction,
} from "@/feature/ceo-reply-phrase/actions";
import {
  PHRASE_CONTENT_MAX_LENGTH,
  PHRASE_MAX_COUNT,
  PHRASE_NAME_MAX_LENGTH,
} from "@/feature/ceo-reply-phrase/constants";
import type { CeoReplyPhrase } from "@/feature/ceo-reply-phrase/domain";
import { CEO_REPLY_PHRASE_COPY } from "@/feature/ceo-reply-phrase/message";
import { type ReplyPhraseFormValues, replyPhraseSchema } from "@/feature/ceo-reply-phrase/schema";

const EMPTY_PHRASE: ReplyPhraseFormValues = { name: "", content: "" };

interface ReplyPhraseSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  phrases: CeoReplyPhrase[];
}

/**
 * 자주 쓰는 문구 관리 시트.
 *
 * 목록 + 인라인 생성/수정 폼 + `AlertDialog` 삭제가 한 시트에 들어간다(`notice-sheet` 와 같은 형태).
 * 시트가 열려 있는 동안에는 로컬 목록을 들고 있다가 변경 후 전량 재조회한다 —
 * revalidate 로 새 prop 이 내려와 입력 중이던 값이 날아가지 않게 하기 위함이다.
 */
export function ReplyPhraseSheet({ open, onOpenChange, phrases }: ReplyPhraseSheetProps) {
  const router = useRouter();
  const [items, setItems] = React.useState<CeoReplyPhrase[]>(phrases);
  const [isPending, startTransition] = React.useTransition();
  const [editingId, setEditingId] = React.useState<number | null>(null);
  const [isFormOpen, setIsFormOpen] = React.useState(false);

  const form = useForm<ReplyPhraseFormValues>({
    resolver: zodResolver(replyPhraseSchema),
    defaultValues: EMPTY_PHRASE,
  });

  const resetForm = React.useCallback(() => {
    form.reset(EMPTY_PHRASE);
    setEditingId(null);
    setIsFormOpen(false);
  }, [form]);

  // 시트가 **열리는 순간에만** 서버 목록으로 되돌리고 폼을 비운다.
  const wasOpen = React.useRef(false);
  React.useEffect(() => {
    if (open && !wasOpen.current) {
      setItems(phrases);
      resetForm();
    }
    wasOpen.current = open;
  }, [open, phrases, resetForm]);

  const isLimitReached = items.length >= PHRASE_MAX_COUNT;
  const isEditing = editingId !== null;

  /**
   * 변경 후 목록을 다시 맞춘다.
   *
   * 시트의 로컬 `items` 만 갱신하면 답변 폼에 내려간 `phrases` prop 이 옛 목록으로 남는다
   * — 문구를 추가하고 시트를 닫아도 선택 드롭다운에 새 문구가 없는 상태가 된다.
   * 그래서 `router.refresh()` 로 서버 렌더를 함께 갱신해 두 경로가 같은 값을 보게 한다.
   */
  const reload = React.useCallback(() => {
    startTransition(async () => {
      const { success, data, message } = await fetchReplyPhrasesAction();
      if (success && data) setItems(data);
      else toast.error(message ?? CEO_REPLY_PHRASE_COPY.LOAD_FAILED);
      router.refresh();
    });
  }, [router]);

  const onSubmit = (values: ReplyPhraseFormValues) => {
    startTransition(async () => {
      const { success, message } = isEditing
        ? await updateReplyPhraseAction(editingId, values)
        : await createReplyPhraseAction(values);

      if (success) {
        toast.success(isEditing ? CEO_REPLY_PHRASE_COPY.UPDATE_SUCCESS : CEO_REPLY_PHRASE_COPY.CREATE_SUCCESS);
        resetForm();
        reload();
      } else {
        const failureMessage =
          message ?? (isEditing ? CEO_REPLY_PHRASE_COPY.UPDATE_FAILED : CEO_REPLY_PHRASE_COPY.CREATE_FAILED);
        toast.error(failureMessage);
        // 서버 사이드 검증 실패(금칙어 등)는 폼이 그대로 남아 있을 때 토스트만으로는
        // 눈에 띄지 않을 수 있어, 폼 자체에도 눈에 보이는 에러를 함께 남긴다 —
        // 이 setError 가 이 컴포넌트의 리렌더를 보장해 사용자가 실패를 놓치지 않게 한다.
        form.setError("content", { type: "server", message: failureMessage });
      }
    });
  };

  function handleEdit(target: CeoReplyPhrase) {
    setEditingId(target.id);
    setIsFormOpen(true);
    form.reset({ name: target.name ?? "", content: target.content });
  }

  function handleDelete(target: CeoReplyPhrase) {
    startTransition(async () => {
      const { success, message } = await deleteReplyPhraseAction(target.id);
      if (success) {
        toast.success(CEO_REPLY_PHRASE_COPY.DELETE_SUCCESS);
        if (editingId === target.id) resetForm();
        reload();
      } else {
        toast.error(message ?? CEO_REPLY_PHRASE_COPY.DELETE_FAILED);
      }
    });
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{CEO_REPLY_PHRASE_COPY.TITLE}</SheetTitle>
          <SheetDescription>{CEO_REPLY_PHRASE_COPY.DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-4 overflow-y-auto px-4">
          {items.length === 0 ? (
            <p className="rounded-md border border-dashed p-4 text-center text-muted-foreground text-sm">
              {CEO_REPLY_PHRASE_COPY.EMPTY}
            </p>
          ) : (
            <ul className="flex flex-col gap-2">
              {items.map((item) => (
                <li key={item.id} className="flex flex-col gap-2 rounded-md border p-3">
                  <span className="font-medium text-sm">{item.displayName}</span>
                  {/* 점주가 자유 입력한 내용이라 줄바꿈을 살린다. */}
                  <p className="line-clamp-2 whitespace-pre-line text-muted-foreground text-xs">{item.content}</p>
                  <div className="flex justify-end gap-1">
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      disabled={isPending}
                      onClick={() => handleEdit(item)}
                    >
                      {CEO_REPLY_PHRASE_COPY.EDIT}
                    </Button>
                    <AlertDialog>
                      <AlertDialogTrigger asChild>
                        <Button type="button" variant="destructive" size="sm" disabled={isPending}>
                          {CEO_REPLY_PHRASE_COPY.DELETE}
                        </Button>
                      </AlertDialogTrigger>
                      <AlertDialogContent>
                        <AlertDialogHeader>
                          <AlertDialogTitle>{CEO_REPLY_PHRASE_COPY.DELETE_CONFIRM_TITLE}</AlertDialogTitle>
                          <AlertDialogDescription>
                            {CEO_REPLY_PHRASE_COPY.DELETE_CONFIRM_DESCRIPTION}
                          </AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter>
                          <AlertDialogCancel>{CEO_REPLY_PHRASE_COPY.DELETE_CONFIRM_DISMISS}</AlertDialogCancel>
                          <AlertDialogAction onClick={() => handleDelete(item)}>
                            {CEO_REPLY_PHRASE_COPY.DELETE_CONFIRM_ACTION}
                          </AlertDialogAction>
                        </AlertDialogFooter>
                      </AlertDialogContent>
                    </AlertDialog>
                  </div>
                </li>
              ))}
            </ul>
          )}

          <Separator />

          {isFormOpen ? (
            <form
              id="reply-phrase-form"
              className="flex flex-col gap-3"
              noValidate
              onSubmit={form.handleSubmit(onSubmit)}
            >
              <Controller
                control={form.control}
                name="name"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="reply-phrase-name">{CEO_REPLY_PHRASE_COPY.NAME_LABEL}</FieldLabel>
                    <Input
                      {...field}
                      value={field.value ?? ""}
                      id="reply-phrase-name"
                      placeholder={CEO_REPLY_PHRASE_COPY.NAME_PLACEHOLDER}
                      maxLength={PHRASE_NAME_MAX_LENGTH}
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
                    <FieldLabel htmlFor="reply-phrase-content">{CEO_REPLY_PHRASE_COPY.CONTENT_LABEL}</FieldLabel>
                    <Textarea
                      {...field}
                      id="reply-phrase-content"
                      placeholder={CEO_REPLY_PHRASE_COPY.CONTENT_PLACEHOLDER}
                      maxLength={PHRASE_CONTENT_MAX_LENGTH}
                      aria-invalid={fieldState.invalid}
                      disabled={isPending}
                      rows={5}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Button
                type="button"
                variant="outline"
                size="sm"
                className="self-end"
                disabled={isPending}
                onClick={resetForm}
              >
                {CEO_REPLY_PHRASE_COPY.CANCEL}
              </Button>
            </form>
          ) : (
            <div className="flex flex-col gap-2">
              {/* 서버도 409 로 막지만 눌러보고 실패하게 두지 않는다. */}
              {isLimitReached && <p className="text-muted-foreground text-xs">{CEO_REPLY_PHRASE_COPY.LIMIT_REACHED}</p>}
              <Button
                type="button"
                variant="outline"
                disabled={isPending || isLimitReached}
                onClick={() => setIsFormOpen(true)}
              >
                {CEO_REPLY_PHRASE_COPY.ADD}
              </Button>
            </div>
          )}
        </div>

        {isFormOpen && (
          <SheetFooter>
            <Button type="submit" form="reply-phrase-form" disabled={isPending}>
              {isEditing ? CEO_REPLY_PHRASE_COPY.SUBMIT_UPDATE : CEO_REPLY_PHRASE_COPY.SUBMIT_CREATE}
            </Button>
          </SheetFooter>
        )}
      </SheetContent>
    </Sheet>
  );
}
