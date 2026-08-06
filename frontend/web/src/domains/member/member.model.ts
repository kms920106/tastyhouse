import type { CouponDiscountType, MemberGender, MemberGradeCode } from './member.types'

export interface MemberCoupon {
  id: number
  couponId: number
  name: string
  description: string
  discountType: CouponDiscountType
  discountAmount: number
  maxDiscountAmount: number | null
  minOrderAmount: number
  useStartAt: string
  useEndAt: string
  expiredAt: string
  used: boolean
  usedAt: string | null
  daysRemaining: number
  expired: boolean
}

export interface Member {
  id: number
  nickname: string
  grade: MemberGradeCode
  gradeName?: string
  gradeIcon?: string
  gradeColor?: string
  statusMessage: string | null
  profileImageUrl: string | null
}

export interface MemberPersonalInfo {
  email: string
  fullName: string
  phoneNumber: string
  birthDate: number | null
  gender: MemberGender | null
  pushNotificationEnabled: boolean
  marketingInfoEnabled: boolean
  eventInfoEnabled: boolean
}

/** 회원 배달 주소록 항목. 좌표는 서버가 저장해 두고 배달팁 계산에만 사용한다. */
export interface MemberDeliveryAddress {
  id: number
  alias: string | null
  roadAddress: string
  lotAddress: string | null
  detailAddress: string | null
  /** 행정동 ID. 주소 문자열 매칭에 실패하면 null이며, 그 주소는 지역별 배달팁만 적용되지 않는다 */
  adminDongId: number | null
  regionName: string | null
  latitude: number
  longitude: number
  /** 기본 배송지 여부. 응답 필드명이 `defaultAddress`라 model도 같은 이름을 쓴다 */
  defaultAddress: boolean
}

export interface SocialMember {
  memberId: number
  nickname: string
  grade: MemberGradeCode
  profileImageUrl: string | null
  following: boolean
}
