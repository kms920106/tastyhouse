"use server";

import { revalidatePath } from "next/cache";

import { reviewRepository } from "@/api/review/review.repository";
import { reviewService } from "@/api/review/review.service";
import type { ReviewComment, ReviewDetail } from "@/feature/review/domain";

import { REVIEW_MESSAGE } from "./message";
import { type ReviewHiddenFormValues, reviewHiddenSchema } from "./schema";

const REVIEWS_PATH = "/dashboard/reviews";

type ActionResult = {
  success: boolean;
  message?: string;
};

type ReviewDetailResult = {
  success: boolean;
  message?: string;
  data?: ReviewDetail;
};

type ReviewCommentsResult = {
  success: boolean;
  message?: string;
  data?: ReviewComment[];
};

function parseHidden(values: ReviewHiddenFormValues) {
  const parsed = reviewHiddenSchema.safeParse(values);
  if (!parsed.success) {
    return {
      ok: false as const,
      message: parsed.error.issues[0]?.message ?? REVIEW_MESSAGE.INVALID_INPUT,
    };
  }
  return { ok: true as const, data: parsed.data };
}

// 리뷰 상세 조회
export async function fetchReviewAction(id: number): Promise<ReviewDetailResult> {
  const { error, data } = await reviewService.getReview(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 리뷰 숨김/노출 전환
export async function setReviewHiddenAction(id: number, hidden: boolean): Promise<ActionResult> {
  const parsed = parseHidden({ hidden });
  if (!parsed.ok) {
    return { success: false, message: parsed.message };
  }

  const { error } = await reviewRepository.updateHidden(id, parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(REVIEWS_PATH);
  return { success: true };
}

// 리뷰 삭제
export async function deleteReviewAction(id: number): Promise<ActionResult> {
  const { error } = await reviewRepository.remove(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(REVIEWS_PATH);
  return { success: true };
}

// 리뷰 댓글/답글 조회
export async function fetchReviewCommentsAction(id: number): Promise<ReviewCommentsResult> {
  const { error, data } = await reviewService.getComments(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 댓글 숨김/노출 전환 (시트 내부에서 재조회하므로 revalidatePath 불필요)
export async function setCommentHiddenAction(commentId: number, hidden: boolean): Promise<ActionResult> {
  const parsed = parseHidden({ hidden });
  if (!parsed.ok) {
    return { success: false, message: parsed.message };
  }

  const { error } = await reviewRepository.updateCommentHidden(commentId, parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  return { success: true };
}

// 댓글 삭제
export async function deleteCommentAction(commentId: number): Promise<ActionResult> {
  const { error } = await reviewRepository.removeComment(commentId);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  return { success: true };
}

// 답글 숨김/노출 전환
export async function setReplyHiddenAction(replyId: number, hidden: boolean): Promise<ActionResult> {
  const parsed = parseHidden({ hidden });
  if (!parsed.ok) {
    return { success: false, message: parsed.message };
  }

  const { error } = await reviewRepository.updateReplyHidden(replyId, parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  return { success: true };
}

// 답글 삭제
export async function deleteReplyAction(replyId: number): Promise<ActionResult> {
  const { error } = await reviewRepository.removeReply(replyId);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  return { success: true };
}
