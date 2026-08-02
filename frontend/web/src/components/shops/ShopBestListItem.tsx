import {
  ShopCard,
  ShopCardContent,
  ShopCardHeader,
  ShopCardImage,
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
}

export function ShopBestListItem({ id, name, imageUrl, stationName, rating, foodTypes }: Props) {
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
          <ShopCardTags tags={foodNames} />
        </ShopCardContent>
      </ShopCard>
    </li>
  )
}
