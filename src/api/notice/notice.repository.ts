import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  NoticeCreateRequest,
  NoticeDetailResponse,
  NoticeListItemResponse,
  NoticeListQueryRequest,
  NoticeUpdateRequest,
} from "./notice.dto";

/**
 * 공지사항 관리자 API
 */

const ENDPOINT = "/api/notices";

export const noticeRepository = {
  // 공지사항 목록 조회
  getList(query: NoticeListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<NoticeListItemResponse[]>> {
    return api.get<NoticeListItemResponse[]>(`${ENDPOINT}/v1`, {
      params: { ...query, ...pageRequest },
    });
  },

  // 공지사항 등록
  create(body: NoticeCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1`, body);
  },

  // 공지사항 상세 조회
  getDetail(id: number): Promise<ApiResponse<NoticeDetailResponse>> {
    return api.get<NoticeDetailResponse>(`${ENDPOINT}/v1/${id}`);
  },

  // 공지사항 수정
  update(id: number, body: NoticeUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/${id}`, body);
  },

  // 공지사항 삭제
  remove(id: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/${id}`);
  },
};
