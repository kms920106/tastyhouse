import { reviewRepository } from '@/domains/review/review.repository'
import type { Metadata } from 'next'
import ReviewDetailPage from './_components/ReviewDetailPage'

interface Props {
  params: Promise<{
    id: string
  }>
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { id } = await params
  const reviewId = Number(id)

  const { data } = await reviewRepository.getReviewDetail(reviewId)

  if (!data) return {}

  const title = `${data.shopName} 리뷰`
  const description = data.content
  const thumbnailUrl = data.imageUrls[0]

  return {
    title,
    description,
    openGraph: {
      title,
      description,
      ...(thumbnailUrl && { images: [thumbnailUrl] }),
    },
  }
}

export default async function Page({ params }: Props) {
  const { id } = await params
  const reviewId = Number(id)

  return <ReviewDetailPage reviewId={reviewId} />
}
