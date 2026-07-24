import type { VariantProps } from "class-variance-authority";

import type { badgeVariants } from "@/components/ui/badge";

type BadgeVariant = VariantProps<typeof badgeVariants>["variant"];

/** 처리 상태 (검색: 5종 전부, 변경: RECEIVED 제외 4종) */
export const BUG_STATUS = ["RECEIVED", "IN_PROGRESS", "RESOLVED", "REJECTED", "ON_HOLD"] as const;
export type BugStatus = (typeof BUG_STATUS)[number];

export const BUG_STATUS_LABEL: Record<BugStatus, string> = {
  RECEIVED: "접수",
  IN_PROGRESS: "처리중",
  RESOLVED: "해결",
  REJECTED: "반려",
  ON_HOLD: "보류",
};

/** 상태 '변경' 허용값 — RECEIVED 로의 되돌림 불가 */
export const BUG_STATUS_TRANSITIONS = ["IN_PROGRESS", "RESOLVED", "REJECTED", "ON_HOLD"] as const;
export type BugStatusTransition = (typeof BUG_STATUS_TRANSITIONS)[number];

export const BUG_STATUS_BADGE_VARIANT: Record<BugStatus, BadgeVariant> = {
  RECEIVED: "secondary",
  IN_PROGRESS: "default",
  RESOLVED: "default",
  REJECTED: "destructive",
  ON_HOLD: "outline",
};

/** 분류 */
export const BUG_CATEGORY = ["PAYMENT", "LOGIN", "ORDER", "RESERVATION", "UI", "PERFORMANCE", "ETC"] as const;
export type BugCategory = (typeof BUG_CATEGORY)[number];

export const BUG_CATEGORY_LABEL: Record<BugCategory, string> = {
  PAYMENT: "결제",
  LOGIN: "로그인",
  ORDER: "주문",
  RESERVATION: "예약",
  UI: "UI",
  PERFORMANCE: "성능",
  ETC: "기타",
};

/** 우선순위 */
export const BUG_PRIORITY = ["LOW", "MEDIUM", "HIGH", "CRITICAL"] as const;
export type BugPriority = (typeof BUG_PRIORITY)[number];

export const BUG_PRIORITY_LABEL: Record<BugPriority, string> = {
  LOW: "낮음",
  MEDIUM: "보통",
  HIGH: "높음",
  CRITICAL: "긴급",
};

export const BUG_PRIORITY_BADGE_VARIANT: Record<BugPriority, BadgeVariant> = {
  LOW: "secondary",
  MEDIUM: "outline",
  HIGH: "default",
  CRITICAL: "destructive",
};

/** 플랫폼 (응답 전용) */
export const BUG_PLATFORM_LABEL: Record<string, string> = {
  IOS: "iOS",
  ANDROID: "Android",
};
