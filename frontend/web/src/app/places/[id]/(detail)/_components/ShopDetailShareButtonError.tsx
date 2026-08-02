'use client'

import ShareButton from '@/components/layouts/header-parts/ShareButton'
import { toast } from '@/components/ui/AppToaster'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'

export default function ShopDetailShareButtonError() {
  const handleClick = () => {
    toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
  }

  return <ShareButton onClick={handleClick} />
}
