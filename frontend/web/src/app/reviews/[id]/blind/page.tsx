import { REVIEW_BLIND_CONSENT_COPY } from '@/domains/review'
import type { Metadata } from 'next'
import ReviewBlindConsentPage from './_components/ReviewBlindConsentPage'

export const metadata: Metadata = {
  title: REVIEW_BLIND_CONSENT_COPY.PAGE_TITLE,
}

interface Props {
  params: Promise<{ id: string }>
}

export default async function Page({ params }: Props) {
  const { id } = await params

  return <ReviewBlindConsentPage reviewId={Number(id)} />
}
