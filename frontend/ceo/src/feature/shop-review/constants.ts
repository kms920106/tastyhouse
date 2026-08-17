import type { ReviewSortType, ShopReviewTab } from "./domain";

/**
 * 탭·정렬·주문유형 카탈로그.
 *
 * 게시중단 요청 사유만 서버 카탈로그(`GET /api/shops/v1/review-blind-reasons`)에서 받아 쓰고,
 * 나머지는 값 집합이 URL 파싱·폼 스키마와 함께 묶여 있어야 하므로 여기에 둔다.
 */

export const SHOP_REVIEW_TABS = {
  ALL: "ALL",
  UNANSWERED: "UNANSWERED",
  BLINDED: "BLINDED",
  OWNER_ONLY: "OWNER_ONLY",
} as const;

// "차단"(관리자 조치)과 "사장님만보기"(작성자 선택)는 독립된 축이라 라벨을 섞지 않는다.
export const SHOP_REVIEW_TAB_OPTIONS = [
  { value: SHOP_REVIEW_TABS.ALL, label: "전체" },
  { value: SHOP_REVIEW_TABS.UNANSWERED, label: "미답변" },
  { value: SHOP_REVIEW_TABS.BLINDED, label: "차단" },
  { value: SHOP_REVIEW_TABS.OWNER_ONLY, label: "사장님만보기" },
] as const satisfies readonly { value: ShopReviewTab; label: string }[];

export const REVIEW_SORT_TYPE_OPTIONS = [
  { value: "RECOMMENDED", label: "추천순" },
  { value: "LATEST", label: "최신순" },
  { value: "OLDEST", label: "등록순" },
] as const satisfies readonly { value: ReviewSortType; label: string }[];

/** 서버 미설정 가게의 기본값과 같은 값(원문 "기본 적용값은 최신순") */
export const DEFAULT_REVIEW_SORT_TYPE: ReviewSortType = "LATEST";

/**
 * 주문유형 카탈로그.
 *
 * 목록·상세 표시는 서버가 내려주는 `orderMethodDescription` 을 그대로 쓰고,
 * 이 표는 **필터 드롭다운 옵션을 만들 때만** 쓴다(서버가 필터용 카탈로그를 주지 않는다).
 */
export const ORDER_METHOD_OPTIONS = [
  { value: "DELIVERY", label: "배달" },
  { value: "TAKEOUT", label: "포장" },
  { value: "TABLE", label: "테이블오더" },
  { value: "RESERVATION", label: "예약" },
] as const;

/** 별점 필터 옵션(5 → 1) */
export const RATING_FILTER_OPTIONS = [5, 4, 3, 2, 1] as const;

/** 기타 사유를 고르면 상세 사유가 필수가 된다(서버 `REVIEW_BLIND_DETAIL_REASON_REQUIRED`) */
export const BLIND_REASON_ETC = "ETC";

/** 게시중단 요청 취소가 가능한 상태 */
export const BLIND_REQUEST_PENDING_STATUS = "PENDING";

/** 서버 `@Size(max = 1000)` 과 같은 값 */
export const OWNER_REPLY_MAX_LENGTH = 1000;

/** 서버 `@Size(max = 500)` 과 같은 값 */
export const BLIND_DETAIL_REASON_MAX_LENGTH = 500;

/**
 * 게시중단 요청 증빙 첨부 최대 개수 — 서버 `@Size(max = 3)` 과 같은 값
 * (`docs/tasks/backend.md` 4-1).
 */
export const BLIND_ATTACHMENT_MAX_COUNT = 3;

/**
 * 첨부 `<input type="file">` 의 `accept`.
 *
 * 신분증·위임장 스캔본이 대상이라 **PDF 를 포함한다** — 이미지 전용 검증기(`validateImageFile`)는
 * `createImageBitmap` 으로 치수를 재느라 PDF 를 거부하므로, 검증은 `validateConsentFile()` 을 재사용한다.
 */
export const BLIND_ATTACHMENT_ACCEPT = "image/jpeg,image/png,image/gif,image/webp,application/pdf";

/** 목록 한 페이지 크기 */
export const SHOP_REVIEW_PAGE_SIZE = 10;

/** 원문 기준 리뷰 사진 최대 장수 — 확대 캐러셀에 캐러셀 컨트롤을 붙일지 판단하는 기준값이다 */
export const REVIEW_IMAGE_MAX_COUNT = 5;

/** 항목별 평균 평점 표시 순서 — 상세와 통계가 같은 순서를 쓰도록 한곳에 둔다 */
export const REVIEW_RATING_ASPECTS = [
  { key: "taste", label: "맛" },
  { key: "amount", label: "양" },
  { key: "price", label: "가격" },
  { key: "atmosphere", label: "분위기" },
  { key: "kindness", label: "친절" },
  { key: "hygiene", label: "위생" },
] as const;

export type ReviewRatingAspectKey = (typeof REVIEW_RATING_ASPECTS)[number]["key"];
