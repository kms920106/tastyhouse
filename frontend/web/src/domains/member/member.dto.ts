import type { PaymentStatus } from '../payment'
import type {
  CouponDiscountType,
  MemberGender,
  MemberGradeCode,
  PointType,
  WithdrawReason,
} from './member.types'

interface PointHistoryItem {
  reason: string
  date: string
  pointAmount: number
  pointType: PointType
}

export interface NicknameAvailabilityResponse {
  available: boolean
}

export interface PhoneAvailabilityResponse {
  available: boolean
}

export interface OtherMemberProfileResponse {
  id: number
  nickname: string
  memberGrade: MemberGradeCode
  statusMessage: string | null
  profileImageUrl: string | null
  following: boolean
}

export interface MemberProfileResponse {
  id: number
  nickname: string
  grade: MemberGradeCode
  statusMessage: string | null
  profileImageUrl: string | null
}

export interface MyProfileResponse {
  id: number
  nickname: string
  grade: MemberGradeCode
  statusMessage: string | null
  profileImageUrl: string | null
}

export interface MemberInfoResponse {
  id: number
  nickname: string
  grade: MemberGradeCode
  statusMessage: string | null
  profileImageUrl: string | null
  fullName: string
  phoneNumber: string
  email: string
}

export interface MemberStatsResponse {
  reviewCount: number
  followingCount: number
  followerCount: number
}

export interface MemberCouponListItemResponse {
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

export interface UsablePointResponse {
  usablePoints: number
}

export interface PointHistoryResponse {
  availablePoints: number
  expiredThisMonth: number
  histories: PointHistoryItem[]
}

export interface MyReviewCountResponse {
  reviewCount: number
}

export interface MyReviewListItemResponse {
  id: number
  imageUrl: string
}

export interface MyPaymentListItemResponse {
  paymentId: number
  shopName: string
  shopThumbnailImageUrl: string
  firstProductName: string
  totalItemCount: number
  amount: number
  paymentStatus: PaymentStatus
  paymentDate: string
}

export interface MyBookmarkedShopListItemResponse {
  bookmarkId: number
  shopId: number
  shopName: string
  stationName: string
  rating: number
  imageUrl: string
  bookmarked: boolean
}

export interface UpdateProfileRequest {
  nickname?: string
  statusMessage?: string
  profileImageFileId?: number
}

export interface PersonalInfoResponse {
  email: string
  fullName: string
  phoneNumber: string
  birthDate: number | null
  gender: MemberGender | null
  pushNotificationEnabled: boolean
  marketingInfoEnabled: boolean
  eventInfoEnabled: boolean
}

export interface VerifyPasswordRequest {
  password: string
}

export interface VerifyPasswordResponse {
  verifyToken: string
}

export interface UpdatePersonalInfoRequest {
  fullName: string
  phoneNumber?: string
  birthDate?: number
  gender?: MemberGender
  pushNotificationEnabled?: boolean
  marketingInfoEnabled?: boolean
  eventInfoEnabled?: boolean
}

export interface MyGradeResponse {
  currentGrade: MemberGradeCode
  currentGradeDisplayName: string
  nextGrade: MemberGradeCode | null
  nextGradeDisplayName: string | null
  currentReviewCount: number
  reviewsNeededForNextGrade: number | null
}

export interface UpdatePasswordRequest {
  newPassword: string
  newPasswordConfirm: string
}

export interface WithdrawRequest {
  reason: WithdrawReason
  reasonDetail?: string
}

export interface MemberDeliveryAddressItemResponse {
  id: number
  alias: string | null
  roadAddress: string
  lotAddress: string | null
  detailAddress: string | null
  /** 행정동 ID. 주소 문자열 매칭에 실패하면 null이며, 그래도 등록은 성공한다 */
  adminDongId: number | null
  regionName: string | null
  latitude: number
  longitude: number
  /**
   * 기본 배송지 여부.
   *
   * 요청 필드명은 `isDefault`인데 응답은 `defaultAddress`다 — 서버가 boolean 필드를
   * record 접근자 이름 그대로 직렬화하기 때문이며, 프론트가 맞춰야 하는 쪽이다.
   */
  defaultAddress: boolean
}

export interface MemberDeliveryAddressCreateRequest {
  alias?: string
  roadAddress: string
  lotAddress?: string
  detailAddress?: string
  /** 필수 — 좌표가 없으면 서버가 저장을 거부한다. 카카오 로컬 API로 변환해서 채운다 */
  latitude: number
  /** 필수 — 좌표가 없으면 서버가 저장을 거부한다 */
  longitude: number
  isDefault?: boolean
}

/**
 * 배달 주소 수정 요청.
 *
 * 생성 요청과 필드가 같되 `isDefault`만 없다 — 기본 배송지 지정은 전용 엔드포인트
 * (`PATCH /v1/me/delivery-addresses/{id}/default`)가 단독으로 담당하므로 여기에 실어도
 * 서버가 무시한다. 수정 폼에서 기본 배송지를 함께 바꾸려면 PUT 후 PATCH를 순서대로 호출한다.
 */
export interface MemberDeliveryAddressUpdateRequest {
  alias?: string
  roadAddress: string
  lotAddress?: string
  detailAddress?: string
  /** 필수 — 좌표가 없으면 서버가 저장을 거부한다 */
  latitude: number
  /** 필수 — 좌표가 없으면 서버가 저장을 거부한다 */
  longitude: number
}
