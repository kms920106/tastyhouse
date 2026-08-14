/**
 * 시스템 접근권한 이력 조회 DTO — `docs/tasks/backend.md` §2-2 응답과 1:1 대응.
 *
 * 이 레이어를 벗어나지 않는다(`src/api/AGENTS.md`). UI/feature 는
 * `@/feature/ceo/domain` 의 도메인 타입만 import 한다.
 */

/** 목록 조회 query 파라미터. 날짜는 `yyyy-MM-dd`, 생략 시 서버 기본값(오늘 - 1년 ~ 오늘). */
export interface CeoShopAccessHistoryListQueryRequest {
  // backend.md §2-2 — GRANT | REVOKE. 생략 시 전체
  actionType?: string;
  /** 특정 가게로 좁힘. 생략 시 전체 */
  shopId?: number;
  startDate?: string;
  endDate?: string;
}

export interface CeoShopAccessHistoryItemResponse {
  id: number;
  shopId: number;
  // backend.md §2-2 — DAO 가 SHOP join 으로 투영한다
  shopName: string;
  // backend.md §2-2 — GRANT | REVOKE
  actionType: string;
  /** 한글 라벨 (`권한 부여` / `권한 말소`) */
  actionTypeName: string;
  /** ISO-8601 LocalDateTime */
  occurredAt: string;
}
