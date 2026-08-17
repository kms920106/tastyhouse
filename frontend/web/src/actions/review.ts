'use server'

import { reviewRepository } from '@/domains/review/review.repository'
import { revalidatePath } from 'next/cache'

export async function getLatestReviews({
  page,
  size,
  type,
}: {
  page: number
  size: number
  type: 'ALL' | 'FOLLOWING'
}) {
  return reviewRepository.getLatestReviews({ page, size, type })
}

export async function getMemberReviews(
  memberId: number | string,
  page: number = 0,
  size: number = 9,
) {
  return reviewRepository.getMemberReviews(memberId, { page, size })
}

export async function toggleReviewLike(reviewId: number) {
  return reviewRepository.toggleReviewLike(reviewId)
}

export async function createComment(reviewId: number, { content }: { content: string }) {
  const result = await reviewRepository.createReviewComment(reviewId, { content })

  if (!result.error && result.data) {
    revalidatePath(`/reviews/${reviewId}`)
  }

  return result
}

export async function createReply(
  reviewId: number,
  commentId: number,
  { content, replyToMemberId }: { content: string; replyToMemberId: number },
) {
  const result = await reviewRepository.createReviewReply(reviewId, commentId, {
    content,
    replyToMemberId,
  })

  if (!result.error && result.data) {
    revalidatePath(`/reviews/${reviewId}`)
  }

  return result
}

export async function getReviewWriteInfo(orderProductId: number) {
  return reviewRepository.getReviewWriteInfo(orderProductId)
}

export async function createOrderReview({
  orderProductId,
  productId,
  tasteRating,
  amountRating,
  priceRating,
  content,
  uploadedFileIds,
  tags,
  ownerOnly,
  deliveryRating,
  deliveryComment,
}: {
  orderProductId: number | null
  productId: number
  tasteRating: number
  amountRating: number
  priceRating: number
  content: string
  uploadedFileIds: number[]
  tags: string[]
  ownerOnly: boolean
  deliveryRating?: number
  deliveryComment?: string
}) {
  const result = await reviewRepository.createReview({
    orderProductId,
    productId,
    tasteRating,
    amountRating,
    priceRating,
    content,
    uploadedFileIds,
    tags,
    ownerOnly,
    deliveryRating,
    deliveryComment,
  })

  if (!result.error && result.data) {
    revalidatePath('/orders')
  }

  return result
}

export async function updateReview(
  reviewId: number,
  {
    tasteRating,
    amountRating,
    priceRating,
    content,
    uploadedFileIds,
    tags,
    deliveryRating,
    deliveryComment,
  }: {
    tasteRating: number
    amountRating: number
    priceRating: number
    content: string
    uploadedFileIds: number[]
    tags: string[]
    deliveryRating?: number | null
    deliveryComment?: string | null
  },
) {
  const result = await reviewRepository.updateReview(reviewId, {
    tasteRating,
    amountRating,
    priceRating,
    content,
    uploadedFileIds,
    tags,
    deliveryRating,
    deliveryComment,
  })

  if (!result.error && result.data) {
    revalidatePath(`/reviews/${reviewId}`)
  }

  return result
}

/**
 * 게시중단 리뷰 삭제 동의.
 *
 * 성공하면 리뷰가 삭제되므로 상세로 돌아갈 수 없다 — 호출부는 `/reviews`로 이동시킨다.
 * 응답 본문이 `Void`라 `data`로 성공을 판정할 수 없으므로 `error` 부재로 판정한다.
 */
export async function consentReviewBlindDeleteAction(reviewId: number) {
  const result = await reviewRepository.consentReviewBlindDelete(reviewId)

  if (!result.error) {
    revalidatePath('/reviews')
    revalidatePath('/notifications')
  }

  return result
}

/**
 * 게시중단 리뷰 삭제 거부.
 *
 * 서버는 아무 상태 전이도 하지 않는다(30일 배치가 재노출을 처리).
 */
export async function rejectReviewBlindDeleteAction(reviewId: number) {
  const result = await reviewRepository.rejectReviewBlindDelete(reviewId)

  if (!result.error) {
    revalidatePath('/notifications')
  }

  return result
}
