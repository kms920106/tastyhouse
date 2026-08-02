import "server-only";

import type { ApiResponse } from "@/api/shared/types";
import type { RankMember, RankPeriod, RankPeriodDetail, RankPrize, RankPrizeDetail } from "@/feature/rank/domain";

import type { RankMemberListQueryRequest } from "./rank.dto";
import { rankRepository } from "./rank.repository";

export const rankService = {
  // 회원 랭킹 목록 조회 — 도메인 반환
  async getMembers(query: RankMemberListQueryRequest): Promise<ApiResponse<RankMember[]>> {
    const res = await rankRepository.getMembers(query);
    return {
      ...res,
      data: res.data?.map((item) => ({
        memberId: item.memberId,
        nickname: item.nickname,
        profileImageUrl: item.profileImageUrl,
        reviewCount: item.reviewCount,
        rankNo: item.rankNo,
        grade: item.grade,
      })),
    };
  },

  // 랭킹 기간 목록 조회 — 도메인 반환
  async getPeriods(): Promise<ApiResponse<RankPeriod[]>> {
    const res = await rankRepository.getPeriods();
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        startAt: item.startAt,
        endAt: item.endAt,
        visible: item.visible,
      })),
    };
  },

  // 랭킹 기간 상세 조회 — 도메인 반환
  async getPeriod(id: number): Promise<ApiResponse<RankPeriodDetail>> {
    const res = await rankRepository.getPeriod(id);
    if (!res.data) return { ...res, data: undefined };
    return {
      ...res,
      data: {
        id: res.data.id,
        startAt: res.data.startAt,
        endAt: res.data.endAt,
        visible: res.data.visible,
        createdAt: res.data.createdAt,
        updatedAt: res.data.updatedAt,
      },
    };
  },

  // 기간별 경품 목록 조회 — 도메인 반환
  async getPrizes(periodId: number): Promise<ApiResponse<RankPrize[]>> {
    const res = await rankRepository.getPrizes(periodId);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        prizeRank: item.prizeRank,
        name: item.name,
        brand: item.brand,
        image: item.image,
      })),
    };
  },

  // 경품 상세 조회 — 도메인 반환
  async getPrize(prizeId: number): Promise<ApiResponse<RankPrizeDetail>> {
    const res = await rankRepository.getPrize(prizeId);
    if (!res.data) return { ...res, data: undefined };
    return {
      ...res,
      data: {
        id: res.data.id,
        periodId: res.data.periodId,
        prizeRank: res.data.prizeRank,
        name: res.data.name,
        brand: res.data.brand,
        image: res.data.image,
      },
    };
  },
};
