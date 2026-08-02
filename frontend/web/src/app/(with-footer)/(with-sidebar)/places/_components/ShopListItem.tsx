'use client'

import {
  ShopCard,
  ShopCardContent,
  ShopCardHeader,
  ShopCardImage,
  ShopCardName,
  ShopCardRating,
  ShopCardStation,
  ShopCardStats,
  ShopCardTags,
} from '@/components/shops/ShopCard'
import { getShopFoodTypeCodeName } from '@/domains/shop/shop.constants'
import type { ShopFoodType } from '@/domains/shop/shop.types'

interface Props {
  id: number
  name: string
  imageUrl: string
  stationName: string
  rating: number
  reviewCount: number
  bookmarkCount: number
  foodTypes: ShopFoodType[]
}

export default function ShopListItem({
  id,
  name,
  imageUrl,
  stationName,
  rating,
  reviewCount,
  bookmarkCount,
  foodTypes,
}: Props) {
  const foodNames = foodTypes.map((foodType) => getShopFoodTypeCodeName(foodType))

  return (
    <li key={id}>
      <ShopCard shopId={id}>
        <ShopCardImage src={imageUrl} alt={name} />
        <ShopCardContent>
          <ShopCardHeader>
            <ShopCardStation>{stationName}</ShopCardStation>
            <ShopCardRating value={rating} />
          </ShopCardHeader>
          <ShopCardName>{name}</ShopCardName>
          <ShopCardStats reviewCount={reviewCount} bookmarkCount={bookmarkCount} />
          <ShopCardTags tags={foodNames} variant="secondary" />
        </ShopCardContent>
      </ShopCard>
    </li>
  )
}
