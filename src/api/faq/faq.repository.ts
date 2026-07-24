import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  FaqCategoryCreateRequest,
  FaqCategoryDetailResponse,
  FaqCategoryListItemResponse,
  FaqCategoryUpdateRequest,
  FaqCreateRequest,
  FaqDetailResponse,
  FaqListItemResponse,
  FaqListQueryRequest,
  FaqUpdateRequest,
} from "./faq.dto";

/**
 * FAQ 관리자 API
 */

const ENDPOINT = "/api/faqs";

export const faqRepository = {
  // FAQ 카테고리 목록 조회
  getCategories(): Promise<ApiResponse<FaqCategoryListItemResponse[]>> {
    return api.get<FaqCategoryListItemResponse[]>(`${ENDPOINT}/v1/categories`);
  },

  // FAQ 카테고리 등록
  createCategory(body: FaqCategoryCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/categories`, body);
  },

  // FAQ 카테고리 상세 조회
  getCategory(categoryId: number): Promise<ApiResponse<FaqCategoryDetailResponse>> {
    return api.get<FaqCategoryDetailResponse>(`${ENDPOINT}/v1/categories/${categoryId}`);
  },

  // FAQ 카테고리 수정
  updateCategory(categoryId: number, body: FaqCategoryUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/categories/${categoryId}`, body);
  },

  // FAQ 카테고리 삭제
  removeCategory(categoryId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/categories/${categoryId}`);
  },

  // FAQ 목록 조회
  getList(query: FaqListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<FaqListItemResponse[]>> {
    return api.get<FaqListItemResponse[]>(`${ENDPOINT}/v1`, {
      params: { ...query, ...pageRequest },
    });
  },

  // FAQ 등록
  create(body: FaqCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1`, body);
  },

  // FAQ 상세 조회
  getDetail(id: number): Promise<ApiResponse<FaqDetailResponse>> {
    return api.get<FaqDetailResponse>(`${ENDPOINT}/v1/${id}`);
  },

  // FAQ 수정
  update(id: number, body: FaqUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/${id}`, body);
  },

  // FAQ 삭제
  remove(id: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/${id}`);
  },
};
