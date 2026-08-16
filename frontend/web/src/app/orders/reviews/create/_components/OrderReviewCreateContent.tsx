import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
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

  return (
    <OrderReviewCreateForm
      orderProductId={orderProductId}
      productId={data.productId}
      productName={data.productName}
      productImageUrl={data.productImageUrl}
      productPrice={data.productPrice}
    />
  )
}
