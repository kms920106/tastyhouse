import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  EventAnnouncementRequest,
  EventAnnouncementResponse,
  EventCreateRequest,
  EventDetailResponse,
  EventListItemResponse,
  EventListQueryRequest,
  EventUpdateRequest,
  EventWinnerCreateRequest,
  EventWinnerResponse,
} from "./event.dto";

/**
 * 이벤트 관리자 API
 */

const ENDPOINT = "/api/events";

export const eventRepository = {
  // 이벤트 목록 조회
  getList(query: EventListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<EventListItemResponse[]>> {
    return api.get<EventListItemResponse[]>(`${ENDPOINT}/v1`, {
      params: { ...query, ...pageRequest },
    });
  },

  // 이벤트 등록
  create(body: EventCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1`, body);
  },

  // 이벤트 상세 조회
  getDetail(id: number): Promise<ApiResponse<EventDetailResponse>> {
    return api.get<EventDetailResponse>(`${ENDPOINT}/v1/${id}`);
  },

  // 이벤트 수정
  update(id: number, body: EventUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/${id}`, body);
  },

  // 이벤트 삭제 (Soft Delete)
  remove(id: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/${id}`);
  },

  // 당첨자 발표 공지 조회
  getAnnouncement(id: number): Promise<ApiResponse<EventAnnouncementResponse>> {
    return api.get<EventAnnouncementResponse>(`${ENDPOINT}/v1/${id}/announcement`);
  },

  // 당첨자 발표 공지 등록
  createAnnouncement(id: number, body: EventAnnouncementRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${id}/announcement`, body);
  },

  // 당첨자 발표 공지 수정
  updateAnnouncement(id: number, body: EventAnnouncementRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/${id}/announcement`, body);
  },

  // 당첨자 목록 조회 (순위 오름차순, 페이지네이션 없음)
  getWinners(id: number): Promise<ApiResponse<EventWinnerResponse[]>> {
    return api.get<EventWinnerResponse[]>(`${ENDPOINT}/v1/${id}/winners`);
  },

  // 당첨자 등록
  createWinner(id: number, body: EventWinnerCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${id}/winners`, body);
  },

  // 당첨자 삭제 (Hard Delete)
  removeWinner(id: number, winnerId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/${id}/winners/${winnerId}`);
  },
};
