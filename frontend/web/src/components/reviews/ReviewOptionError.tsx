'use client'

import { toast } from '../ui/AppToaster'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import ReviewOptionButton from './ReviewOptionButton'

export default function ReviewOptionError() {
  const handleClick = () => {
    toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
  }

  return <ReviewOptionButton onClick={handleClick} />
}
