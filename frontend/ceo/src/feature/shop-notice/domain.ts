/**
 * 점주 공지(사장님 공지) 도메인 타입.
 *
 * UI 와 `api/shop-notice/shop-notice.service.ts` 가 함께 쓰는 타입이므로 DTO 가 아닌 이 타입을
 * Props 에 노출한다(`src/feature/AGENTS.md`).
 */
export interface ShopNoticeItem {
  id: number;
  content: string;
  imageUrls: string[];
  /** 앱 노출 여부. 가게당 최대 1건만 true 다 */
  exposed: boolean;
  /** 관리자 게시중단 여부. true 면 점주가 노출을 켜도 고객에게 보이지 않는다 */
  hidden: boolean;
  createdAt: string;
  updatedAt: string;
}
