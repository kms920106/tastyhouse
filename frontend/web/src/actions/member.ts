'use server'

import type {
  UpdatePasswordRequest,
  UpdatePersonalInfoRequest,
  UpdateProfileRequest,
  VerifyPasswordRequest,
  WithdrawRequest,
} from '@/domains/member'
import { memberRepository } from '@/domains/member/member.repository'
import { memberService } from '@/domains/member/member.service'
import { AUTH_COOKIE_KEYS } from '@/lib/auth-config'
import { cookies } from 'next/headers'

export async function getMyProfile() {
  return memberService.getMyProfile()
}

export async function getMemberStats(memberId: number) {
  return memberRepository.getMemberStats(memberId)
}

export async function getMyStats() {
  return memberRepository.getMyStats()
}

export async function getMemberAvailableCoupons() {
  return memberRepository.getMyAvailableCoupons()
}

export async function getMemberUsablePoint() {
  return memberRepository.getMyUsablePoint()
}

export async function getMyBookmarks(page: number = 0, size: number = 10) {
  return memberRepository.getMyBookmarks({ page, size })
}

export async function getMyReviewCount() {
  return memberRepository.getMyReviewCount()
}

export async function getMyReviews(page: number = 0, size: number = 9) {
  return memberRepository.getMyReviews({ page, size })
}

export async function getMemberProfile(memberId: number) {
  return memberRepository.getMemberProfile(memberId)
}

export async function getMemberPersonalInfo() {
  return memberRepository.getMyPersonalInfo()
}

export async function updateMemberProfile(data: UpdateProfileRequest) {
  return memberRepository.updateMyProfile(data)
}

export async function verifyMemberPassword(data: VerifyPasswordRequest) {
  return memberRepository.verifyPassword(data)
}

export async function updateMemberPersonalInfo(
  data: UpdatePersonalInfoRequest,
  verifyToken: string,
  phoneVerifyToken?: string,
) {
  return memberRepository.updateMyPersonalInfo(data, verifyToken, phoneVerifyToken)
}

export async function updateMemberPassword(data: UpdatePasswordRequest, verifyToken: string) {
  return memberRepository.updateMyPassword(data, verifyToken)
}

export async function checkNicknameAvailability(nickname: string) {
  return memberRepository.checkNicknameAvailability(nickname)
}

export async function withdrawMember(data: WithdrawRequest) {
  const result = await memberRepository.withdrawMember(data)

  if (!result?.error) {
    const cookieStore = await cookies()
    cookieStore.delete(AUTH_COOKIE_KEYS.ACCESS_TOKEN)
    cookieStore.delete(AUTH_COOKIE_KEYS.REFRESH_TOKEN)
  }

  return result
}
