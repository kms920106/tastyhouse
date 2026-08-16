import MenuReviewSection from '@/components/menu-reviews/MenuReviewSection'
import BorderedSection from '@/components/ui/BorderedSection'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { menuReviewRepository } from '@/domains/menu-review/menu-review.repository'
import { reviewRepository } from '@/domains/review/review.repository'
import { PAGE_PATHS } from '@/lib/paths'
import { redirect } from 'next/navigation'
import OrderReviewCreateForm from './OrderReviewCreateForm'

interface Props {
  orderProductId: number
}

export default async function OrderReviewCreateContent({ orderProductId }: Props) {
  const { error, status, data } = await reviewRepository.getReviewWriteInfo(orderProductId)

  if (error && status === 401) {
    redirect(PAGE_PATHS.AUTH_LOGIN)
  }

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('리뷰 작성')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  // 메뉴 평가는 매장 리뷰와 독립 리소스다. 조회가 실패해도 매장 리뷰 작성은 그대로 진행돼야 하므로
  // 빈 배열로 떨어뜨려 섹션만 사라지게 한다.
  const { data: writableMenus } = await menuReviewRepository.getWritableMenuReviews(data.orderId)

  return (
    <>
      <OrderReviewCreateForm
        orderProductId={orderProductId}
        productId={data.productId}
        productName={data.productName}
        productImageUrl={data.productImageUrl}
        productPrice={data.productPrice}
        orderMethod={data.orderMethod}
      />
      {/* 매장 리뷰 폼 바깥에 둔다 — 같은 폼이면 제출이 하나로 묶여 "하나만 저장"이 깨진다. */}
      <BorderedSection>
        <MenuReviewSection orderId={data.orderId} items={writableMenus ?? []} />
      </BorderedSection>
    </>
  )
}
