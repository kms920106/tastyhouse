'use client'

import { useShopBookmark } from '@/domains/shop/shop.hook'
import ShopDetailBookmarkButton from './ShopDetailBookmarkButton'

interface Props {
  initialIsBookmarked: boolean
  shopId: number
}

export default function ShopDetailBookmarkButtonClient({ initialIsBookmarked, shopId }: Props) {
  const { isBookmarked, isPending, toggleBookmark } = useShopBookmark({
    shopId,
    initialIsBookmarked,
  })

  return (
    <ShopDetailBookmarkButton
      onClick={toggleBookmark}
      isBookmarked={isBookmarked}
      disabled={isPending}
    />
  )
}
