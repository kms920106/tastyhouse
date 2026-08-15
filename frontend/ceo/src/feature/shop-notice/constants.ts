/** 공지 본문 최대 길이 */
export const NOTICE_CONTENT_MAX = 2000;

/** 공지 1건에 첨부할 수 있는 이미지 최대 장수 */
export const NOTICE_IMAGE_MAX_COUNT = 3;

export const NOTICE_IMAGE_MAX_BYTES = 10 * 1024 * 1024;

/**
 * 최소 해상도.
 *
 * 권장 크기(1280 × 560)의 절반을 하한으로 둔다 — 그보다 작으면 고객 화면에서 심하게 확대되어 깨진다.
 */
export const NOTICE_IMAGE_MIN_WIDTH = 640;
export const NOTICE_IMAGE_MIN_HEIGHT = 280;

export const NOTICE_IMAGE_RECOMMENDED = "권장 크기 1280 × 560 px";

/** 파일 선택 다이얼로그에서 허용할 MIME */
export const NOTICE_IMAGE_ACCEPT = "image/jpeg,image/png";
