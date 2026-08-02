import ReviewOptionButton from '@/components/reviews/ReviewOptionButton'
import ReviewOptionDrawer from '@/components/reviews/ReviewOptionDrawer'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'

interface Props {
  reviewId: number
  memberId: number
  memberNickname: string
  content: string
  isLoggedIn: boolean
}

export default function ReviewOptionDrawerServer({
  reviewId,
  memberId,
  memberNickname,
  content,
  isLoggedIn,
}: Props) {
  if (!isLoggedIn) {
    return (
      <Link href={PAGE_PATHS.AUTH_LOGIN}>
        <ReviewOptionButton />
      </Link>
    )
  }

  return (
    <ReviewOptionDrawer
      reviewId={reviewId}
      memberId={memberId}
      memberNickname={memberNickname}
      content={content}
    />
  )
}
