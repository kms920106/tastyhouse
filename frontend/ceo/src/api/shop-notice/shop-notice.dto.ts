/**
 * 점주 공지 API DTO.
 *
 * DTO 는 이 레이어 밖으로 나가지 않는다 — UI 는 `feature/shop-notice/domain.ts` 의
 * `ShopNoticeItem` 을 쓰고, 변환은 `shop-notice.service.ts` 가 담당한다(`src/api/AGENTS.md`).
 */

export interface ShopNoticeResponse {
  id: number;
  content: string;
  imageUrls: string[];
  exposed: boolean;
  hidden: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ShopNoticeExposureUpdateRequest {
  exposed: boolean;
}

export interface ShopNoticeContentValidateRequest {
  content: string;
}
