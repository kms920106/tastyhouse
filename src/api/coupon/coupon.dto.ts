// 할인 유형: AMOUNT(정액, 원) / RATE(정률, %)
export type DiscountType = "AMOUNT" | "RATE";

// 쿠폰 목록 조회
export interface CouponListQueryRequest {
  name?: string;
  discountType?: DiscountType;
  visible?: boolean;
}

// 쿠폰 목록 조회
export interface CouponListItemResponse {
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

// 쿠폰 등록
export interface CouponCreateRequest {
  name: string;
  description?: string;
  discountType: DiscountType;
  discountAmount: number;
  maxDiscountAmount?: number;
  minOrderAmount?: number;
  maxDiscountCount?: number;
  issueStartAt: string;
  issueEndAt: string;
  useStartAt: string;
  useEndAt: string;
  visible?: boolean;
}

// 쿠폰 상세 조회
export interface CouponDetailResponse {
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

// 쿠폰 수정 (등록과 동일, visible 필수)
export interface CouponUpdateRequest {
  name: string;
  description?: string;
  discountType: DiscountType;
  discountAmount: number;
  maxDiscountAmount?: number;
  minOrderAmount?: number;
  maxDiscountCount?: number;
  issueStartAt: string;
  issueEndAt: string;
  useStartAt: string;
  useEndAt: string;
  visible: boolean;
}

// 쿠폰 회원 발급
export interface CouponIssueRequest {
  memberId: number;
}

// 쿠폰 발급 현황 조회
export interface MemberCouponAdminItemResponse {
  id: number;
  memberId: number;
  used: boolean;
  usedAt: string | null;
  expiredAt: string;
  issuedAt: string;
}
