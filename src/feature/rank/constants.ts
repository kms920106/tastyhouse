import type { RankType } from "@/api/rank/rank.dto";

export const RANK_TYPE_LABELS: Record<RankType, string> = {
  ALL: "전체",
  MONTHLY: "월간",
  WEEKLY: "주간",
};

/** 회원 랭킹 조회 기본 조회 개수 */
export const RANK_MEMBER_DEFAULT_LIMIT = 100;

/** 집계 실행 시 type 지정 시 기본 상위 N명 */
export const AGGREGATION_DEFAULT_LIMIT = 10;

export const PRIZE_NAME_MAX = 100;
export const PRIZE_BRAND_MAX = 100;
