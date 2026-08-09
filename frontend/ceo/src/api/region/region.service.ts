import "server-only";

import type { ApiResponse } from "@/api/shared/types";
import type {
  AdminDongBoundary,
  AdminDongBoundaryResult,
  AdminDongTree,
  AdminDongTreeNode,
  GeoRing,
} from "@/feature/shop/domain";

import type {
  AdminDongBoundaryItemResponse,
  AdminDongBoundaryQueryRequest,
  AdminDongTreeItemResponse,
  AdminDongTreeQueryRequest,
  GeoPointResponse,
} from "./region.dto";
import { regionRepository } from "./region.repository";

/**
 * 행정동 조회 서비스 — DTO → domain 매핑이 실제로 필요한 조회에만 둔다.
 *
 * 경계 응답은 좌표 배열을 그대로 쓰기 어렵다. 링 안의 점 개수가 수천 개까지 가고 지도가 이동할
 * 때마다 다시 받으므로, 여기서 좌표를 정규화해 두면 캔버스 렌더러가 매 프레임 방어 코드를 돌지
 * 않아도 된다. 키워드 검색은 매핑이 사실상 항등이라 서비스를 두지 않고 액션이 리포지토리를
 * 직접 호출한다(`api/AGENTS.md` 규칙).
 */

/** 서버가 좌표를 문자열(BigDecimal 직렬화)로 줄 수도 있어 숫자로 통일한다 */
function toNumber(value: number | string): number {
  return typeof value === "number" ? value : Number(value);
}

function toRing(points: GeoPointResponse[]): GeoRing {
  return points.map((point) => ({
    latitude: toNumber(point.latitude),
    longitude: toNumber(point.longitude),
  }));
}

function toBoundary(item: AdminDongBoundaryItemResponse): AdminDongBoundary {
  return {
    adminDongId: item.adminDongId,
    regionName: item.regionName,
    centerLatitude: toNumber(item.centerLatitude),
    centerLongitude: toNumber(item.centerLongitude),
    // 경계 미보유는 오류가 아니라 정상 상태다 — null 을 그대로 통과시킨다.
    rings: item.rings ? item.rings.map(toRing) : null,
  };
}

function toTreeNode(item: AdminDongTreeItemResponse): AdminDongTreeNode {
  return {
    name: item.name,
    adminDongId: item.adminDongId,
    code: item.code,
    dongCount: item.dongCount,
  };
}

export const regionService = {
  async getAdminDongTree(query: AdminDongTreeQueryRequest): Promise<ApiResponse<AdminDongTree>> {
    const response = await regionRepository.getAdminDongTree(query);
    if (response.error !== undefined || !response.data) return { ...response, data: undefined };

    return {
      ...response,
      data: {
        level: response.data.level,
        items: response.data.items.map(toTreeNode),
      },
    };
  },

  async getAdminDongBoundaries(query: AdminDongBoundaryQueryRequest): Promise<ApiResponse<AdminDongBoundaryResult>> {
    const response = await regionRepository.getAdminDongBoundaries(query);
    if (response.error !== undefined || !response.data) return { ...response, data: undefined };

    return {
      ...response,
      data: {
        truncated: response.data.truncated,
        items: response.data.items.map(toBoundary),
      },
    };
  },
};
