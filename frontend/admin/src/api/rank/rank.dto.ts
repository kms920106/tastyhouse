// 랭킹 관리자 API 요청/응답 DTO (Rank Admin — /api/ranks)
// DTO 는 이 계층 밖으로 나가지 않는다. UI/feature 는 @/feature/rank/domain 을 사용한다.

export const RANK_TYPE_VALUES = ["ALL", "MONTHLY", "WEEKLY"] as const;
export type RankType = (typeof RANK_TYPE_VALUES)[number];

// 회원 랭킹 목록 조회 쿼리
export interface RankMemberListQueryRequest {
  type?: RankType;
  limit?: number;
}

// 회원 랭킹 목록 항목
export interface RankMemberListItemResponse {
  memberId: number;
  nickname: string;
  profileImageUrl: string | null;
  reviewCount: number;
  rankNo: number;
  grade: string;
}

// 랭킹 수동 집계 요청 (전부 optional — 빈 body {} 허용)
export interface RankAggregationRequest {
  type?: RankType;
  baseDate?: string;
  limit?: number;
}

// 랭킹 기간 목록 항목
export interface RankPeriodListItemResponse {
  id: number;
  startAt: string;
  endAt: string;
  visible: boolean;
}

// 랭킹 기간 상세
export interface RankPeriodDetailResponse {
  id: number;
  startAt: string;
  endAt: string;
  visible: boolean;
  createdAt: string;
  updatedAt: string;
}

// 랭킹 기간 등록/수정 (공용)
export interface RankPeriodSaveRequest {
  startAt: string;
  endAt: string;
  visible: boolean;
}

// 경품 이미지 (imageFileId 가 없으면 null)
export interface RankPrizeImage {
  id: number;
  name: string;
  url: string;
}

// 랭킹 경품 목록 항목 (기간별, 등수 오름차순)
export interface RankPrizeListItemResponse {
  id: number;
  prizeRank: number;
  name: string;
  brand: string;
  image: RankPrizeImage | null;
}

// 랭킹 경품 상세 (prizeId 는 전역 유니크 — 경로에 기간 id 없음)
export interface RankPrizeDetailResponse {
  id: number;
  periodId: number;
  prizeRank: number;
  name: string;
  brand: string;
  image: RankPrizeImage | null;
}

// 랭킹 경품 등록/수정 (공용)
export interface RankPrizeSaveRequest {
  prizeRank: number;
  name: string;
  brand: string;
  imageFileId?: number;
}
