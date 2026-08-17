import "server-only";

import { api } from "@/api/shared/client";
import type { ApiResponse } from "@/api/shared/types";

import type {
  ProductAvailabilityChangeResponse,
  ProductAvailabilityGroupResponse,
  ProductAvailabilitySearchRequest,
  ProductHiddenRequest,
  ProductOptionAvailabilityGroupResponse,
  ProductOptionHiddenRequest,
  ProductOptionReleaseRequest,
  ProductOptionSoldOutRequest,
  ProductOptionSoldOutUntilRequest,
  ProductReleaseRequest,
  ProductSoldOutRequest,
  ProductSoldOutUntilRequest,
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
};
