import 'server-only'

import { api } from '@/lib/api'
import {
  RankDurationResponse,
  RankMemberMeQuery,
  RankMemberQuery,
  RankMemberListItemResponse,
  RankPrizeListItemResponse,
} from './rank.dto'

const ENDPOINT = '/api/ranks'

export const rankRepository = {
  // 랭킹 기간 조회
  async getRankDuration() {
    return api.get<RankDurationResponse>(`${ENDPOINT}/v1/duration`)
  },
  // 랭킹 경품 목록 조회
  async getRankPrizes() {
    return api.get<RankPrizeListItemResponse[]>(`${ENDPOINT}/v1/prizes`)
  },
  // 멤버 리뷰 랭킹 조회
  async getRankMembers(params: RankMemberQuery) {
    return api.get<RankMemberListItemResponse[]>(`${ENDPOINT}/v1/members`, { params })
  },
  // 내 리뷰 랭킹 조회
  async getRankMembersMe(params: RankMemberMeQuery) {
    return api.get<RankMemberListItemResponse>(`${ENDPOINT}/v1/members/me`, { params })
  },
}
