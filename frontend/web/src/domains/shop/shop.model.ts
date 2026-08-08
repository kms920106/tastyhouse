import { ShopAmenityCode, ShopDeliveryTipDayType, ShopExtraDeliveryTipType, ShopFoodType } from '.'

export interface Shop {
  id: number
  name: string
  latitude: number
  longitude: number
  rating: number
  roadAddress: string
  lotAddress: string
  phoneNumber: string
  /** 최소주문금액. 0이면 미설정(제한 없음)이며, 배달 주문에만 적용된다. */
  minOrderAmount: number
  /** 배달팁 하한. 0이면 배달팁 없음 */
  minDeliveryTip: number
  /** 배달팁 상한 (고객 주소 확정 전) */
  maxDeliveryTip: number
}

/** 수령시간 예약 슬롯 1개. `label`·`dayLabel`은 서버가 완성해 내려주므로 프론트가 조립하지 않는다. */
export interface ScheduledOrderSlot {
  /** 슬롯 시작 시각. 주문 생성 시 이 값을 그대로 보낸다 */
  startAt: string
  /** 슬롯 종료 시각. 포장(단일 시각)이면 startAt과 동일 */
  endAt: string
  /** 표시용 문구 (예: `"오후 6:00~오후 6:30"`) */
  label: string
  /** `"오늘"` | `"내일"` */
  dayLabel: string
}

/** 가게·주문방법별 예약 가능 여부와 슬롯 목록. */
export interface ScheduledOrderAvailability {
  /** 예약주문 운영 중 AND 슬롯 1개 이상 */
  available: boolean
  /** 안내 문구용 리드타임(분). 배달 120 / 포장 60 */
  leadTimeMinutes: number
  /** DELIVERY면 범위 슬롯(true), TAKEOUT이면 단일 시각(false) */
  rangeSlot: boolean
  slots: ScheduledOrderSlot[]
}

/** 배달팁 상세 안내 팝업·재견적에 쓰는 배달팁 견적. */
export interface ShopDeliveryTip {
  /** 확정 배달팁. 확정 불가(주소 미확정 등)면 null */
  deliveryTip: number | null
  minDeliveryTip: number
  maxDeliveryTip: number
  breakdown: ShopDeliveryTipBreakdown[]
  tiers: ShopDeliveryTipTier[]
  extraTipType: ShopExtraDeliveryTipType
  distance: ShopDeliveryTipDistance | null
  regions: ShopDeliveryTipRegion[]
  schedules: ShopDeliveryTipSchedule[]
  /** 공휴일 배달팁. 0이면 미설정 */
  holidayTipAmount: number
}

export interface ShopDeliveryTipBreakdown {
  /** 계산 근거 문구. 서버가 완성해서 내려주므로 프론트가 조립하지 않는다 */
  label: string
  amount: number
}

export interface ShopDeliveryTipTier {
  minOrderAmount: number
  tipAmount: number
}

export interface ShopDeliveryTipDistance {
  baseDistanceMeters: number
  surchargeUnit: string
  surchargeAmount: number
}

export interface ShopDeliveryTipRegion {
  regionName: string
  tipAmount: number
}

export interface ShopDeliveryTipSchedule {
  dayType: ShopDeliveryTipDayType
  /** 요일 구분 설명. 서버가 완성해서 내려주므로 프론트가 코드를 라벨로 변환하지 않는다 */
  dayTypeDescription: string
  /** `"HH:mm"` — 영업시간·휴게시간 응답과 같은 포맷이다 */
  startTime: string
  /** `"HH:mm"` */
  endTime: string
  tipAmount: number
}

export interface ShopBusinessHour {
  dayType: string
  dayTypeDescription: string
  openTime: string
  closeTime: string
  closed: boolean
}

export interface ShopBreakTime {
  dayType: string
  dayTypeDescription: string
  startTime: string
  endTime: string
}

export interface ShopClosedDay {
  closedDayType: string
  description: string
}

export interface ShopFood {
  code: ShopFoodType
  name: string
  activeImageUrl: string
  inactiveImageUrl: string
}

export interface ShopStation {
  id: number
  name: string
}

export interface ShopAmenity {
  code: ShopAmenityCode
  name: string
  activeImageUrl: string
  inactiveImageUrl: string
}

export interface ShopMapMarker {
  id: number
  latitude: number
  longitude: number
  name: string
}
