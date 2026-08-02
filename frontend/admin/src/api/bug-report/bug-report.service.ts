import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";
import type { BugCategory, BugPriority, BugStatus } from "@/feature/bug-report/constants";
import type { BugReportDetail, BugReportListItem, MemberSummary } from "@/feature/bug-report/domain";

import type { BugReportListQueryRequest, MemberSummaryResponse } from "./bug-report.dto";
import { bugReportRepository } from "./bug-report.repository";

function toMemberSummary(member: MemberSummaryResponse | null): MemberSummary | null {
  if (!member) return null;
  return { id: member.id, nickname: member.nickname };
}

export const bugReportService = {
  // 버그 제보 목록 조회
  // 도메인 반환
  async getBugReports(
    query: BugReportListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<BugReportListItem[]>> {
    const res = await bugReportRepository.getList(query, pageRequest);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        member: toMemberSummary(item.member),
        device: item.device,
        title: item.title,
        status: item.status as BugStatus,
        category: item.category as BugCategory | null,
        priority: item.priority as BugPriority | null,
        imageCount: item.imageCount,
        createdAt: item.createdAt,
      })),
    };
  },

  // 버그 제보 상세 조회
  // 도메인 반환
  async getBugReport(id: number): Promise<ApiResponse<BugReportDetail>> {
    const res = await bugReportRepository.getDetail(id);
    if (!res.data) return { ...res, data: undefined };
    const { data } = res;
    return {
      ...res,
      data: {
        id: data.id,
        member: toMemberSummary(data.member),
        device: data.device,
        title: data.title,
        content: data.content,
        status: data.status as BugStatus,
        category: data.category as BugCategory | null,
        priority: data.priority as BugPriority | null,
        assigneeAdminId: data.assigneeAdminId,
        adminAnswer: data.adminAnswer,
        resolvedAt: data.resolvedAt,
        appVersion: data.appVersion,
        platform: data.platform,
        osVersion: data.osVersion,
        images: data.images.map((img) => ({ id: img.id, name: img.name, url: img.url })),
        createdAt: data.createdAt,
        updatedAt: data.updatedAt,
      },
    };
  },
};
