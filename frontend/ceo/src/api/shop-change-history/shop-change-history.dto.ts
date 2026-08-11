/**
 * 가게 변경이력 조회 DTO — `docs/tasks/backend.md` 3-1·3-2 응답과 1:1 대응.
 *
 * 이 레이어를 벗어나지 않는다(`src/api/AGENTS.md`). UI/feature 는
 * `@/feature/shop/domain` 의 도메인 타입만 import 한다.
 */

/** 목록 조회 query 파라미터. `changedDate` 는 `yyyy-MM-dd`, 생략 시 서버가 오늘로 본다. */
export interface ShopChangeHistoryListQueryRequest {
  category?: string;
  changeType?: string;
  changedDate?: string;
}

export interface ShopChangeHistoryItemResponse {
  id: number;
  category: string;
  categoryName: string;
  changeType: string;
  changeTypeName: string;
  actionType: string;
  actionTypeName: string;
  /** 변경 전 요약. `CREATE` 시 null. 줄바꿈이 포함된 사람이 읽는 문자열이다 */
  previousValue: string | null;
  /** 변경 후 요약. `DELETE` 시 null */
  newValue: string | null;
  /** ISO-8601 LocalDateTime (예: "2026-08-11T19:46:03") */
  changedAt: string;
}

export interface ShopChangeTypeResponse {
  code: string;
  name: string;
}

export interface ShopChangeCategoryResponse {
  code: string;
  name: string;
  changeTypes: ShopChangeTypeResponse[];
}
