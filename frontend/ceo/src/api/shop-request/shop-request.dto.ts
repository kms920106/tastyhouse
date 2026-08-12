/**
 * 가게 요청처리 현황 조회 DTO — `docs/tasks/backend.md` 4-1 ~ 4-6 응답과 1:1 대응.
 *
 * 이 레이어를 벗어나지 않는다(`src/api/AGENTS.md`). UI/feature 는
 * `@/feature/shop/domain` 의 도메인 타입만 import 한다.
 */

/**
 * 목록 조회 query 파라미터.
 *
 * 날짜는 `yyyy-MM-dd`, 생략하면 그쪽 경계를 적용하지 않는다(반열림 구간의 한쪽만 적용).
 * `requestType`/`status` 미지정은 전체다.
 */
export interface ShopRequestListQueryRequest {
  requestType?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
}

export interface ShopRequestListItemResponse {
  requestId: number;
  requestType: string;
  requestTypeDescription: string;
  summary: string;
  status: string;
  statusDescription: string;
  /** 반려 사유. REJECTED 일 때만 채워진다 */
  rejectReason: string | null;
  contractAmending: boolean;
  hasAttachment: boolean;
  commentCount: number;
  /** ISO-8601 LocalDateTime (예: "2026-08-11T19:46:03") */
  requestedAt: string;
  /** 접수 직후에는 null */
  processedAt: string | null;
}

export interface ShopRequestImageChangeResponse {
  imageType: string;
  imageTypeDescription: string;
  imageUrl: string;
}

export interface ShopRequestAdjustmentResponse {
  counterpartShopName: string;
  counterpartBusinessNumber: string;
  franchiseName: string;
  reason: string;
  consentFileUrl: string;
}

/**
 * 상세 응답 — 목록의 전 필드 + 첨부 + 유형별 블록.
 *
 * `imageChange`/`deliveryAreaAdjustment` 는 `requestType` 에 따라 한쪽만 채워진다.
 */
export interface ShopRequestDetailResponse extends ShopRequestListItemResponse {
  attachmentLabel: string | null;
  attachmentUrl: string | null;
  imageChange: ShopRequestImageChangeResponse | null;
  deliveryAreaAdjustment: ShopRequestAdjustmentResponse | null;
}

export interface ShopRequestCommentResponse {
  commentId: number;
  authorType: string;
  authorTypeDescription: string;
  content: string;
  createdAt: string;
}

/** 문의 작성 요청 본문. 서버 제약은 `@NotBlank` + `@Size(max = 1000)` */
export interface ShopRequestCommentCreateRequest {
  content: string;
}

export interface ShopRequestTypeItemResponse {
  code: string;
  description: string;
  contractAmending: boolean;
}

export interface ShopRequestStatusItemResponse {
  code: string;
  description: string;
}

/** 요청 유형·상태 카탈로그. 한글 라벨을 서버가 내려 프론트 상수 복제를 막는다 */
export interface ShopRequestTypeCatalogResponse {
  requestTypes: ShopRequestTypeItemResponse[];
  statuses: ShopRequestStatusItemResponse[];
}
