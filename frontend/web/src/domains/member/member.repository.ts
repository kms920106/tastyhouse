import 'server-only'

import { api } from '@/lib/api'
import { PaginationParams } from '@/types/common'
import type {
  MemberCouponListItemResponse,
  MemberProfileResponse,
  MemberStatsResponse,
  MyBookmarkedShopListItemResponse,
  MyGradeResponse,
  MyProfileResponse,
  MyReviewCountResponse,
  MyReviewListItemResponse,
  NicknameAvailabilityResponse,
  PersonalInfoResponse,
  PhoneAvailabilityResponse,
  PointHistoryResponse,
  UpdatePasswordRequest,
  UpdatePersonalInfoRequest,
  UpdateProfileRequest,
  UsablePointResponse,
  VerifyPasswordRequest,
  VerifyPasswordResponse,
  WithdrawRequest,
} from './member.dto'

const ENDPOINT = '/api/members'

export const memberRepository = {
  // 닉네임 중복확인
  async checkNicknameAvailability(nickname: string) {
    return api.get<NicknameAvailabilityResponse>(`${ENDPOINT}/v1/nickname/availability`, {
      params: { nickname },
    })
  },
  // 휴대폰번호 가입 가능 여부 확인
  async checkPhoneAvailability(phoneNumber: string) {
    return api.get<PhoneAvailabilityResponse>(`${ENDPOINT}/v1/phone/availability`, {
      params: { phoneNumber },
    })
  },
  // 회원 기본 프로필 조회
  async getMemberProfile(memberId: number) {
    return api.get<MemberProfileResponse>(`${ENDPOINT}/v1/${memberId}/profile`)
  },
  // 내 프로필 조회
  async getMyProfile() {
    return api.get<MyProfileResponse>(`${ENDPOINT}/v1/me/profile`)
  },
  // 특정 회원 통계 조회
  async getMemberStats(memberId: number) {
    return api.get<MemberStatsResponse>(`${ENDPOINT}/v1/${memberId}/stats`)
  },
  // 내 통계 조회
  async getMyStats() {
    return api.get<MemberStatsResponse>(`${ENDPOINT}/v1/me/stats`)
  },
  // 사용 가능한 쿠폰 목록 조회
  async getMyAvailableCoupons() {
    return api.get<MemberCouponListItemResponse[]>(`${ENDPOINT}/v1/me/coupons/available`)
  },
  // 보유 쿠폰 목록 조회 (전체)
  async getMyCoupons() {
    return api.get<MemberCouponListItemResponse[]>(`${ENDPOINT}/v1/me/coupons`)
  },
  // 사용 가능 포인트 조회 (주문용)
  async getMyUsablePoint() {
    return api.get<UsablePointResponse>(`${ENDPOINT}/v1/me/point/usable`)
  },
  // 포인트 내역 조회
  async getMyPointHistory() {
    return api.get<PointHistoryResponse>(`${ENDPOINT}/v1/me/point/history`)
  },
  // 내가 작성한 리뷰 개수 조회
  async getMyReviewCount() {
    return api.get<MyReviewCountResponse>(`${ENDPOINT}/v1/me/reviews/count`)
  },
  // 내가 작성한 리뷰 목록 조회
  async getMyReviews(params: PaginationParams) {
    return api.get<MyReviewListItemResponse[]>(`${ENDPOINT}/v1/me/reviews`, {
      params,
    })
  },
  // 내가 즐겨찾기한 플레이스 목록 조회
  async getMyBookmarks(params: PaginationParams) {
    return api.get<MyBookmarkedShopListItemResponse[]>(`${ENDPOINT}/v1/me/bookmarks`, {
      params,
    })
  },
  // 프로필 수정
  async updateMyProfile(data: UpdateProfileRequest) {
    return api.put<void>(`${ENDPOINT}/v1/me/profile`, data)
  },
  // 개인정보 조회
  async getMyPersonalInfo() {
    return api.get<PersonalInfoResponse>(`${ENDPOINT}/v1/me/personal-info`)
  },
  // 비밀번호 인증
  async verifyPassword(data: VerifyPasswordRequest) {
    return api.post<VerifyPasswordResponse>(`${ENDPOINT}/v1/me/verify-password`, data)
  },
  // 개인정보 수정
  async updateMyPersonalInfo(
    data: UpdatePersonalInfoRequest,
    verifyToken: string,
    phoneVerifyToken?: string,
  ) {
    return api.put<void>(`${ENDPOINT}/v1/me/personal-info`, data, {
      headers: {
        'X-Verify-Token': verifyToken,
        ...(phoneVerifyToken ? { 'X-Phone-Verify-Token': phoneVerifyToken } : {}),
      },
    })
  },
  // 내 등급 조회
  async getMyGrade() {
    return api.get<MyGradeResponse>(`${ENDPOINT}/v1/me/grade`)
  },
  // 비밀번호 변경
  async updateMyPassword(data: UpdatePasswordRequest, verifyToken: string) {
    return api.put<void>(`${ENDPOINT}/v1/me/password`, data, {
      headers: {
        'X-Verify-Token': verifyToken,
      },
    })
  },
  // 회원 탈퇴
  async withdrawMember(data: WithdrawRequest) {
    return api.delete<void>(`${ENDPOINT}/v1/me`, data)
  },
}
