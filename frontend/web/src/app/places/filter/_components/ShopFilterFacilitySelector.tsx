'use client'

import { FacilityButton } from '@/components/shops/FacilityItem'
import { FacilitySelectorLayout } from '@/components/shops/FacilitySelectorLayout'
import { ShopAmenity } from '@/domains/shop/shop.model'
import { useShopFilterState } from './ShopFilterStateProvider'

interface Props {
  amenities: ShopAmenity[]
}

export default function ShopFilterFacilitySelector({ amenities }: Props) {
  const { selectedAmenities, toggleAmenity } = useShopFilterState()

  return (
    <FacilitySelectorLayout>
      {amenities.map((amenity: ShopAmenity) => {
        const isSelected = selectedAmenities.includes(amenity.code)
        return (
          <FacilityButton
            key={amenity.code}
            amenity={amenity}
            isSelected={isSelected}
            onClick={() => toggleAmenity(amenity.code)}
          />
        )
      })}
    </FacilitySelectorLayout>
  )
}
