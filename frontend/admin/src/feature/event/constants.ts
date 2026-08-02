import type { EventStatus } from "./domain";

// 이벤트 상태 옵션 (Select/필터 공용)
export const EVENT_STATUS_OPTIONS: { value: EventStatus; label: string }[] = [
  { value: "SCHEDULED", label: "예정" },
  { value: "ACTIVE", label: "진행중" },
  { value: "ENDED", label: "종료" },
];

// 폼 필드 최대 길이 제약
export const EVENT_NAME_MAX = 200;
export const EVENT_DESC_MAX = 1000;
export const EVENT_SUBTITLE_MAX = 200;
export const ANNOUNCEMENT_NAME_MAX = 200;
export const ANNOUNCEMENT_CONTENT_MAX = 1000;
export const WINNER_NAME_MAX = 50;
