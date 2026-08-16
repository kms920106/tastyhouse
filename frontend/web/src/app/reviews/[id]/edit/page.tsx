import type { Metadata } from 'next'
import ReviewEditPage from './_components/ReviewEditPage'

export const metadata: Metadata = {
  title: '리뷰 수정',
}

interface Props {
  params: Promise<{ id: string }>
}

export default async function Page({ params }: Props) {
  const { id } = await params

  return <ReviewEditPage reviewId={Number(id)} />
}
