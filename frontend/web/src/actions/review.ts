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
}: {
  orderProductId: number | null
  productId: number
  tasteRating: number
  amountRating: number
  priceRating: number
  content: string
  uploadedFileIds: number[]
  tags: string[]
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
  })

  if (!result.error && result.data) {
    revalidatePath('/orders')
  }

  return result
}
