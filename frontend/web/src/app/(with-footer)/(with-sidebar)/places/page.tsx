import { ShopAmenityCode, ShopFoodType } from '@/domains/shop'
import ShopPage from './_components/ShopPage'

interface Props {
  searchParams: Promise<{
    stationId?: string
    foodTypes?: string
    amenities?: string
  }>
}

export default async function Page({ searchParams }: Props) {
  const params = await searchParams

  const stationId = params.stationId ? Number(params.stationId) : undefined
  const foodTypes = params.foodTypes?.split(',').filter(Boolean) as ShopFoodType[] | undefined
  const amenities = params.amenities?.split(',').filter(Boolean) as ShopAmenityCode[] | undefined

  return <ShopPage stationId={stationId} foodTypes={foodTypes} amenities={amenities} />
}
