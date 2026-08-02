import FetchErrorState from '@/components/ui/FetchErrorState'
import { memberRepository } from '@/domains/member/member.repository'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import CouponListItem from './CouponListItem'

export default async function CouponList() {
  const { error, status, data } = await memberRepository.getMyCoupons()

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('할인쿠폰')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  return data.map((coupon) => <CouponListItem key={coupon.id} memberCoupon={coupon} />)
}
