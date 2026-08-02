import { ShopAmenityCode, ShopFoodType } from '.'

export interface Shop {
  id: number
  name: string
  latitude: number
  longitude: number
  rating: number
  roadAddress: string
  lotAddress: string
  phoneNumber: string
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
