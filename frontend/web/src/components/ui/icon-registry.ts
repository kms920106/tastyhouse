/**
 * 아이콘 메타 레지스트리.
 * - 키: public/images 하위에서 `icon-` 접두사 제거한 kebab-case
 * - 서브폴더는 `'<폴더>/<키>'` namespace (루트는 prefix 없음)
 * - alt: 의미 있는 아이콘만 지정. 미지정 = 장식적(Icon에서 aria-hidden 자동)
 * - 등록 원칙: 실제 사용 중인 아이콘만 1차 등록. 신규 사용 시점에 추가.
 */
interface IconMeta {
  src: string
  width: number
  height: number
  alt?: string
}

export const ICON_REGISTRY = {
  // ── 루트 (실제 사용 중) ───────────────────────────
  'allcheck-off': {
    src: '/images/icon-allcheck-off.png',
    width: 25,
    height: 25,
    alt: '전체 동의 선택 안됨',
  },
  'allcheck-on': {
    src: '/images/icon-allcheck-on.png',
    width: 25,
    height: 25,
    alt: '전체 동의 선택됨',
  },
  'calendar-next': { src: '/images/icon-calendar-next.png', width: 6, height: 10 },
  'calendar-prev': { src: '/images/icon-calendar-prev.png', width: 6, height: 10 },
  camera: { src: '/images/icon-camera.png', width: 23, height: 18, alt: '카메라' },
  'check-off': { src: '/images/icon-check-off.png', width: 14, height: 10, alt: '선택 안됨' },
  'check-on': { src: '/images/icon-check-on.png', width: 14, height: 10, alt: '선택됨' },
  'check-red': { src: '/images/icon-check-red.png', width: 49, height: 35, alt: '체크' },
  'circle-red': { src: '/images/icon-circle-red.png', width: 95, height: 95 },
  clear: { src: '/images/icon-clear.png', width: 15, height: 15, alt: '초기화' },
  close: { src: '/images/icon-close.png', width: 16, height: 16, alt: '닫기' },
  delete: { src: '/images/icon-delete.png', width: 9, height: 9, alt: '삭제' },
  'filter-nav': { src: '/images/icon-filter-nav.png', width: 10, height: 6 },
  kakao: { src: '/images/icon-kakao.png', width: 30, height: 30, alt: '카카오톡' },
  'logo-150': { src: '/images/icon-logo-150.png', width: 75, height: 87, alt: '테이스티하우스' },
  'nav-bottom-gray': { src: '/images/icon-nav-bottom-gray.png', width: 12, height: 8 },
  'nav-right': { src: '/images/icon-nav-right.svg', width: 6, height: 10 },
  notice: { src: '/images/icon-notice.png', width: 35, height: 35, alt: '알림' },
  'remove-image': { src: '/images/icon-remove-image.png', width: 15, height: 15, alt: '삭제' },
  search: { src: '/images/icon-search.png', width: 18, height: 18, alt: '검색' },
  'setting-black': { src: '/images/icon-setting-black.png', width: 18, height: 18, alt: '설정' },
  'toggle-off': { src: '/images/icon-toggle-off.png', width: 43, height: 24, alt: '꺼짐' },
  'toggle-on': { src: '/images/icon-toggle-on.png', width: 43, height: 24, alt: '켜짐' },

  // ── layout/ (Footer 메인 네비 5탭 × on/off) ──────
  // Footer는 fill 모드로 24×24 컨테이너 사용 — width/height는 sizes 힌트
  'layout/home-off': { src: '/images/layout/icon-home-off.png', width: 24, height: 24 },
  'layout/home-on': { src: '/images/layout/icon-home-on.png', width: 24, height: 24 },
  'layout/mypage-off': { src: '/images/layout/icon-mypage-off.png', width: 24, height: 24 },
  'layout/mypage-on': { src: '/images/layout/icon-mypage-on.png', width: 24, height: 24 },
  'layout/place-off': { src: '/images/layout/icon-place-off.png', width: 24, height: 24 },
  'layout/place-on': { src: '/images/layout/icon-place-on.png', width: 24, height: 24 },
  'layout/rank-off': { src: '/images/layout/icon-rank-off.png', width: 24, height: 24 },
  'layout/rank-on': { src: '/images/layout/icon-rank-on.png', width: 24, height: 24 },
  'layout/review-off': { src: '/images/layout/icon-review-off.png', width: 24, height: 24 },
  'layout/review-on': { src: '/images/layout/icon-review-on.png', width: 24, height: 24 },

  // ── account/profile/ ──────────────────────────────
  'account/profile/camera': {
    src: '/images/account/profile/icon-camera.png',
    width: 15,
    height: 12,
    alt: '카메라',
  },

  // ── home/ ─────────────────────────────────────────
  'home/fast-review': {
    src: '/images/home/icon-fast-reveiw.png',
    width: 26,
    height: 26,
    alt: '빠른 리뷰',
  },

  // ── mypage/ (알파벳 순: bookmark-off → bookmark-on → pen → review-on → setting) ──
  'mypage/bookmark-off': {
    src: '/images/mypage/icon-bookmark-off.png',
    width: 16,
    height: 24,
    alt: '찜',
  },
  'mypage/bookmark-on': {
    src: '/images/mypage/icon-bookmark-on.png',
    width: 16,
    height: 24,
    alt: '찜',
  },
  'mypage/pen': { src: '/images/mypage/icon-pen.png', width: 18, height: 16, alt: 'pencil' },
  'mypage/review-on': {
    src: '/images/mypage/icon-review-on.png',
    width: 22,
    height: 25,
    alt: '리뷰',
  },
  'mypage/setting': { src: '/images/mypage/icon-setting.png', width: 24, height: 24, alt: '설정' },

  // ── order/ ────────────────────────────────────────
  'order/cart-black': {
    src: '/images/order/icon-cart-black.png',
    width: 22,
    height: 22,
    alt: '장바구니',
  },

  // ── place/order-method/ (알파벳 순) ───────────────
  'place/order-method/delivery-off': {
    src: '/images/place/order-method/icon-delivery-off.png',
    width: 32,
    height: 32,
  },
  'place/order-method/delivery-on': {
    src: '/images/place/order-method/icon-delivery-on.png',
    width: 32,
    height: 32,
  },
  'place/order-method/packaging-off': {
    src: '/images/place/order-method/icon-packaging-off.png',
    width: 32,
    height: 32,
  },
  'place/order-method/packaging-on': {
    src: '/images/place/order-method/icon-packaging-on.png',
    width: 32,
    height: 32,
  },
  'place/order-method/reservation-off': {
    src: '/images/place/order-method/icon-reservation-off.png',
    width: 32,
    height: 32,
  },
  'place/order-method/reservation-on': {
    src: '/images/place/order-method/icon-reservation-on.png',
    width: 32,
    height: 32,
  },
  'place/order-method/table-off': {
    src: '/images/place/order-method/icon-table-off.png',
    width: 32,
    height: 32,
  },
  'place/order-method/table-on': {
    src: '/images/place/order-method/icon-table-on.png',
    width: 32,
    height: 32,
  },

  // ── product/ ──────────────────────────────────────
  'product/spiciness': {
    src: '/images/product/icon-spiciness.png',
    width: 9,
    height: 15,
    alt: '맵기',
  },

  // ── rank/ (level-XX-40 → level-XX-120 → rank-0X) ─
  'rank/level-01-40': { src: '/images/rank/icon-level-01-40.png', width: 40, height: 40 },
  'rank/level-02-40': { src: '/images/rank/icon-level-02-40.png', width: 40, height: 40 },
  'rank/level-03-40': { src: '/images/rank/icon-level-03-40.png', width: 40, height: 40 },
  'rank/level-04-40': { src: '/images/rank/icon-level-04-40.png', width: 40, height: 40 },
  'rank/level-05-40': { src: '/images/rank/icon-level-05-40.png', width: 40, height: 40 },
  'rank/level-01-120': { src: '/images/rank/icon-level-01-120.png', width: 120, height: 120 },
  'rank/level-02-120': { src: '/images/rank/icon-level-02-120.png', width: 120, height: 120 },
  'rank/level-03-120': { src: '/images/rank/icon-level-03-120.png', width: 120, height: 120 },
  'rank/level-04-120': { src: '/images/rank/icon-level-04-120.png', width: 120, height: 120 },
  'rank/level-05-120': { src: '/images/rank/icon-level-05-120.png', width: 120, height: 120 },
  'rank/rank-01': { src: '/images/rank/icon-rank-01.png', width: 70, height: 70, alt: '1등' },
  'rank/rank-02': { src: '/images/rank/icon-rank-02.png', width: 70, height: 70, alt: '2등' },
  'rank/rank-03': { src: '/images/rank/icon-rank-03.png', width: 70, height: 70, alt: '3등' },

  // ── today-discount/ ───────────────────────────────
  'today-discount/view-01': {
    src: '/images/today-discount/icon-view-01.png',
    width: 17,
    height: 17,
    alt: '그리드 뷰',
  },
  'today-discount/view-02': {
    src: '/images/today-discount/icon-view-02.png',
    width: 16,
    height: 13,
    alt: '리스트 뷰',
  },
} as const satisfies Record<string, IconMeta>

export type IconName = keyof typeof ICON_REGISTRY
