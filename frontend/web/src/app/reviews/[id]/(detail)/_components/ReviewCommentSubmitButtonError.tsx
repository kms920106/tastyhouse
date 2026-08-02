'use client'

import { toast } from '@/components/ui/AppToaster'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import ReviewCommentSubmitButton from './ReviewCommentSubmitButton'

export default function ReviewCommentSubmitButtonError() {
  const handleClick = () => {
    toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
  }

  return <ReviewCommentSubmitButton onClick={handleClick} isSubmitting={false} />
}
