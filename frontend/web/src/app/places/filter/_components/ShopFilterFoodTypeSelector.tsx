'use client'

import FoodTypeItemContent from '@/components/shops/FoodTypeItemContent'
import { ShopFood } from '@/domains/shop'
import { cn } from '@/lib/utils'
import { useShopFilterState } from './ShopFilterStateProvider'

interface Props {
  foods: ShopFood[]
}

export default function ShopFilterFoodTypeSelector({ foods }: Props) {
  const { selectedFoodTypes, toggleFoodType } = useShopFilterState()

  return (
    <div className="grid grid-cols-4 gap-2.5">
      {foods.map((foodType: ShopFood) => {
        const isSelected = selectedFoodTypes.includes(foodType.code)
        return (
          <button
            key={foodType.code}
            className={cn(
              'flex flex-col items-center justify-center gap-[15px] px-5 py-[17px] border box-border cursor-pointer',
              isSelected ? 'bg-[#f8f5f4] border-main' : 'border-line',
            )}
            style={{ aspectRatio: '80 / 95' }}
            onClick={() => toggleFoodType(foodType.code)}
          >
            <FoodTypeItemContent
              name={foodType.name}
              imageUrl={isSelected ? foodType.activeImageUrl : foodType.inactiveImageUrl}
              isSelected={isSelected}
            />
          </button>
        )
      })}
    </div>
  )
}
