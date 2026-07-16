// FAQ 카테고리 등록
export interface FaqCategoryCreateRequest {
  name: string;
  sort: number;
  visible: boolean;
}

// FAQ 카테고리 목록 조회
export interface FaqCategoryListItemResponse {
  id: number;
  name: string;
  sort: number;
  visible: boolean;
  createdAt: string;
}

// FAQ 카테고리 상세 조회
export interface FaqCategoryDetailResponse {
  id: number;
  name: string;
  sort: number;
  visible: boolean;
  createdAt: string;
  updatedAt: string;
}

// FAQ 카테고리 수정
export interface FaqCategoryUpdateRequest {
  name: string;
  sort: number;
  visible: boolean;
}

// FAQ 목록 조회
export interface FaqListQueryRequest {
  categoryId?: number;
  question?: string;
  visible?: boolean;
}

// FAQ 목록 조회 (answer 미포함)
export interface FaqListItemResponse {
  id: number;
  faqCategoryId: number;
  question: string;
  sort: number;
  visible: boolean;
  createdAt: string;
}

// FAQ 등록
export interface FaqCreateRequest {
  faqCategoryId: number;
  question: string;
  answer: string;
  sort: number;
  visible: boolean;
}

// FAQ 상세 조회
export interface FaqDetailResponse {
  id: number;
  faqCategoryId: number;
  question: string;
  answer: string;
  sort: number;
  visible: boolean;
  createdAt: string;
  updatedAt: string;
}

// FAQ 수정
export interface FaqUpdateRequest {
  faqCategoryId: number;
  question: string;
  answer: string;
  sort: number;
  visible: boolean;
}
