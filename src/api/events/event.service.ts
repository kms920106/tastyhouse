import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type { EventAnnouncement, EventDetail, EventListItem, EventWinner } from "../../feature/event/domain";
import type { EventListQueryRequest } from "./event.dto";
import { eventRepository } from "./event.repository";

export const eventService = {
  // 이벤트 목록 조회
  // 도메인 반환
  async getEvents(query: EventListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<EventListItem[]>> {
    const res = await eventRepository.getList(query, pageRequest);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        name: item.name,
        status: item.status,
        file: item.file,
        startAt: item.startAt,
        endAt: item.endAt,
      })),
    };
  },

  // 이벤트 상세 조회
  // 도메인 반환
  async getEvent(id: number): Promise<ApiResponse<EventDetail>> {
    const res = await eventRepository.getDetail(id);
    if (!res.data) return { ...res, data: undefined };
    return {
      ...res,
      data: {
        id: res.data.id,
        name: res.data.name,
        description: res.data.description,
        subtitle: res.data.subtitle,
        thumbnailFile: res.data.thumbnailFile,
        bannerFile: res.data.bannerFile,
        contentHtml: res.data.contentHtml,
        status: res.data.status,
        startAt: res.data.startAt,
        endAt: res.data.endAt,
        createdAt: res.data.createdAt,
        updatedAt: res.data.updatedAt,
      },
    };
  },

  // 당첨자 발표 공지 조회
  // 도메인 반환
  async getEventAnnouncement(id: number): Promise<ApiResponse<EventAnnouncement>> {
    const res = await eventRepository.getAnnouncement(id);
    if (!res.data) return { ...res, data: undefined };
    return {
      ...res,
      data: {
        id: res.data.id,
        eventId: res.data.eventId,
        name: res.data.name,
        content: res.data.content,
        announcedAt: res.data.announcedAt,
      },
    };
  },

  // 당첨자 목록 조회 (순위 오름차순)
  // 도메인 반환
  async getEventWinners(id: number): Promise<ApiResponse<EventWinner[]>> {
    const res = await eventRepository.getWinners(id);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        eventId: item.eventId,
        rankNo: item.rankNo,
        winnerName: item.winnerName,
        phoneNumber: item.phoneNumber,
        announcedAt: item.announcedAt,
      })),
    };
  },
};
