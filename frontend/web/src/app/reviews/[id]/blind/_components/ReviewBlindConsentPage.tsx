import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '@/components/layouts/Header'
import { BackButton } from '@/components/layouts/header-parts'
import ReviewImageGallery from '@/components/reviews/ReviewImageGallery'
import FetchErrorState from '@/components/ui/FetchErrorState'
import TextContent from '@/components/ui/TextContent'
import {
  REVIEW_BLIND_CONSENT_COPY,
  REVIEW_BLIND_ERROR_CODE,
  REVIEW_BLIND_REASON_LABEL,
} from '@/domains/review'
import { reviewRepository } from '@/domains/review/review.repository'
import { getIsLoggedIn } from '@/lib/auth-config'
import { formatDate } from '@/lib/date'
import { PAGE_PATHS } from '@/lib/paths'
import { notFound, redirect } from 'next/navigation'
import ReviewBlindConsentActions from './ReviewBlindConsentActions'

interface Props {
  reviewId: number
}

export default async function ReviewBlindConsentPage({ reviewId }: Props) {
  const isLoggedIn = await getIsLoggedIn()

  if (!isLoggedIn) {
    redirect(PAGE_PATHS.AUTH_LOGIN)
  }

  const { error, status, errorCode, data } = await reviewRepository.getReviewBlindDetail(reviewId)

  if (error && status === 401) {
    redirect(PAGE_PATHS.AUTH_LOGIN)
  }

  // 타인 리뷰·존재하지 않는 리뷰는 존재를 숨기기 위해 서버가 404로 통일해 내린다.
  if (status === 404 || errorCode === REVIEW_BLIND_ERROR_CODE.REVIEW_NOT_FOUND) {
    notFound()
  }

  if (error || !data) {
    return (
      <>
        <BlindConsentHeader />
        <FetchErrorState message={REVIEW_BLIND_CONSENT_COPY.FETCH_FAILED} />
      </>
    )
  }

  const reasonLabel = data.reasonDescription ?? REVIEW_BLIND_REASON_LABEL[data.reason]
  const blindUntilLabel = data.blindUntil
    ? formatDate(data.blindUntil, 'YYYY-MM-DD')
    : REVIEW_BLIND_CONSENT_COPY.BLIND_UNTIL_EMPTY

  return (
    <>
      <BlindConsentHeader />

      <div className="px-[15px] py-5">
        <section className="rounded-[2.5px] bg-[#fafafa] px-4 py-5">
          <p className="text-sm leading-[14px] font-bold text-foreground">
            {REVIEW_BLIND_CONSENT_COPY.NOTICE_TITLE}
          </p>
          <p className="mt-2.5 text-sm leading-[20px] text-[#666666]">
            {REVIEW_BLIND_CONSENT_COPY.NOTICE_DESCRIPTION}
          </p>

          <dl className="mt-5 flex flex-col gap-2.5">
            <InfoRow label={REVIEW_BLIND_CONSENT_COPY.REASON_LABEL} value={reasonLabel} />
            {data.detailReason && (
              <InfoRow
                label={REVIEW_BLIND_CONSENT_COPY.DETAIL_REASON_LABEL}
                value={data.detailReason}
              />
            )}
            <InfoRow label={REVIEW_BLIND_CONSENT_COPY.BLIND_UNTIL_LABEL} value={blindUntilLabel} />
          </dl>
        </section>

        <section className="mt-[30px]">
          <h2 className="text-sm leading-[14px] font-bold text-foreground">
            {REVIEW_BLIND_CONSENT_COPY.REVIEW_CONTENT_LABEL}
          </h2>
          {data.imageUrls.length > 0 && (
            <div className="mt-[15px]">
              <ReviewImageGallery imageUrls={data.imageUrls} />
            </div>
          )}
          <div className="mt-[15px]">
            <TextContent text={data.content} />
          </div>
        </section>

        <section className="mt-[30px] border-t border-line pt-[30px]">
          <p className="text-sm leading-[14px] font-bold text-foreground">
            {REVIEW_BLIND_CONSENT_COPY.CHOICE_TITLE}
          </p>
          <p className="mt-2.5 text-sm leading-[20px] whitespace-pre-line text-[#666666]">
            {REVIEW_BLIND_CONSENT_COPY.CHOICE_DESCRIPTION}
          </p>
          <div className="mt-5">
            <ReviewBlindConsentActions reviewId={reviewId} />
          </div>
        </section>
      </div>
    </>
  )
}

function BlindConsentHeader() {
  return (
    <Header variant="white" height={55}>
      <HeaderLeft>
        <BackButton />
      </HeaderLeft>
      <HeaderCenter>
        <HeaderTitle>{REVIEW_BLIND_CONSENT_COPY.HEADER_TITLE}</HeaderTitle>
      </HeaderCenter>
    </Header>
  )
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex gap-2.5 text-sm leading-[20px]">
      <dt className="w-[80px] shrink-0 text-[#999999]">{label}</dt>
      <dd className="flex-1 break-words text-foreground">{value}</dd>
    </div>
  )
}
