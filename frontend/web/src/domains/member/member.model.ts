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

export interface SocialMember {
  memberId: number
  nickname: string
  grade: MemberGradeCode
  profileImageUrl: string | null
  following: boolean
}
