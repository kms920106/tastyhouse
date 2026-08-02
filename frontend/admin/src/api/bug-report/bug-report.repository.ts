import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  BugReportAssignRequest,
  BugReportClassifyRequest,
  BugReportDetailResponse,
  BugReportListItemResponse,
  BugReportListQueryRequest,
  BugReportStatusUpdateRequest,
} from "./bug-report.dto";

/**
 * 버그 제보 관리자 API
 */

const ENDPOINT = "/api/bug-reports";

export const bugReportRepository = {
  // 버그 제보 목록 조회
  getList(
    query: BugReportListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<BugReportListItemResponse[]>> {
    return api.get<BugReportListItemResponse[]>(`${ENDPOINT}/v1`, {
      params: { ...query, ...pageRequest },
    });
  },

  // 버그 제보 상세 조회
  getDetail(id: number): Promise<ApiResponse<BugReportDetailResponse>> {
    return api.get<BugReportDetailResponse>(`${ENDPOINT}/v1/${id}`);
  },

  // 버그 제보 처리 상태 변경
  updateStatus(id: number, body: BugReportStatusUpdateRequest): Promise<ApiResponse<null>> {
    return api.patch<null>(`${ENDPOINT}/v1/${id}/status`, body);
  },

  // 버그 제보 분류/우선순위 지정
  classify(id: number, body: BugReportClassifyRequest): Promise<ApiResponse<null>> {
    return api.patch<null>(`${ENDPOINT}/v1/${id}/classification`, body);
  },

  // 버그 제보 담당자 배정
  assign(id: number, body: BugReportAssignRequest): Promise<ApiResponse<null>> {
    return api.patch<null>(`${ENDPOINT}/v1/${id}/assignee`, body);
  },
};
