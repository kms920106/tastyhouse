/**
 * 점주 계정 단위 도메인 타입.
 *
 * 가게 단위 타입(`@/feature/shop/domain`)과 달리 `shopId` 에 종속되지 않는다 —
 * 개인정보 접속기록(로그인 이력)·시스템 접근권한 이력은 계정에 귀속된 기록이다.
 *
 * 의존 방향은 일방통행이다: `src/api/<resource>` 가 이 모듈을 import 할 수 있고
 * 그 반대는 안 된다(`src/api/AGENTS.md`).
 */

/** 개인정보 접속기록 1건 — `docs/tasks/backend.md` §2-1 응답에 대응 */
export interface CeoLoginHistoryItem {
  id: number;
  /**
   * `SUCCESS` | `FAILURE`.
   *
   * 리터럴 유니온으로 좁히지 않는다 — 백엔드 enum 상수가 추가되는 것이
   * 프론트 타입 에러가 되지 않게 한다(`ShopRequestListItem` 의 `status` 선례).
   */
  result: string;
  /** 서버가 내려준 한글 라벨 (`로그인 성공` / `로그인 실패`) */
  resultName: string;
  /** `BAD_CREDENTIALS` | `ACCOUNT_INACTIVE`. 성공 시 null */
  failureReason: string | null;
  /** 서버가 내려준 한글 라벨 (`비밀번호 불일치` / `비활성 계정`). 성공 시 null */
  failureReasonName: string | null;
  ipAddress: string | null;
  /** 접속 기기 정보(User-Agent). 서버가 500자로 절단해 저장한다 */
  userAgent: string | null;
  /** ISO-8601 LocalDateTime (예: "2026-08-14T19:46:03") */
  loggedInAt: string;
}

/** 시스템 접근권한 이력 1건 — `docs/tasks/backend.md` §2-2 응답에 대응 */
export interface CeoShopAccessHistoryItem {
  id: number;
  shopId: number;
  /** 가게 이름. 서버 DAO 가 `SHOP` join 으로 투영해 내려준다 */
  shopName: string;
  /** `GRANT` | `REVOKE`. 리터럴 유니온으로 좁히지 않는다 */
  actionType: string;
  /** 서버가 내려준 한글 라벨 (`권한 부여` / `권한 말소`) */
  actionTypeName: string;
  /** ISO-8601 LocalDateTime */
  occurredAt: string;
}
