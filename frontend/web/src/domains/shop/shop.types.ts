export type ShopFoodType =
  | 'KOREAN'
  | 'JAPANESE'
  | 'WESTERN'
  | 'CHINESE'
  | 'WORLD'
  | 'SNACK'
  | 'BAR'
  | 'CAFE'

export type ShopAmenityCode =
  | 'PARKING'
  | 'RESTROOM'
  | 'RESERVATION'
  | 'BABY_CHAIR'
  | 'PET_FRIENDLY'
  | 'OUTLET'
  | 'TAKEOUT'
  | 'DELIVERY'

export type ShopImageCategoryCode = 'EXTERIOR' | 'INTERIOR' | 'FOOD' | 'OTHER'

/** 추가 배달팁 방식. 거리별과 지역별은 상호 배타이므로 서버가 하나만 유지한다. */
export type ShopExtraDeliveryTipType = 'NONE' | 'DISTANCE' | 'REGION'

/**
 * 시간별 배달팁 요일 구분.
 *
 * `'HOLIDAY'`는 시간별 배달팁에서 사용할 수 없다(서버가 거부) — 공휴일은 전용 설정으로 처리한다.
 */
export type ShopDeliveryTipDayType =
  | 'DAILY'
  | 'WEEKDAY'
  | 'WEEKEND'
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY'
