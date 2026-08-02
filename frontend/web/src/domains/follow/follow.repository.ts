import 'server-only'

import { api } from '@/lib/api'
import { PaginationParams } from '@/types/common'
import { IsFollowingResponse, MemberSocialProfileListItemResponse } from './follow.dto'

const ENDPOINT = '/api/follows'

export const followRepository = {
  // 팔로잉 목록 조회
  async getFollowingList(memberId: number, params: PaginationParams) {
    return api.get<MemberSocialProfileListItemResponse[]>(`${ENDPOINT}/v1/${memberId}/following`, {
      params,
    })
  },

  // 팔로워 목록 조회
  async getFollowerList(memberId: number, params: PaginationParams) {
    return api.get<MemberSocialProfileListItemResponse[]>(`${ENDPOINT}/v1/${memberId}/followers`, {
      params,
    })
  },

  // 팔로잉 목록 조회 (비로그인)
  async getPublicFollowingList(memberId: number, params: PaginationParams) {
    return api.get<MemberSocialProfileListItemResponse[]>(
      `${ENDPOINT}/v1/${memberId}/following/public`,
      { params },
    )
  },

  // 팔로워 목록 조회 (비로그인)
  async getPublicFollowerList(memberId: number, params: PaginationParams) {
    return api.get<MemberSocialProfileListItemResponse[]>(
      `${ENDPOINT}/v1/${memberId}/followers/public`,
      { params },
    )
  },

  // 팔로우
  async followMember(memberId: number) {
    return api.post<void>(`${ENDPOINT}/v1/${memberId}`)
  },

  // 언팔로우
  async unfollowMember(memberId: number) {
    return api.delete<void>(`${ENDPOINT}/v1/${memberId}`)
  },

  // 팔로워 삭제
  async removeFollower(followerId: number) {
    return api.delete<void>(`${ENDPOINT}/v1/followers/${followerId}`)
  },

  // 회원 검색 (닉네임)
  async searchMembersByNickname(nickname: string, params: PaginationParams) {
    return api.get<MemberSocialProfileListItemResponse[]>(`${ENDPOINT}/v1/search`, {
      params: { nickname, ...params },
    })
  },

  // 팔로우 여부 조회
  async isFollowing(memberId: number) {
    return api.get<IsFollowingResponse>(`${ENDPOINT}/v1/${memberId}/is-following`)
  },
}
