import type {
  ShopAmenityCode,
  ShopDeliveryTipDayType,
  ShopFoodType,
  ShopOperatingStatus,
} from './shop.types'

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

const SHOP_DELIVERY_TIP_DAY_TYPE_NAMES: Record<ShopDeliveryTipDayType, string> = {
  DAILY: '매일',
  WEEKDAY: '평일',
  WEEKEND: '주말',
  MONDAY: '월요일',
  TUESDAY: '화요일',
  WEDNESDAY: '수요일',
  THURSDAY: '목요일',
  FRIDAY: '금요일',
  SATURDAY: '토요일',
  SUNDAY: '일요일',
}

export const getShopAmenityCodeName = (amenityCode: ShopAmenityCode): string => {
  return SHOP_AMENITY_CODE_NAMES[amenityCode]
}

export const getShopDeliveryTipDayTypeName = (dayType: ShopDeliveryTipDayType): string => {
  return SHOP_DELIVERY_TIP_DAY_TYPE_NAMES[dayType] || dayType
}

// 가게 영업 상태 라벨. 주문 상태의 '준비 중'과 문자열이 겹치는 별개 개념이다.
const SHOP_OPERATING_STATUS_NAMES: Record<ShopOperatingStatus, string> = {
  OPEN: '영업중',
  PREPARING: '준비중',
}

export const getShopOperatingStatusName = (status: ShopOperatingStatus): string => {
  return SHOP_OPERATING_STATUS_NAMES[status] || status
}
