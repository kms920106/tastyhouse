'use client'

import ImageContainer from '@/components/ui/ImageContainer'
import Rating from '@/components/ui/Rating'
import { useShopBookmark } from '@/domains/shop/shop.hook'
import { PAGE_PATHS } from '@/lib/paths'
import Icon from '@/components/ui/Icon'
import Link from 'next/link'

interface Props {
  shopId: number
  shopImage: string
  region: string
  shopName: string
  rating: number
  isBookmarked: boolean
}

export default function BookmarkListItem({
  shopId,
  shopImage,
  region,
  shopName,
  rating,
  isBookmarked: initialIsBookmarked,
}: Props) {
  const { isBookmarked, isPending, toggleBookmark } = useShopBookmark({
    shopId,
    initialIsBookmarked,
  })

  const handleBookmarkClick = (e: React.MouseEvent) => {
    e.preventDefault()
    e.stopPropagation()
    toggleBookmark()
  }

  return (
    <Link href={PAGE_PATHS.PLACE_DETAIL(shopId)} className="block">
      <div className="relative p-2.5 bg-white border border-line box-border shadow-2xs rounded-[2.5px]">
        <div className="flex items-center gap-4">
          <ImageContainer src={shopImage} alt={shopName} size={75} rounded="2.5px" />
          <div className="flex-1 flex flex-col justify-between py-1">
            <p className="text-xs leading-[12px] text-[#aaaaaa] truncate">{region}</p>
            <p className="text-lg leading-[18px] mt-2 truncate">{shopName}</p>
            <Rating value={rating} className="mt-[15px]" />
          </div>
        </div>
        <button
          type="button"
          onClick={handleBookmarkClick}
          className="absolute"
          style={{
            right: '15px',
            top: '-3px',
          }}
          disabled={isPending}
        >
          <Icon name={isBookmarked ? 'mypage/bookmark-on' : 'mypage/bookmark-off'} />
        </button>
      </div>
    </Link>
  )
}
