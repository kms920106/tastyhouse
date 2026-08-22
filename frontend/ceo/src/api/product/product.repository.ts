import "server-only";

import { api } from "@/api/shared/client";
import type { ApiResponse } from "@/api/shared/types";

import type {
  ProductAvailabilityChangeResponse,
  ProductAvailabilityGroupResponse,
  ProductAvailabilitySearchRequest,
  // ===== 메뉴·옵션 관리 =====
  ProductCategoryMoveRequest,
  ProductCategoryOrderRequest,
  ProductCategoryResponse,
  ProductCategoryUpdateRequest,
  ProductCreateRequest,
  ProductDeleteRequest,
  ProductDetailResponse,
  ProductExposureRequest,
  ProductExposureResponse,
  ProductHiddenRequest,
  ProductImageListResponse,
  ProductImageSortRequest,
  ProductOptionAvailabilityGroupResponse,
  ProductOptionGroupLinkedProductResponse,
  ProductOptionGroupLinkedProductsResponse,
  ProductOptionGroupMergeExclusionRequest,
  ProductOptionGroupMergePreviewParams,
  ProductOptionGroupMergePreviewResponse,
  ProductOptionGroupMergeRequest,
  ProductOptionGroupMergeSuggestionResponse,
  ProductOptionGroupResponse,
  ProductOptionGroupSaveRequest,
  ProductOptionGroupSortRequest,
  ProductOptionHiddenRequest,
  ProductOptionReleaseRequest,
  ProductOptionSaveRequest,
  ProductOptionSoldOutRequest,
  ProductOptionSoldOutUntilRequest,
  ProductOptionSortRequest,
  ProductOrderRequest,
  ProductReleaseRequest,
  ProductSoldOutRequest,
  ProductSoldOutUntilRequest,
  ProductUpdateRequest,
  ProductVegetarianRequestBody,
  ProductVegetarianResponse,
} from "./product.dto";

/**
 * 점주 품절·숨김 설정 API (transport only)
 *
 * 하위 리소스 경로 규칙은 리소스마다 다르므로(`src/api/AGENTS.md`) 일반화하지 않고
 * `docs/tasks/backend.md` §3-3 을 그대로 반영한다.
 * - **`shopId` 가 경로가 아니라 query/body 에 있다.** 리뷰(`/api/shops/v1/{shopId}/reviews`)와 달리
 *   이 리소스는 메뉴 기준(`/api/products`)이라 가게가 경로 세그먼트가 아니다.
 * - 옵션 4종은 메뉴 4종과 동형이며 `/options` 접두 세그먼트 하나만 더 붙는다.
 */

const ENDPOINT = "/api/products";

const VERSION_PATH = `${ENDPOINT}/v1`;
const CATEGORY_PATH = `${VERSION_PATH}/categories`;
const OPTION_GROUP_PATH = `${VERSION_PATH}/option-groups`;
const OPTION_GROUP_MERGE_SUGGESTION_PATH = `${OPTION_GROUP_PATH}/merge-suggestions`;

const AVAILABILITY_PATH = `${ENDPOINT}/v1/availability`;
const OPTION_AVAILABILITY_PATH = `${AVAILABILITY_PATH}/options`;

/**
 * boolean 필터는 **체크됐을 때만 실어보낸다.**
 *
 * `false` 를 명시적으로 보내면 백엔드 동적 where 가 `= false` 조건을 걸어 "미지정 = 전체"와
 * 결과가 달라진다. 공유 클라이언트는 `null`/`undefined` 만 건너뛰므로 여기서 걸러 낸다.
 */
function toSearchParams(request: ProductAvailabilitySearchRequest): Record<string, string | number> {
  const params: Record<string, string | number> = { shopId: request.shopId };
  if (request.keyword) params.keyword = request.keyword;
  if (request.soldOutOnly) params.soldOutOnly = "true";
  if (request.hiddenOnly) params.hiddenOnly = "true";
  return params;
}

export const productRepository = {
  getAvailability(request: ProductAvailabilitySearchRequest): Promise<ApiResponse<ProductAvailabilityGroupResponse[]>> {
    return api.get<ProductAvailabilityGroupResponse[]>(AVAILABILITY_PATH, { params: toSearchParams(request) });
  },

  getOptionAvailability(
    request: ProductAvailabilitySearchRequest,
  ): Promise<ApiResponse<ProductOptionAvailabilityGroupResponse[]>> {
    return api.get<ProductOptionAvailabilityGroupResponse[]>(OPTION_AVAILABILITY_PATH, {
      params: toSearchParams(request),
    });
  },

  // ===== 메뉴 =====

  markSoldOut(body: ProductSoldOutRequest): Promise<ApiResponse<ProductAvailabilityChangeResponse>> {
    return api.patch<ProductAvailabilityChangeResponse>(`${AVAILABILITY_PATH}/sold-out`, body);
  },

  hide(body: ProductHiddenRequest): Promise<ApiResponse<ProductAvailabilityChangeResponse>> {
    return api.patch<ProductAvailabilityChangeResponse>(`${AVAILABILITY_PATH}/hidden`, body);
  },

  release(body: ProductReleaseRequest): Promise<ApiResponse<ProductAvailabilityChangeResponse>> {
    return api.patch<ProductAvailabilityChangeResponse>(`${AVAILABILITY_PATH}/release`, body);
  },

  changeSoldOutUntil(body: ProductSoldOutUntilRequest): Promise<ApiResponse<ProductAvailabilityChangeResponse>> {
    return api.patch<ProductAvailabilityChangeResponse>(`${AVAILABILITY_PATH}/sold-out-until`, body);
  },

  // ===== 옵션 =====

  markOptionsSoldOut(body: ProductOptionSoldOutRequest): Promise<ApiResponse<ProductAvailabilityChangeResponse>> {
    return api.patch<ProductAvailabilityChangeResponse>(`${OPTION_AVAILABILITY_PATH}/sold-out`, body);
  },

  hideOptions(body: ProductOptionHiddenRequest): Promise<ApiResponse<ProductAvailabilityChangeResponse>> {
    return api.patch<ProductAvailabilityChangeResponse>(`${OPTION_AVAILABILITY_PATH}/hidden`, body);
  },

  releaseOptions(body: ProductOptionReleaseRequest): Promise<ApiResponse<ProductAvailabilityChangeResponse>> {
    return api.patch<ProductAvailabilityChangeResponse>(`${OPTION_AVAILABILITY_PATH}/release`, body);
  },

  changeOptionsSoldOutUntil(
    body: ProductOptionSoldOutUntilRequest,
  ): Promise<ApiResponse<ProductAvailabilityChangeResponse>> {
    return api.patch<ProductAvailabilityChangeResponse>(`${OPTION_AVAILABILITY_PATH}/sold-out-until`, body);
  },

  // =====================================================================================
  // 메뉴·옵션 관리 (`docs/tasks/backend.md` §2~§7)
  //
  // 경로 규칙이 리소스마다 다르므로 일반화하지 않고 스펙을 그대로 옮긴다.
  // `shopId` 는 조회에서 query, 변경에서 body 로 간다 — 서버가 그렇게 받는다.
  // =====================================================================================

  // ===== 메뉴그룹 (§3) =====

  getCategories(shopId: number): Promise<ApiResponse<ProductCategoryResponse[]>> {
    return api.get<ProductCategoryResponse[]>(CATEGORY_PATH, { params: { shopId } });
  },

  createCategory(body: ProductCategoryUpdateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(CATEGORY_PATH, body);
  },

  updateCategory(id: number, body: ProductCategoryUpdateRequest): Promise<ApiResponse<void>> {
    return api.put<void>(`${CATEGORY_PATH}/${id}`, body);
  },

  /** 삭제도 `shopId` 를 body 로 보낸다 — 소유권 검증에 필요하고 경로에 가게가 없다 */
  deleteCategory(id: number, shopId: number): Promise<ApiResponse<void>> {
    return api.delete<void>(`${CATEGORY_PATH}/${id}`, { shopId });
  },

  // ===== 메뉴 CRUD (§2) =====

  /**
   * 메뉴 상세.
   *
   * **`docs/tasks/backend.md` 에 이 엔드포인트가 없다.** §2 는 등록(POST)·수정(PUT)·삭제(DELETE)만
   * 정의하는데, `frontend.md` §4 의 메뉴 상세 화면은 단건 조회 없이는 성립하지 않는다.
   * 두 스펙 문서 사이의 공백이므로 §2-2 의 수정 경로(`PUT /api/products/v1/{id}`) 규칙을 그대로 따라
   * `GET /api/products/v1/{id}?shopId=` 로 가정했다. **backend 구현 시 이 경로를 확정해야 한다.**
   */
  getProductDetail(id: number, shopId: number): Promise<ApiResponse<ProductDetailResponse>> {
    return api.get<ProductDetailResponse>(`${VERSION_PATH}/${id}`, { params: { shopId } });
  },

  createProduct(body: ProductCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(VERSION_PATH, body);
  },

  updateProduct(id: number, body: ProductUpdateRequest): Promise<ApiResponse<void>> {
    return api.put<void>(`${VERSION_PATH}/${id}`, body);
  },

  /** 삭제는 일괄이고 **HTTP 200 + 부분실패**로 돌아온다(§2-3) */
  deleteProducts(body: ProductDeleteRequest): Promise<ApiResponse<ProductAvailabilityChangeResponse>> {
    return api.delete<ProductAvailabilityChangeResponse>(VERSION_PATH, body);
  },

  // ===== 순서 변경 (§4) =====

  changeCategoryOrder(body: ProductCategoryOrderRequest): Promise<ApiResponse<void>> {
    return api.put<void>(`${CATEGORY_PATH}/order`, body);
  },

  changeProductOrder(body: ProductOrderRequest): Promise<ApiResponse<void>> {
    return api.put<void>(`${VERSION_PATH}/order`, body);
  },

  /** 그룹 이동. 도착 그룹의 최종 순서까지 함께 보내야 놓은 위치가 보존된다 */
  moveProductCategory(body: ProductCategoryMoveRequest): Promise<ApiResponse<void>> {
    return api.put<void>(`${VERSION_PATH}/category`, body);
  },

  // ===== 옵션그룹 · 옵션 (§5) =====

  getOptionGroups(shopId: number): Promise<ApiResponse<ProductOptionGroupResponse[]>> {
    return api.get<ProductOptionGroupResponse[]>(OPTION_GROUP_PATH, { params: { shopId } });
  },

  createOptionGroup(body: ProductOptionGroupSaveRequest): Promise<ApiResponse<number>> {
    return api.post<number>(OPTION_GROUP_PATH, body);
  },

  updateOptionGroup(id: number, body: ProductOptionGroupSaveRequest): Promise<ApiResponse<void>> {
    return api.put<void>(`${OPTION_GROUP_PATH}/${id}`, body);
  },

  deleteOptionGroup(id: number, shopId: number): Promise<ApiResponse<void>> {
    return api.delete<void>(`${OPTION_GROUP_PATH}/${id}`, { shopId });
  },

  createOption(optionGroupId: number, body: ProductOptionSaveRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${OPTION_GROUP_PATH}/${optionGroupId}/options`, body);
  },

  updateOption(id: number, body: ProductOptionSaveRequest): Promise<ApiResponse<void>> {
    return api.put<void>(`${VERSION_PATH}/options/${id}`, body);
  },

  deleteOption(id: number, shopId: number): Promise<ApiResponse<void>> {
    return api.delete<void>(`${VERSION_PATH}/options/${id}`, { shopId });
  },

  changeOptionOrder(optionGroupId: number, body: ProductOptionSortRequest): Promise<ApiResponse<void>> {
    return api.put<void>(`${OPTION_GROUP_PATH}/${optionGroupId}/options/sort`, body);
  },

  // ===== 메뉴-옵션그룹 연결 (§5-2) =====

  linkOptionGroup(productId: number, optionGroupId: number, shopId: number): Promise<ApiResponse<void>> {
    return api.post<void>(`${VERSION_PATH}/${productId}/option-groups/${optionGroupId}`, { shopId });
  },

  /** 컨트롤러가 `@ModelAttribute`로 받아 `shopId`는 query로 보낸다 */
  unlinkOptionGroup(productId: number, optionGroupId: number, shopId: number): Promise<ApiResponse<void>> {
    return api.delete<void>(`${VERSION_PATH}/${productId}/option-groups/${optionGroupId}`, undefined, {
      params: { shopId },
    });
  },

  changeLinkedOptionGroupOrder(productId: number, body: ProductOptionGroupSortRequest): Promise<ApiResponse<void>> {
    return api.put<void>(`${VERSION_PATH}/${productId}/option-groups/sort`, body);
  },

  /** 해제 전 영향 확인 — 이 그룹을 쓰는 다른 메뉴가 몇 개인지 보여준다 */
  getOptionGroupLinkedProducts(
    optionGroupId: number,
    shopId: number,
  ): Promise<ApiResponse<ProductOptionGroupLinkedProductResponse[]>> {
    return api.get<ProductOptionGroupLinkedProductResponse[]>(`${OPTION_GROUP_PATH}/${optionGroupId}/products`, {
      params: { shopId },
    });
  },

  /**
   * 가게 옵션그룹 전체의 연결 메뉴 목록을 한 번에 조회한다 — 옵션그룹 연결 다이얼로그가 후보 그룹마다
   * {@link getOptionGroupLinkedProducts}를 개별 호출하던 N+1을 피하기 위한 벌크 조회.
   */
  getOptionGroupsLinkedProducts(shopId: number): Promise<ApiResponse<ProductOptionGroupLinkedProductsResponse[]>> {
    return api.get<ProductOptionGroupLinkedProductsResponse[]>(`${OPTION_GROUP_PATH}/products`, {
      params: { shopId },
    });
  },

  // ===== 옵션그룹 합치기 (§2-6) =====

  /** 추천 합치기 목록. 0건이면 진입 배너를 숨겨야 하므로 빈 배열도 정상 응답이다 */
  getOptionGroupMergeSuggestions(shopId: number): Promise<ApiResponse<ProductOptionGroupMergeSuggestionResponse[]>> {
    return api.get<ProductOptionGroupMergeSuggestionResponse[]>(OPTION_GROUP_MERGE_SUGGESTION_PATH, {
      params: { shopId },
    });
  },

  /** [X] 제외 — 재클릭은 멱등이며 기존 exclusion id 를 돌려준다 */
  excludeOptionGroupMergeSuggestion(body: ProductOptionGroupMergeExclusionRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${OPTION_GROUP_MERGE_SUGGESTION_PATH}/exclusions`, body);
  },

  /**
   * 기준 선택 후 diff 미리보기.
   *
   * `optionGroupIds` 는 배열이라 공유 클라이언트가 **반복 query 파라미터**로 직렬화한다
   * (`?optionGroupIds=1&optionGroupIds=2`) — 서버가 `List<Long>` 으로 바인딩하는 형태다.
   */
  getOptionGroupMergePreview(
    params: ProductOptionGroupMergePreviewParams,
  ): Promise<ApiResponse<ProductOptionGroupMergePreviewResponse>> {
    return api.get<ProductOptionGroupMergePreviewResponse>(`${OPTION_GROUP_PATH}/merge-preview`, { params });
  },

  /** 합치기 실행. 경로 `{baseOptionGroupId}` 가 **살아남는** 기준 그룹이다 */
  mergeOptionGroups(baseOptionGroupId: number, body: ProductOptionGroupMergeRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${OPTION_GROUP_PATH}/${baseOptionGroupId}/merge`, body);
  },

  // ===== 노출기간 (§6) =====

  getExposure(productId: number, shopId: number): Promise<ApiResponse<ProductExposureResponse>> {
    return api.get<ProductExposureResponse>(`${VERSION_PATH}/${productId}/exposure`, { params: { shopId } });
  },

  /** 기간·요일·시간대 전체 치환(replace-all) */
  changeExposure(productId: number, body: ProductExposureRequest): Promise<ApiResponse<void>> {
    return api.put<void>(`${VERSION_PATH}/${productId}/exposure`, body);
  },

  /** 스케줄 해제 → 상시 노출로 복귀. 컨트롤러가 `@ModelAttribute`로 받아 `shopId`는 query로 보낸다 */
  clearExposure(productId: number, shopId: number): Promise<ApiResponse<void>> {
    return api.delete<void>(`${VERSION_PATH}/${productId}/exposure`, undefined, { params: { shopId } });
  },

  // ===== 이미지 (§7-1) =====

  getProductImages(productId: number, shopId: number): Promise<ApiResponse<ProductImageListResponse>> {
    return api.get<ProductImageListResponse>(`${VERSION_PATH}/${productId}/images`, { params: { shopId } });
  },

  /**
   * 이미지 등록 요청 (multipart).
   *
   * 파일을 `fileId` 로 먼저 올리지 않고 원본을 그대로 보낸다 — 서버가 `ImageIO` 로 실제 해상도를
   * 검증한 뒤(`PRODUCT_IMAGE_SPEC_INVALID`) 통과분만 저장하기 때문이다(`backend.md` §7-1).
   */
  requestProductImage(productId: number, shopId: number, file: File): Promise<ApiResponse<number>> {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("shopId", String(shopId));
    return api.upload<number>(`${VERSION_PATH}/${productId}/images`, formData);
  },

  /** 순서 변경은 승인 대상이 아니라 즉시 반영된다 */
  changeProductImageOrder(productId: number, body: ProductImageSortRequest): Promise<ApiResponse<void>> {
    return api.put<void>(`${VERSION_PATH}/${productId}/images/sort`, body);
  },

  /** 컨트롤러가 `@ModelAttribute`로 받아 `shopId`는 query로 보낸다 */
  deleteProductImage(imageId: number, shopId: number): Promise<ApiResponse<void>> {
    return api.delete<void>(`${VERSION_PATH}/images/${imageId}`, undefined, { params: { shopId } });
  },

  // ===== 채식 (§7-1) =====

  getVegetarian(productId: number, shopId: number): Promise<ApiResponse<ProductVegetarianResponse>> {
    return api.get<ProductVegetarianResponse>(`${VERSION_PATH}/${productId}/vegetarian`, { params: { shopId } });
  },

  requestVegetarian(productId: number, body: ProductVegetarianRequestBody): Promise<ApiResponse<number>> {
    return api.post<number>(`${VERSION_PATH}/${productId}/vegetarian`, body);
  },

  /**
   * 채식 해제도 승인 대상이라 DELETE 가 즉시 반영이 아니라 "해제 요청"이다.
   * 컨트롤러가 `@ModelAttribute`로 받아 `shopId`는 query로 보낸다.
   */
  clearVegetarian(productId: number, shopId: number): Promise<ApiResponse<void>> {
    return api.delete<void>(`${VERSION_PATH}/${productId}/vegetarian`, undefined, { params: { shopId } });
  },
};
