import "server-only";

import { api } from "@/api/shared/client";
import type { ApiResponse } from "@/api/shared/types";

import type {
  ShopNoticeContentValidateRequest,
  ShopNoticeExposureUpdateRequest,
  ShopNoticeResponse,
} from "./shop-notice.dto";

/**
 * 점주용 공지 관리 API (transport only)
 *
 * 하위 리소스 경로 규칙은 리소스마다 다르므로(`src/api/AGENTS.md`) 일반화하지 않고
 * `docs/tasks/backend.md` 3장을 그대로 반영한다.
 * - 목록·등록·수정·삭제는 부모 `{shopId}` 경로를 유지한다 (`/v1/{shopId}/notices/...`)
 * - **금칙어 사전검사(`validate`)만 개별 공지가 아닌 컬렉션 하위 POST 다** — 저장 전 본문을
 *   검사하는 용도라 대상 `noticeId` 가 아직 없기 때문이다.
 */

const ENDPOINT = "/api/shops";

export const shopNoticeRepository = {
  getList(shopId: number): Promise<ApiResponse<ShopNoticeResponse[]>> {
    return api.get<ShopNoticeResponse[]>(`${ENDPOINT}/v1/${shopId}/notices`);
  },

  // multipart 직접 전송 — 선업로드(fileId) 없이 원본 File 을 그대로 보낸다.
  create(shopId: number, formData: FormData): Promise<ApiResponse<number>> {
    return api.upload<number>(`${ENDPOINT}/v1/${shopId}/notices`, formData);
  },

  update(shopId: number, noticeId: number, formData: FormData): Promise<ApiResponse<void>> {
    return api.uploadPut<void>(`${ENDPOINT}/v1/${shopId}/notices/${noticeId}`, formData);
  },

  remove(shopId: number, noticeId: number): Promise<ApiResponse<void>> {
    return api.delete<void>(`${ENDPOINT}/v1/${shopId}/notices/${noticeId}`);
  },

  // 노출 토글은 JSON body — 파일이 없으므로 upload 계열을 쓰지 않는다.
  updateExposure(shopId: number, noticeId: number, body: ShopNoticeExposureUpdateRequest): Promise<ApiResponse<void>> {
    return api.put<void>(`${ENDPOINT}/v1/${shopId}/notices/${noticeId}/exposure`, body);
  },

  validateContent(shopId: number, body: ShopNoticeContentValidateRequest): Promise<ApiResponse<string[]>> {
    return api.post<string[]>(`${ENDPOINT}/v1/${shopId}/notices/validate`, body);
  },
};
