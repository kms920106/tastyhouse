'use server'

import { menuReviewRepository } from '@/domains/menu-review/menu-review.repository'
import { PAGE_PATHS } from '@/lib/paths'
import { revalidatePath } from 'next/cache'

/**
 * 메뉴 평가가 반영돼야 하는 화면을 함께 무효화한다.
 *
 * `/orders`만 무효화하면 단독 평가 화면(`/orders/{id}/menu-reviews`)이 옛 서버 데이터를 그대로
 * 렌더해 `menuReviewId`가 null인 채로 남고, 다시 저장하면 `MENU_REVIEW_ALREADY_EXISTS`로 막힌다.
 */
function revalidateMenuReviewPaths(orderId: number) {
  revalidatePath(PAGE_PATHS.ORDERS)
  revalidatePath(PAGE_PATHS.ORDER_MENU_REVIEWS(orderId))
}

export async function createMenuReview({
  orderId,
  orderProductId,
  rating,
  comment,
}: {
  orderId: number
  orderProductId: number
  rating: number
  comment?: string
}) {
  const result = await menuReviewRepository.createMenuReview({ orderProductId, rating, comment })

  if (!result.error) {
    revalidateMenuReviewPaths(orderId)
  }

  return result
}

export async function updateMenuReview(
  menuReviewId: number,
  { orderId, rating, comment }: { orderId: number; rating: number; comment?: string },
) {
  const result = await menuReviewRepository.updateMenuReview(menuReviewId, { rating, comment })

  if (!result.error) {
    revalidateMenuReviewPaths(orderId)
  }

  return result
}
