import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  BannerCreateRequest,
  BannerDetailResponse,
  BannerListItemResponse,
  BannerListQueryRequest,
  BannerUpdateRequest,
} from "./banner.dto";

/**
 * 배너 관리자 API
 */

const ENDPOINT = "/api/banners";

export const bannerRepository = {
  // 배너 목록 조회
  getList(query: BannerListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<BannerListItemResponse[]>> {
    return api.get<BannerListItemResponse[]>(`${ENDPOINT}/v1`, {
      params: { ...query, ...pageRequest },
    });
  },

  // 배너 등록
  create(body: BannerCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1`, body);
  },

  // 배너 상세 조회
  getDetail(id: number): Promise<ApiResponse<BannerDetailResponse>> {
    return api.get<BannerDetailResponse>(`${ENDPOINT}/v1/${id}`);
  },

  // 배너 수정
  update(id: number, body: BannerUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/${id}`, body);
  },

  // 배너 삭제
  remove(id: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/${id}`);
  },
};
