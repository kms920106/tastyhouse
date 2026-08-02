import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type { NoticeDetail, NoticeListItem } from "../../feature/notice/domain";
import type { NoticeListQueryRequest } from "./notice.dto";
import { noticeRepository } from "./notice.repository";

export const noticeService = {
  // 공지사항 목록 조회
  // 도메인 반환
  async getNotices(query: NoticeListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<NoticeListItem[]>> {
    const res = await noticeRepository.getList(query, pageRequest);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        title: item.title,
        content: item.content,
        visible: item.visible,
        createdAt: item.createdAt,
      })),
    };
  },

  // 공지사항 상세 조회
  // 도메인 반환
  async getNotice(id: number): Promise<ApiResponse<NoticeDetail>> {
    const res = await noticeRepository.getDetail(id);
    if (!res.data) return { ...res, data: undefined };
    return {
      ...res,
      data: {
        id: res.data.id,
        title: res.data.title,
        content: res.data.content,
        visible: res.data.visible,
        createdAt: res.data.createdAt,
        updatedAt: res.data.updatedAt,
      },
    };
  },
};
