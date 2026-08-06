import {
  ShopCard,
  ShopCardContent,
  ShopCardDeliveryTip,
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
  /** 배달팁 하한. 상한과 함께 0이면 노출하지 않는다 */
  minDeliveryTip: number
  /** 배달팁 상한 (고객 주소 확정 전) */
  maxDeliveryTip: number
}

export function ShopBestListItem({
  id,
  name,
  imageUrl,
  stationName,
  rating,
  foodTypes,
  minOrderAmount,
  minDeliveryTip,
  maxDeliveryTip,
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
          <ShopCardDeliveryTip min={minDeliveryTip} max={maxDeliveryTip} />
          <ShopCardTags tags={foodNames} />
        </ShopCardContent>
      </ShopCard>
    </li>
  )
}
