import type { ShopFoodType, ShopAmenityCode } from './shop.types'

const SHOP_FOOD_TYPE_NAMES: Record<ShopFoodType, string> = {
  KOREAN: '한식',
  JAPANESE: '일식',
  WESTERN: '양식',
  CHINESE: '중식',
  WORLD: '세계음식',
  SNACK: '분식',
  BAR: '주점',
  CAFE: '카페',
}

const SHOP_AMENITY_CODE_NAMES: Record<ShopAmenityCode, string> = {
  PARKING: '주차',
  RESTROOM: '내부화장실',
  RESERVATION: '예약',
  BABY_CHAIR: '아기의자',
  PET_FRIENDLY: '애견동반',
  OUTLET: '개별 콘센트',
  TAKEOUT: '포장',
  DELIVERY: '배달',
}

export const getShopFoodTypeCodeName = (foodType: ShopFoodType): string => {
  return SHOP_FOOD_TYPE_NAMES[foodType] || foodType
}

export const getShopAmenityCodeName = (amenityCode: ShopAmenityCode): string => {
  return SHOP_AMENITY_CODE_NAMES[amenityCode]
}
