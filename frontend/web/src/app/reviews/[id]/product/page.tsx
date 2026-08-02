import ReviewDetailProductPage from './_components/ReviewDetailProductPage'

interface Props {
  params: Promise<{
    id: string
  }>
}

export default async function Page({ params }: Props) {
  const { id } = await params
  const reviewId = Number(id)

  return <ReviewDetailProductPage reviewId={reviewId} />
}
