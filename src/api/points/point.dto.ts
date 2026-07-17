// 포인트 유형: EARNED(적립) / USE(사용) / REFUND(환불)
export type PointType = "EARNED" | "USE" | "REFUND";

// 포인트 잔액 조회
export interface PointBalanceResponse {
  memberId: number;
  availablePoints: number;
  expiredThisMonth: number;
}

// 포인트 이력 항목
export interface PointHistoryItemResponse {
  pointType: PointType;
  pointAmount: number;
  reason: string;
  createdAt: string;
}

// 포인트 이력 조회 쿼리
export interface PointHistoryQueryRequest {
  type?: PointType;
}

// 포인트 수동 적립
export interface PointEarnRequest {
  amount: number;
  reason: string;
}

// 포인트 수동 차감
export interface PointDeductRequest {
  amount: number;
  reason: string;
}
