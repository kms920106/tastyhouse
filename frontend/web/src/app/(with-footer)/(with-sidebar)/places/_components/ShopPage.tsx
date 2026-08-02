import { ShopAmenityCode, ShopFoodType } from '@/domains/shop'
import ShopContent from './ShopContent'
import ShopHeader from './ShopHeader'

interface Props {
  stationId?: number
  foodTypes?: ShopFoodType[]
  amenities?: ShopAmenityCode[]
}

export default function ShopPage({ stationId, foodTypes, amenities }: Props) {
  return (
    <>
      <ShopHeader />
      <ShopContent stationId={stationId} foodTypes={foodTypes} amenities={amenities} />
    </>
  )
}
