"use client";

import * as React from "react";

import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
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
import {
  deleteCommentAction,
  deleteReplyAction,
  fetchReviewCommentsAction,
  setCommentHiddenAction,
  setReplyHiddenAction,
} from "@/feature/review/actions";
import type { ReviewComment, ReviewListItem, ReviewReply } from "@/feature/review/domain";
import { REVIEW_MESSAGE } from "@/feature/review/message";
import { formatDateTime } from "@/lib/date";

interface ReviewCommentsSheetProps {
  /** 댓글 관리 대상 리뷰. null 이면 닫힌 상태. */
  review: Pick<ReviewListItem, "id" | "memberNickname"> | null;
  onOpenChange: (open: boolean) => void;
}

export function ReviewCommentsSheet({ review, onOpenChange }: ReviewCommentsSheetProps) {
  const [comments, setComments] = React.useState<ReviewComment[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [pendingId, setPendingId] = React.useState<number | null>(null);

  const reviewId = review?.id ?? null;

  const loadComments = React.useCallback(() => {
    if (reviewId == null) return;

    let active = true;
    setIsLoading(true);
    setError(null);

    void fetchReviewCommentsAction(reviewId).then((result) => {
      if (!active) return;
      if (result.success && result.data) {
        setComments(result.data);
      } else {
        setError(result.message ?? REVIEW_MESSAGE.COMMENTS_LOAD_FAILED);
      }
      setIsLoading(false);
    });

    return () => {
      active = false;
    };
  }, [reviewId]);

  React.useEffect(() => {
    if (reviewId == null) return;
    setComments([]);
    setError(null);
    return loadComments();
  }, [reviewId, loadComments]);

  async function handleToggleCommentHidden(comment: ReviewComment) {
    setPendingId(comment.id);
    const { success, message } = await setCommentHiddenAction(comment.id, !comment.hidden);
    setPendingId(null);
    if (success) {
      toast.success(comment.hidden ? REVIEW_MESSAGE.COMMENT_VISIBLE_SUCCESS : REVIEW_MESSAGE.COMMENT_HIDDEN_SUCCESS);
      loadComments();
    } else {
      toast.error(message ?? REVIEW_MESSAGE.COMMENT_HIDDEN_FAILED);
    }
  }

  async function handleDeleteComment(comment: ReviewComment) {
    setPendingId(comment.id);
    const { success, message } = await deleteCommentAction(comment.id);
    setPendingId(null);
    if (success) {
      toast.success(REVIEW_MESSAGE.COMMENT_DELETE_SUCCESS);
      loadComments();
    } else {
      toast.error(message ?? REVIEW_MESSAGE.COMMENT_DELETE_FAILED);
    }
  }

  async function handleToggleReplyHidden(reply: ReviewReply) {
    setPendingId(reply.id);
    const { success, message } = await setReplyHiddenAction(reply.id, !reply.hidden);
    setPendingId(null);
    if (success) {
      toast.success(reply.hidden ? REVIEW_MESSAGE.REPLY_VISIBLE_SUCCESS : REVIEW_MESSAGE.REPLY_HIDDEN_SUCCESS);
      loadComments();
    } else {
      toast.error(message ?? REVIEW_MESSAGE.REPLY_HIDDEN_FAILED);
    }
  }

  async function handleDeleteReply(reply: ReviewReply) {
    setPendingId(reply.id);
    const { success, message } = await deleteReplyAction(reply.id);
    setPendingId(null);
    if (success) {
      toast.success(REVIEW_MESSAGE.REPLY_DELETE_SUCCESS);
      loadComments();
    } else {
      toast.error(message ?? REVIEW_MESSAGE.REPLY_DELETE_FAILED);
    }
  }

  return (
    <Sheet open={review != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>댓글 관리</SheetTitle>
          <SheetDescription>
            {review ? `"${review.memberNickname}" 님 리뷰의 댓글·답글을 관리합니다.` : ""}
          </SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-4 overflow-y-auto px-4">
          {error ? (
            <p className="text-destructive text-sm">{error}</p>
          ) : isLoading ? (
            <div className="space-y-2">
              <Skeleton className="h-16 w-full" />
              <Skeleton className="h-16 w-full" />
            </div>
          ) : comments.length ? (
            <ul className="space-y-3">
              {comments.map((comment) => (
                <li key={comment.id} className="space-y-2 rounded-md border p-3">
                  <div className="flex items-start justify-between gap-2">
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2">
                        <p className="font-medium text-sm">{comment.memberNickname}</p>
                        <Badge variant={comment.hidden ? "destructive" : "default"} className="text-xs">
                          {comment.hidden ? "숨김" : "노출"}
                        </Badge>
                      </div>
                      <p className="mt-1 whitespace-pre-wrap break-words text-sm">{comment.content}</p>
                      <p className="mt-1 text-muted-foreground text-xs">{formatDateTime(comment.createdAt)}</p>
                    </div>
                  </div>
                  <div className="flex justify-end gap-2">
                    <Button
                      type="button"
                      size="sm"
                      variant="outline"
                      disabled={pendingId === comment.id}
                      onClick={() => handleToggleCommentHidden(comment)}
                    >
                      {comment.hidden ? "노출 전환" : "숨김 전환"}
                    </Button>
                    <Button
                      type="button"
                      size="sm"
                      variant="destructive"
                      disabled={pendingId === comment.id}
                      onClick={() => handleDeleteComment(comment)}
                    >
                      삭제
                    </Button>
                  </div>

                  {comment.replies.length ? (
                    <ul className="space-y-2 border-l pl-4">
                      {comment.replies.map((reply) => (
                        <li key={reply.id} className="space-y-2 rounded-md border p-3">
                          <div className="flex items-center gap-2">
                            <p className="font-medium text-sm">{reply.memberNickname}</p>
                            <span className="text-muted-foreground text-xs">→ {reply.replyToMemberNickname}</span>
                            <Badge variant={reply.hidden ? "destructive" : "default"} className="text-xs">
                              {reply.hidden ? "숨김" : "노출"}
                            </Badge>
                          </div>
                          <p className="whitespace-pre-wrap break-words text-sm">{reply.content}</p>
                          <p className="text-muted-foreground text-xs">{formatDateTime(reply.createdAt)}</p>
                          <div className="flex justify-end gap-2">
                            <Button
                              type="button"
                              size="sm"
                              variant="outline"
                              disabled={pendingId === reply.id}
                              onClick={() => handleToggleReplyHidden(reply)}
                            >
                              {reply.hidden ? "노출 전환" : "숨김 전환"}
                            </Button>
                            <Button
                              type="button"
                              size="sm"
                              variant="destructive"
                              disabled={pendingId === reply.id}
                              onClick={() => handleDeleteReply(reply)}
                            >
                              삭제
                            </Button>
                          </div>
                        </li>
                      ))}
                    </ul>
                  ) : null}
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-muted-foreground text-sm">등록된 댓글이 없습니다.</p>
          )}
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
