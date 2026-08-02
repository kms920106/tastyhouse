'use client'

import { getShopAmenityCodeName } from '@/domains/shop/shop.constants'
import type { ShopAmenity } from '@/domains/shop/shop.model'
import FacilityItemContent from './FacilityItemContent'

interface Props {
  amenity: ShopAmenity
}

export function FacilityDiv({ amenity }: Props) {
  return (
    <div
      className="flex flex-col items-center justify-center px-5 py-[17px] border border-main box-border"
      style={{ aspectRatio: '80 / 95' }}
    >
      <FacilityItemContent
        name={getShopAmenityCodeName(amenity.code)}
        imageUrl={amenity.activeImageUrl}
        isSelected={true}
      />
    </div>
  )
}
