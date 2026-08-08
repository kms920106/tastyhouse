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

/**
 * 가게 영업 상태.
 *
 * 주문 상태(`OrderStatusCode`)의 `'PREPARING'`("준비 중")과 문자열이 겹치지만 별개 개념이다.
 * 여기서의 `'PREPARING'`은 "가게가 지금 주문을 받지 않는 상태"를 뜻한다.
 */
export type ShopOperatingStatus = 'OPEN' | 'PREPARING'

/**
 * 주문불가 사유 코드.
 *
 * 화면 표시는 서버가 함께 내려주는 `unavailableReasonName`(한글 문구)을 그대로 쓴다.
 * 이 코드는 사유별 분기(예: 임시중지면 다른 주문방식 유도)에만 사용한다.
 */
export type OrderUnavailableReasonCode =
  | 'PERMANENTLY_CLOSED' // 폐업
  | 'HIDDEN' // 노출정지
  | 'SUSPENDED' // 영업 임시중지
  | 'TEMPORARILY_CLOSED' // 임시휴무 기간
  | 'REGULAR_CLOSED_DAY' // 정기휴무일
  | 'PUBLIC_HOLIDAY_CLOSED' // 공휴일 휴무
  | 'BREAK_TIME' // 휴게시간
  | 'OUT_OF_BUSINESS_HOURS' // 영업시간 밖
  | 'ORDER_METHOD_NOT_SUPPORTED' // 가게가 그 주문유형을 취급하지 않음

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
