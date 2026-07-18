import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  OptionCreateRequest,
  OptionGroupCreateRequest,
  ProductCategoryCreateRequest,
  ProductCategoryResponse,
  ProductCreateRequest,
  ProductDetailResponse,
  ProductImageCreateRequest,
  ProductImagesResponse,
  ProductListItemResponse,
  ProductListQueryRequest,
  ProductOptionGroupsResponse,
  ProductUpdateRequest,
} from "./product.dto";

/**
 * 상품 관리자 API
 */

const ENDPOINT = "/api/products";

export const productRepository = {
  // 상품 목록 조회
  getList(
    query: ProductListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<ProductListItemResponse[]>> {
    return api.get<ProductListItemResponse[]>(`${ENDPOINT}/v1`, {
      params: { ...query, ...pageRequest },
    });
  },

  // 상품 등록
  create(body: ProductCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1`, body);
  },

  // 상품 상세 조회
  getDetail(id: number): Promise<ApiResponse<ProductDetailResponse>> {
    return api.get<ProductDetailResponse>(`${ENDPOINT}/v1/${id}`);
  },

  // 상품 수정
  update(id: number, body: ProductUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/${id}`, body);
  },

  // 상품 품절 처리 (body 없음)
  soldOut(id: number): Promise<ApiResponse<null>> {
    return api.patch<null>(`${ENDPOINT}/v1/${id}/sold-out`);
  },

  // 상품 비활성화 (body 없음)
  deactivate(id: number): Promise<ApiResponse<null>> {
    return api.patch<null>(`${ENDPOINT}/v1/${id}/deactivate`);
  },

  // 상품 옵션 조회 (옵션 그룹 + 옵션)
  getOptions(id: number): Promise<ApiResponse<ProductOptionGroupsResponse>> {
    return api.get<ProductOptionGroupsResponse>(`${ENDPOINT}/v1/${id}/options`);
  },

  // 옵션 그룹 등록 (상품 ID 기준)
  createOptionGroup(id: number, body: OptionGroupCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${id}/option-groups`, body);
  },

  // 옵션 등록 — 경로가 상품 ID 가 아닌 옵션 그룹 ID(groupId) 기준임에 주의
  createOption(groupId: number, body: OptionCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/option-groups/${groupId}/options`, body);
  },

  // 상품 이미지 조회
  getImages(id: number): Promise<ApiResponse<ProductImagesResponse>> {
    return api.get<ProductImagesResponse>(`${ENDPOINT}/v1/${id}/images`);
  },

  // 상품 이미지 등록
  createImage(id: number, body: ProductImageCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${id}/images`, body);
  },

  // 상품 카테고리 목록 조회 (shopId 기준, 페이징 없음)
  getCategories(shopId: number): Promise<ApiResponse<ProductCategoryResponse[]>> {
    return api.get<ProductCategoryResponse[]>(`${ENDPOINT}/v1/categories`, {
      params: { shopId },
    });
  },

  // 상품 카테고리 등록
  createCategory(body: ProductCategoryCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/categories`, body);
  },
};
