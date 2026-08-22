// 상품 관리자 API 요청/응답 DTO (Product Admin — /api/products)
// DTO 는 이 계층 밖으로 나가지 않는다. UI/feature 는 @/feature/product/domain 을 사용한다.

// 상품 목록 조회 쿼리
export interface ProductListQueryRequest {
  shopId?: number;
  productCategoryId?: number;
  name?: string;
  visible?: boolean;
  soldOut?: boolean;
}

// 상품 목록 항목
export interface ProductListItemResponse {
  id: number;
  shopName: string;
  name: string;
  originalPrice: number;
  discountPrice: number | null;
  discountRate: number | null;
  representative: boolean;
  soldOut: boolean;
  visible: boolean;
  sort: number;
}

// 상품 등록
export interface ProductCreateRequest {
  shopId: number;
  productCategoryId?: number;
  name: string;
  description?: string;
  originalPrice: number;
  discountPrice?: number;
  discountRate?: number;
  rating?: number;
  reviewCount?: number;
  representative: boolean;
  spiciness?: number;
  soldOut: boolean;
  visible: boolean;
  sort: number;
}

// 상품 수정 (등록과 동일, shopId 제외)
export interface ProductUpdateRequest {
  productCategoryId?: number;
  name: string;
  description?: string;
  originalPrice: number;
  discountPrice?: number;
  discountRate?: number;
  rating?: number;
  reviewCount?: number;
  representative: boolean;
  spiciness?: number;
  soldOut: boolean;
  visible: boolean;
  sort: number;
}

// 상품 상세
export interface ProductDetailResponse {
  id: number;
  shopId: number;
  productCategoryId: number | null;
  name: string;
  description: string | null;
  originalPrice: number;
  discountPrice: number | null;
  discountRate: number | null;
  rating: number | null;
  reviewCount: number | null;
  representative: boolean;
  spiciness: number | null;
  soldOut: boolean;
  visible: boolean;
  sort: number;
  createdAt: string;
  updatedAt: string;
}

// 옵션그룹 유형 — NORMAL(일반) / CUP_DEPOSIT(일회용컵 보증금). ProductOptionType(NORMAL/COMMON, 요청 전용)과는 다른 축이다.
export type OptionGroupType = "NORMAL" | "CUP_DEPOSIT";

// 옵션
export interface OptionResponse {
  id: number;
  name: string;
  additionalPrice: number;
  soldOut: boolean;
  /** 일회용컵 제공 개수(1~10) — CUP_DEPOSIT 그룹의 보증금 옵션에만 존재. 그 외에는 null */
  cupCount: number | null;
  /** 보증금액(cupCount × 정책 요율) — 서버가 계산해 내려준다. 보증금 옵션이 아니면 null */
  depositAmount: number | null;
  /** 개인컵 사용 시 할인 금액 — 개인컵 옵션에만 존재. 그 외에는 null */
  personalCupDiscountAmount: number | null;
}

// 옵션 그룹 (options 배열 포함, common=true 는 공통 그룹)
export interface OptionGroupResponse {
  id: number;
  name: string;
  description: string | null;
  required: boolean;
  multipleSelect: boolean;
  minSelect: number;
  maxSelect: number;
  common: boolean;
  groupType: OptionGroupType;
  options: OptionResponse[];
}

// 상품 옵션 조회
export interface ProductOptionGroupsResponse {
  optionGroups: OptionGroupResponse[];
}

// 옵션 그룹 등록
export interface OptionGroupCreateRequest {
  name: string;
  description?: string;
  required: boolean;
  multipleSelect: boolean;
  minSelect?: number;
  maxSelect?: number;
  sort: number;
  visible: boolean;
}

// 옵션 등록
export interface OptionCreateRequest {
  name: string;
  additionalPrice: number;
  sort: number;
  soldOut: boolean;
  visible: boolean;
}

// 상품 이미지 조회
export interface ProductImagesResponse {
  imageUrls: string[];
}

// 상품 이미지 등록
export interface ProductImageCreateRequest {
  imageFileId: number;
  sort: number;
  visible: boolean;
}

// 상품 카테고리
export interface ProductCategoryResponse {
  id: number;
  shopId: number;
  name: string;
  sort: number;
  visible: boolean;
}

// 상품 카테고리 등록
export interface ProductCategoryCreateRequest {
  shopId: number;
  name: string;
  sort: number;
  visible: boolean;
}

// ===== 메뉴 검수 (이미지 변경 요청 · 채식 설정 요청) =====

// 승인 상태 — backend ApprovalStatus enum 기준
export type ApprovalStatusValue = "PENDING" | "APPROVED" | "REJECTED" | "CANCELED";

// 채식 단계 — backend VegetarianType enum 기준
export type VegetarianTypeValue = "VEGAN" | "LACTO" | "OVO" | "LACTO_OVO" | "PESCO";

// 검수 목록 조회 쿼리 — status 미지정은 전체
export interface ProductApprovalSearchRequest {
  status?: ApprovalStatusValue;
}

// 메뉴 이미지 변경 요청 목록 항목
export interface ProductImageChangeRequestItemResponse {
  id: number;
  productId: number;
  shopId: number;
  productName: string;
  imageUrl: string | null;
  status: ApprovalStatusValue;
  rejectReason: string | null;
}

// 메뉴 채식 설정 요청 목록 항목
export interface ProductVegetarianRequestItemResponse {
  id: number;
  productId: number;
  shopId: number;
  productName: string;
  vegetarianType: VegetarianTypeValue;
  ingredients: string;
  description: string | null;
  status: ApprovalStatusValue;
  rejectReason: string | null;
}

// 승인요청 반려
export interface ProductApprovalRejectRequest {
  rejectReason: string;
}
