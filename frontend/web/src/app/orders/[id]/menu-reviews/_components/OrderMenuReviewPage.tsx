import MenuReviewSection from '@/components/menu-reviews/MenuReviewSection'
import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '@/components/layouts/Header'
import { BackButton } from '@/components/layouts/header-parts'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { MENU_REVIEW_COPY } from '@/domains/menu-review'
import { menuReviewRepository } from '@/domains/menu-review/menu-review.repository'
import { PAGE_PATHS } from '@/lib/paths'
import { redirect } from 'next/navigation'

interface Props {
  orderId: number
}

/**
 * 메뉴 평가 단독 화면.
 *
 * 이 경로가 설계 원칙 1을 화면에서 실현하는 지점이다 —
 * 매장 리뷰를 쓰지 않고도 메뉴 평가만 남길 수 있어야 한다.
 */
export default async function OrderMenuReviewPage({ orderId }: Props) {
  const { error, status, data } = await menuReviewRepository.getWritableMenuReviews(orderId)

  if (error && status === 401) {
    redirect(PAGE_PATHS.AUTH_LOGIN)
  }

  const header = (
    <Header variant="white" height={55}>
      <HeaderLeft>
        <BackButton />
      </HeaderLeft>
      <HeaderCenter>
        <HeaderTitle>{MENU_REVIEW_COPY.SECTION_TITLE}</HeaderTitle>
      </HeaderCenter>
    </Header>
  )

  if ((error && status === 404) || !data) {
    return (
      <>
        {header}
        <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('메뉴 평가')} />
      </>
    )
  }

  if (error) {
    return (
      <>
        {header}
        <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
      </>
    )
  }

  if (data.length === 0) {
    return (
      <>
        {header}
        <div className="w-full py-20 text-sm leading-relaxed text-[#999999] text-center">
          {MENU_REVIEW_COPY.LIST_EMPTY}
        </div>
      </>
    )
  }

  return (
    <>
      {header}
      <MenuReviewSection orderId={orderId} items={data} />
    </>
  )
}
