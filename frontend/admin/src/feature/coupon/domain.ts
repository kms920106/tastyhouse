export type DiscountType = "AMOUNT" | "RATE";

export interface CouponListItem {
  id: number;
  name: string;
  discountType: DiscountType;
  discountAmount: number;
  maxDiscountAmount: number | null;
  minOrderAmount: number;
  maxDiscountCount: number | null;
  issueStartAt: string;
  issueEndAt: string;
  useStartAt: string;
  useEndAt: string;
  visible: boolean;
}

export interface CouponDetail {
  id: number;
  name: string;
  description: string | null;
  discountType: DiscountType;
  discountAmount: number;
  maxDiscountAmount: number | null;
  minOrderAmount: number;
  maxDiscountCount: number | null;
  issueStartAt: string;
  issueEndAt: string;
  useStartAt: string;
  useEndAt: string;
  visible: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface MemberCouponItem {
  id: number;
  memberId: number;
  used: boolean;
  usedAt: string | null;
  expiredAt: string;
  issuedAt: string;
}
