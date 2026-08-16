import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { memberRepository } from '@/domains/member/member.repository'
import { reviewRepository } from '@/domains/review/review.repository'
import { getIsLoggedIn } from '@/lib/auth-config'
import ReviewEditForm from './ReviewEditForm'
import ReviewEditHeader from './ReviewEditHeader'

interface Props {
  reviewId: number
}

export default async function ReviewEditPage({ reviewId }: Props) {
  const [{ error, status, data }, isLoggedIn] = await Promise.all([
    reviewRepository.getReviewDetail(reviewId),
    getIsLoggedIn(),
  ])

  // 비공개(사장님만보기) 리뷰를 작성자 본인이 아닌 뷰어가 열람하면 서버가 404를 준다(backend.md §3-3).
  if ((error && status === 404) || !data) {
    return (
      <>
        <ReviewEditHeader />
        <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('리뷰')} />
      </>
    )
  }

  if (error) {
    return (
      <>
        <ReviewEditHeader />
        <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
      </>
    )
  }

  // 공개 리뷰는 비로그인·타인도 200으로 조회되므로, 수정 화면은 작성자 본인인지 별도로 검증한다.
  const myProfile = isLoggedIn ? (await memberRepository.getMyProfile()).data : null
  const isAuthor = !!myProfile && myProfile.id === data.memberId

  if (!isAuthor) {
    return (
      <>
        <ReviewEditHeader />
        <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('리뷰')} />
      </>
    )
  }

  return (
    <>
      <ReviewEditHeader />
      <ReviewEditForm
        reviewId={reviewId}
        tasteRating={data.tasteRating}
        amountRating={data.amountRating}
        priceRating={data.priceRating}
        content={data.content}
        tagNames={data.tagNames}
        ownerOnly={data.ownerOnly}
      />
    </>
  )
}
