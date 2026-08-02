// 랭킹 도메인 모델 — UI 와 api/rank.service 가 공유한다.

export type { RankType } from "@/api/rank/rank.dto";

export interface RankMember {
  memberId: number;
  nickname: string;
  profileImageUrl: string | null;
  reviewCount: number;
  rankNo: number;
  grade: string;
}

export interface RankPeriod {
  id: number;
  startAt: string;
  endAt: string;
  visible: boolean;
}

export interface RankPeriodDetail {
  id: number;
  startAt: string;
  endAt: string;
  visible: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface RankPrizeImage {
  id: number;
  name: string;
  url: string;
}

export interface RankPrize {
  id: number;
  prizeRank: number;
  name: string;
  brand: string;
  image: RankPrizeImage | null;
}

export interface RankPrizeDetail {
  id: number;
  periodId: number;
  prizeRank: number;
  name: string;
  brand: string;
  image: RankPrizeImage | null;
}
