import type { MemberGrade, MemberStatus, WithdrawalReason } from "./domain";

// 회원 상태 옵션 (Select/필터 공용)
export const MEMBER_STATUS_OPTIONS: { value: MemberStatus; label: string }[] = [
  { value: "ACTIVE", label: "활성" },
  { value: "SUSPENDED", label: "정지" },
  { value: "DELETED", label: "탈퇴" },
];

// 회원 등급 옵션 (Select/필터 공용)
export const MEMBER_GRADE_OPTIONS: { value: MemberGrade; label: string }[] = [
  { value: "NEWCOMER", label: "뉴커머" },
  { value: "ACTIVE", label: "액티브" },
  { value: "INSIDER", label: "인사이더" },
  { value: "GOURMET", label: "구르메" },
  { value: "TEHA", label: "테하" },
];

// 강제 탈퇴 사유 옵션
export const WITHDRAWAL_REASON_OPTIONS: { value: WithdrawalReason; label: string }[] = [
  { value: "LOW_USAGE_FREQUENCY", label: "서비스 이용 빈도가 낮아서" },
  { value: "INSUFFICIENT_CONTENT", label: "콘텐츠가 부족해서" },
  { value: "SWITCH_TO_ANOTHER_SERVICE", label: "다른 서비스로 이동" },
  { value: "PRIVACY_CONCERNS", label: "개인정보 보호 우려" },
  { value: "OTHER", label: "기타" },
];

// 탈퇴 사유 상세 최대 길이
export const REASON_DETAIL_MAX = 500;
