"use client";

import * as React from "react";

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
import { Field, FieldError } from "@/components/ui/field";
import { Textarea } from "@/components/ui/textarea";
import { createOwnerReplyAction, deleteOwnerReplyAction, updateOwnerReplyAction } from "@/feature/shop-review/actions";
import { OWNER_REPLY_MAX_LENGTH } from "@/feature/shop-review/constants";
import { SHOP_REVIEW_COPY } from "@/feature/shop-review/message";
import { type OwnerReplyFormValues, ownerReplySchema } from "@/feature/shop-review/schema";
import { formatDateTime } from "@/lib/date";

interface OwnerReplyFormProps {
  shopId: number;
  reviewId: number;
  /** 이미 등록된 답변. null 이면 등록 폼, 있으면 답변 카드 + 수정/삭제 */
  replyContent: string | null;
  replyCreatedAt: string | null;
  replyUpdatedAt?: string | null;
  /** 사장님만보기 리뷰면 답글도 고객에게 보이지 않으므로 안내를 노출한다 */
  ownerOnly?: boolean;
  disabled?: boolean;
}

/**
 * 사장님 답변 등록·수정·삭제.
 *
 * 리뷰당 답변은 1건이라 등록/수정을 한 컴포넌트가 다룬다 — 등록 여부는 `replyContent` 로
 * 판정하고, 수정은 같은 폼을 편집 모드로 전환해 재사용한다.
 */
export function OwnerReplyForm({
  shopId,
  reviewId,
  replyContent,
  replyCreatedAt,
  replyUpdatedAt,
  ownerOnly,
  disabled,
}: OwnerReplyFormProps) {
  const [isPending, startTransition] = React.useTransition();
  const [isEditing, setIsEditing] = React.useState(false);

  const form = useForm<OwnerReplyFormValues>({
    resolver: zodResolver(ownerReplySchema),
    defaultValues: { content: replyContent ?? "" },
  });

  const hasReply = replyContent !== null;
  const isBusy = isPending || disabled;

  // 서버 갱신으로 답변이 바뀌면 편집 상태를 닫고 새 값으로 폼을 다시 잡는다.
  React.useEffect(() => {
    form.reset({ content: replyContent ?? "" });
    setIsEditing(false);
  }, [replyContent, form.reset]);

  const onSubmit = (values: OwnerReplyFormValues) => {
    startTransition(async () => {
      const { success, message } = hasReply
        ? await updateOwnerReplyAction(shopId, reviewId, values)
        : await createOwnerReplyAction(shopId, reviewId, values);

      if (success) {
        toast.success(
          hasReply ? SHOP_REVIEW_COPY.OWNER_REPLY_UPDATE_SUCCESS : SHOP_REVIEW_COPY.OWNER_REPLY_CREATE_SUCCESS,
        );
        setIsEditing(false);
        // 등록 직후에는 revalidate 로 답변이 내려오지만, 그 사이 폼이 빈 채로 남지 않도록 값을 유지한다.
        form.reset(values);
      } else {
        toast.error(
          message ??
            (hasReply ? SHOP_REVIEW_COPY.OWNER_REPLY_UPDATE_FAILED : SHOP_REVIEW_COPY.OWNER_REPLY_CREATE_FAILED),
        );
      }
    });
  };

  function handleDelete() {
    startTransition(async () => {
      const { success, message } = await deleteOwnerReplyAction(shopId, reviewId);
      if (success) {
        toast.success(SHOP_REVIEW_COPY.OWNER_REPLY_DELETE_SUCCESS);
        form.reset({ content: "" });
        setIsEditing(false);
      } else {
        toast.error(message ?? SHOP_REVIEW_COPY.OWNER_REPLY_DELETE_FAILED);
      }
    });
  }

  function handleCancelEdit() {
    form.reset({ content: replyContent ?? "" });
    setIsEditing(false);
  }

  // 답변이 있고 편집 중이 아니면 읽기 카드를 보여준다.
  if (hasReply && !isEditing) {
    return (
      <section className="flex flex-col gap-2 rounded-md border bg-muted/40 p-3">
        <span className="font-medium text-sm">{SHOP_REVIEW_COPY.OWNER_REPLY_SECTION_TITLE}</span>
        {ownerOnly && <p className="text-muted-foreground text-xs">{SHOP_REVIEW_COPY.OWNER_REPLY_OWNER_ONLY_GUIDE}</p>}
        {/* 점주가 자유 입력한 답변이라 줄바꿈을 살린다. */}
        <p className="whitespace-pre-line text-sm">{replyContent}</p>
        <div className="flex flex-wrap items-center gap-3 text-muted-foreground text-xs">
          {replyCreatedAt && (
            <span>
              {SHOP_REVIEW_COPY.OWNER_REPLY_CREATED_AT} {formatDateTime(replyCreatedAt)}
            </span>
          )}
          {/* 수정 이력이 있을 때만 노출한다 — 등록 직후에는 created 와 같아 의미가 없다 */}
          {replyUpdatedAt && replyUpdatedAt !== replyCreatedAt && (
            <span>
              {SHOP_REVIEW_COPY.OWNER_REPLY_UPDATED_AT} {formatDateTime(replyUpdatedAt)}
            </span>
          )}
        </div>
        <div className="flex justify-end gap-2">
          <Button type="button" variant="outline" size="sm" disabled={isBusy} onClick={() => setIsEditing(true)}>
            {SHOP_REVIEW_COPY.OWNER_REPLY_EDIT}
          </Button>
          <AlertDialog>
            <AlertDialogTrigger asChild>
              <Button type="button" variant="destructive" size="sm" disabled={isBusy}>
                {SHOP_REVIEW_COPY.OWNER_REPLY_DELETE}
              </Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
              <AlertDialogHeader>
                <AlertDialogTitle>{SHOP_REVIEW_COPY.OWNER_REPLY_DELETE_CONFIRM_TITLE}</AlertDialogTitle>
                <AlertDialogDescription>
                  {SHOP_REVIEW_COPY.OWNER_REPLY_DELETE_CONFIRM_DESCRIPTION}
                </AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel>{SHOP_REVIEW_COPY.OWNER_REPLY_DELETE_CONFIRM_DISMISS}</AlertDialogCancel>
                <AlertDialogAction onClick={handleDelete}>
                  {SHOP_REVIEW_COPY.OWNER_REPLY_DELETE_CONFIRM_ACTION}
                </AlertDialogAction>
              </AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialog>
        </div>
      </section>
    );
  }

  return (
    <form className="flex flex-col gap-2" noValidate onSubmit={form.handleSubmit(onSubmit)}>
      <span className="font-medium text-sm">{SHOP_REVIEW_COPY.OWNER_REPLY_SECTION_TITLE}</span>
      {/* 답글 동작 자체는 그대로 두고 안내만 덧붙인다 — 비공개 리뷰도 등록·수정·삭제가 모두 허용된다 */}
      {ownerOnly && <p className="text-muted-foreground text-xs">{SHOP_REVIEW_COPY.OWNER_REPLY_OWNER_ONLY_GUIDE}</p>}
      {!hasReply && <p className="text-muted-foreground text-xs">{SHOP_REVIEW_COPY.OWNER_REPLY_EMPTY}</p>}

      <Controller
        control={form.control}
        name="content"
        render={({ field, fieldState }) => (
          <Field className="gap-1.5" data-invalid={fieldState.invalid}>
            <Textarea
              {...field}
              id={`owner-reply-content-${reviewId}`}
              placeholder={SHOP_REVIEW_COPY.OWNER_REPLY_PLACEHOLDER}
              maxLength={OWNER_REPLY_MAX_LENGTH}
              aria-label={SHOP_REVIEW_COPY.OWNER_REPLY_SECTION_TITLE}
              aria-invalid={fieldState.invalid}
              disabled={isBusy}
              rows={3}
            />
            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
          </Field>
        )}
      />

      <div className="flex justify-end gap-2">
        {isEditing && (
          <Button type="button" variant="outline" size="sm" disabled={isBusy} onClick={handleCancelEdit}>
            {SHOP_REVIEW_COPY.OWNER_REPLY_EDIT_CANCEL}
          </Button>
        )}
        <Button type="submit" size="sm" disabled={isBusy}>
          {isEditing ? SHOP_REVIEW_COPY.OWNER_REPLY_EDIT_SUBMIT : SHOP_REVIEW_COPY.OWNER_REPLY_SUBMIT}
        </Button>
      </div>
    </form>
  );
}
