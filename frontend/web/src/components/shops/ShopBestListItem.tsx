import {
  ShopCard,
  ShopCardContent,
  ShopCardHeader,
  ShopCardImage,
  ShopCardMinOrder,
  ShopCardName,
  ShopCardRating,
  ShopCardStation,
  ShopCardTags,
} from '@/components/shops/ShopCard'
import { ShopFoodType, getShopFoodTypeCodeName } from '@/domains/shop'

type Props = {
  id: number
  name: string
  imageUrl: string
  stationName: string
  rating: number
  foodTypes: ShopFoodType[]
  /** 가게 최소주문금액. 0이면 미설정이라 노출하지 않는다 */
  minOrderAmount: number
}

export function ShopBestListItem({
  id,
  name,
  imageUrl,
  stationName,
  rating,
  foodTypes,
  minOrderAmount,
}: Props) {
  const foodNames = foodTypes.map((foodType) => getShopFoodTypeCodeName(foodType))

  return (
    <li>
      <ShopCard shopId={id}>
        <ShopCardImage src={imageUrl} alt={name} />
        <ShopCardContent>
          <ShopCardHeader>
            <ShopCardStation>{stationName}</ShopCardStation>
            <ShopCardRating value={rating} />
          </ShopCardHeader>
          <ShopCardName>{name}</ShopCardName>
          <ShopCardMinOrder value={minOrderAmount} />
          <ShopCardTags tags={foodNames} />
        </ShopCardContent>
      </ShopCard>
    </li>
  )
}
