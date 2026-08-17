import { Badge } from "@/components/ui/badge";

/**
 * 요청·신청의 처리 상태를 배지로 렌더한다.
 *
 * 라벨은 매핑하지 않고 **서버가 준 한국어 라벨을 그대로 받는다**(`label` prop) —
 * 상태 문자열의 한글화는 서버 카탈로그의 몫이고, 이 컴포넌트가 아는 것은
 * "어떤 상태를 어떤 색으로 보일지"뿐이다. 그래서 백엔드에 상태가 추가돼도
 * 여기서는 기본값(`outline`)으로 안전하게 떨어진다.
 *
 * 원래 `delivery-area-adjustment-sheet.tsx` 의 로컬 `statusBadgeVariant` 였고,
 * 요청처리 현황 화면이 두 번째 사용처가 되어 `src/components/` 로 승격했다.
 */

/**
 * 상태 코드 → 배지 variant.
 *
 * 통합 상태(`docs/tasks/backend.md` 2-2)의 5종에 더해 `COMPLETED` 도 승인과 같은 색으로 둔다 —
 * 배달지역 조정의 원본 enum 은 완료를 `COMPLETED` 로 부르고 통합 상태만 이를 `APPROVED` 로
 * 접어 넣으므로, 원본 enum 을 그대로 쓰는 조정 신청 시트도 같은 표를 쓸 수 있어야 한다.
 *
 * 계정 단위 이력(`docs/tasks/backend.md` §2-1·§2-2)의 결과·조치 유형도 같은 표에 들어온다 —
 * 요청 처리 상태와 값이 겹치지 않으므로 별도 컴포넌트를 만들지 않는다.
 */
const STATUS_VARIANT: Record<string, "default" | "destructive" | "secondary" | "outline"> = {
  APPROVED: "default",
  COMPLETED: "default",
  REJECTED: "destructive",
  PENDING: "secondary",
  IN_PROGRESS: "secondary",
  CANCELED: "outline",
  // 리뷰 게시중단 생애주기의 종결 상태(`docs/tasks/backend.md` 1-1) — 승인 이후의 결말이라
  // 심사 결과(승인/반려)와 색을 구분해 중립으로 둔다.
  EXPIRED: "outline",
  DELETED: "outline",
  // 개인정보 접속기록 — 로그인 결과
  SUCCESS: "default",
  FAILURE: "destructive",
  // 시스템 접근권한 이력 — 조치 유형. 말소는 되돌리기 어려운 조치라 성공/실패와 같은 색 구분을 쓴다.
  GRANT: "default",
  REVOKE: "destructive",
};

interface StatusBadgeProps {
  /** 상태 코드. 표에 없으면 `outline` 으로 떨어진다 */
  status: string;
  /** 서버가 내려준 한국어 라벨 */
  label: string;
  className?: string;
}

export function StatusBadge({ status, label, className }: StatusBadgeProps) {
  return (
    <Badge variant={STATUS_VARIANT[status] ?? "outline"} className={className}>
      {label}
    </Badge>
  );
}
