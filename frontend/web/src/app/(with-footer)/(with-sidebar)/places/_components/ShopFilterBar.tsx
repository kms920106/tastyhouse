'use client'

import { Skeleton } from '@/components/ui/shadcn/skeleton'
import { ShopAmenityCode, ShopFoodType } from '@/domains/shop'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import { BiFilterAlt } from 'react-icons/bi'

interface Props {
  totalCount: number
  isLoading?: boolean
  stationId?: number
  foodTypes?: ShopFoodType[]
  amenities?: ShopAmenityCode[]
}

export default function ShopFilterBar({
  totalCount,
  isLoading,
  stationId,
  foodTypes,
  amenities,
}: Props) {
  const filterHref = (() => {
    const params = new URLSearchParams()

    if (stationId) params.set('stationId', stationId.toString())
    if (foodTypes && foodTypes.length > 0) params.set('foodTypes', foodTypes.join(','))
    if (amenities && amenities.length > 0) params.set('amenities', amenities.join(','))

    const queryString = params.toString()
    return `${PAGE_PATHS.PLACE_FILTER}${queryString ? `?${queryString}` : ''}`
  })()

  return (
    <div className="flex items-center justify-between mb-5">
      <div className="text-sm leading-[14px]">
        {isLoading ? <Skeleton className="w-12 h-3.5" /> : `총 ${totalCount}개`}
      </div>
      <div className="flex items-center gap-2.5">
        <Link href={filterHref}>
          <BiFilterAlt size={20} />
        </Link>
      </div>
    </div>
  )
}
