'use client'

import ShareButton from '@/components/layouts/header-parts/ShareButton'
import { PAGE_PATHS } from '@/lib/paths'
import { share } from '@/lib/share'
import { useCallback } from 'react'

interface Props {
  shopId: number
  shopName: string
}

export default function ShopDetailShareButtonClient({ shopId, shopName }: Props) {
  const getShareUrl = useCallback(() => {
    return `${window.location.origin}${PAGE_PATHS.PLACE_DETAIL(shopId)}`
  }, [shopId])

  const handleShare = useCallback(async () => {
    await share({
      title: `[테이스티하우스] ${shopName}`,
      text: `'${shopName}' 어때요? 테이스티하우스 앱에서 확인해 보세요.`,
      url: getShareUrl(),
    })
  }, [shopName, getShareUrl])

  return <ShareButton onClick={handleShare} />
}
