'use client'

import { getShopAmenityCodeName } from '@/domains/shop/shop.constants'
import { ShopAmenity } from '@/domains/shop/shop.model'
import { cn } from '@/lib/utils'
import FacilityItemContent from './FacilityItemContent'

interface Props {
  amenity: ShopAmenity
  isSelected: boolean
  onClick: () => void
}

export function FacilityButton({ amenity, isSelected, onClick }: Props) {
  return (
    <button
      onClick={onClick}
      className={cn(
        'flex flex-col items-center justify-center px-5 py-[17px] border box-border cursor-pointer',
        isSelected ? 'bg-[#f8f5f4] border-main' : 'border-line',
      )}
      style={{ aspectRatio: '80 / 95' }}
    >
      <FacilityItemContent
        name={getShopAmenityCodeName(amenity.code)}
        imageUrl={isSelected ? amenity.activeImageUrl : amenity.inactiveImageUrl}
        isSelected={isSelected}
      />
    </button>
  )
}
