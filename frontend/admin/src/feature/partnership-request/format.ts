import type { PartnershipStatus } from "./domain";

// 처리 상태 한글 라벨
export function partnershipStatusLabel(status: PartnershipStatus): string {
  switch (status) {
    case "PENDING":
      return "접수 대기";
    case "IN_PROGRESS":
      return "처리 중";
    case "COMPLETED":
      return "처리 완료";
    default:
      return status;
  }
}

// 처리 상태 Badge variant
export function partnershipStatusBadgeVariant(
  status: PartnershipStatus,
): "default" | "secondary" | "outline" | "destructive" {
  switch (status) {
    case "COMPLETED":
      return "default";
    case "IN_PROGRESS":
      return "secondary";
    case "PENDING":
      return "outline";
    default:
      return "secondary";
  }
}
