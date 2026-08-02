export type PointType = "EARNED" | "USE" | "REFUND";

export interface PointBalance {
  memberId: number;
  availablePoints: number;
  expiredThisMonth: number;
}

export interface PointHistoryItem {
  pointType: PointType;
  pointAmount: number;
  reason: string;
  createdAt: string;
}
