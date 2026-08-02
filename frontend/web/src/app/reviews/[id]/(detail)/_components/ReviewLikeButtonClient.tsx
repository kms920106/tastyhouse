'use client'

import { toast } from '@/components/ui/AppToaster'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { toggleReviewLike } from '@/actions/review'
import { useState, useTransition } from 'react'
import ReviewLikeButton from './ReviewLikeButton'

interface Props {
  initialIsLiked: boolean
  reviewId: number
}

export default function ReviewLikeButtonClient({
  initialIsLiked,
  reviewId,
}: Props) {
  const [isLiked, setIsLiked] = useState(initialIsLiked)
  const [isPending, startTransition] = useTransition()

  const handleLike = () => {
    if (isPending) return

    startTransition(async () => {
      const { error, data } = await toggleReviewLike(reviewId)

      if (error || !data) {
        toast(error || COMMON_ERROR_MESSAGES.MUTATION_ERROR)
        return
      }

      const { liked } = data

      setIsLiked(liked)
    })
  }

  return <ReviewLikeButton onClick={handleLike} isLiked={isLiked} disabled={isPending} />
}
