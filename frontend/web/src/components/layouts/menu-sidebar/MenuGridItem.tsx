import FoodTypeItemContent from '@/components/shops/FoodTypeItemContent'
import type { ShopFood } from '@/domains/shop/shop.model'

interface Props {
  foodType: ShopFood
}

export default function MenuGridItem({ foodType }: Props) {
  return (
    <FoodTypeItemContent
      name={foodType.name}
      imageUrl={foodType.activeImageUrl}
      isSelected={true}
    />
  )
}
