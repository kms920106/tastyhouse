import type { EventStatus } from "./domain";

/** 이벤트 상태 한글 라벨 */
export function eventStatusLabel(status: EventStatus): string {
  switch (status) {
    case "SCHEDULED":
      return "예정";
    case "ACTIVE":
      return "진행중";
    case "ENDED":
      return "종료";
    default:
      return status;
  }
}

/** 이벤트 상태 Badge variant */
export function eventStatusBadgeVariant(status: EventStatus): "default" | "secondary" | "outline" {
  switch (status) {
    case "ACTIVE":
      return "default";
    case "SCHEDULED":
      return "outline";
    default:
      return "secondary";
  }
}
