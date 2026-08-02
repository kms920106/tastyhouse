'use client'

import ReviewLikeButton from '@/app/reviews/[id]/(detail)/_components/ReviewLikeButton'
import { toast } from '@/components/ui/AppToaster'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'

export default function ReviewLikeButtonError() {
  const handleClick = () => {
    toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
  }

  return <ReviewLikeButton onClick={handleClick} isLiked={false} />
}
