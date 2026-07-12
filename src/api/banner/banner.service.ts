import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type { BannerDetail, BannerListItem } from "../../feature/banner/domain";
import type { BannerListQueryRequest } from "./banner.dto";
import { bannerRepository } from "./banner.repository";

export const bannerService = {
  // 배너 목록 조회
  // 도메인 반환
  async getBanners(query: BannerListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<BannerListItem[]>> {
    const res = await bannerRepository.getList(query, pageRequest);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        type: item.type,
        title: item.title,
        file: item.file,
        linkUrl: item.linkUrl,
        startDate: item.startDate,
        endDate: item.endDate,
        sort: item.sort,
        visible: item.visible,
      })),
    };
  },

  // 배너 상세 조회
  // 도메인 반환
  async getBanner(id: number): Promise<ApiResponse<BannerDetail>> {
    const res = await bannerRepository.getDetail(id);
    if (!res.data) return { ...res, data: undefined };
    return {
      ...res,
      data: {
        id: res.data.id,
        type: res.data.type,
        title: res.data.title,
        image: res.data.image,
        linkUrl: res.data.linkUrl,
        startDate: res.data.startDate,
        endDate: res.data.endDate,
        sort: res.data.sort,
        visible: res.data.visible,
        createdAt: res.data.createdAt,
        updatedAt: res.data.updatedAt,
      },
    };
  },
};
