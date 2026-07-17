import type { PartnershipStatus } from "./domain";

// 처리 상태 옵션 (Select/필터 공용)
export const PARTNERSHIP_STATUS_OPTIONS: { value: PartnershipStatus; label: string }[] = [
  { value: "PENDING", label: "접수 대기" },
  { value: "IN_PROGRESS", label: "처리 중" },
  { value: "COMPLETED", label: "처리 완료" },
];
