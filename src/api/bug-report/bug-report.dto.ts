import type { FileResponse } from "@/api/banner/image.dto";

// 버그 제보 목록 조회 (검색 필터)
export interface BugReportListQueryRequest {
  title?: string;
  content?: string;
  memberId?: number;
  status?: string;
  category?: string;
  priority?: string;
}

// 제보 회원 요약
export interface MemberSummaryResponse {
  id: number;
  nickname: string;
}

// 버그 제보 목록 항목
export interface BugReportListItemResponse {
  id: number;
  member: MemberSummaryResponse | null;
  device: string;
  title: string;
  status: string;
  category: string | null;
  priority: string | null;
  imageCount: number;
  createdAt: string;
}

// 버그 제보 상세
export interface BugReportDetailResponse {
  id: number;
  member: MemberSummaryResponse | null;
  device: string;
  title: string;
  content: string;
  status: string;
  category: string | null;
  priority: string | null;
  assigneeAdminId: number | null;
  adminAnswer: string | null;
  resolvedAt: string | null;
  appVersion: string;
  platform: string;
  osVersion: string;
  images: FileResponse[];
  createdAt: string;
  updatedAt: string;
}

// 처리 상태 변경
export interface BugReportStatusUpdateRequest {
  status: string;
  answer?: string | null;
}

// 분류/우선순위 지정
export interface BugReportClassifyRequest {
  category: string;
  priority: string;
}

// 담당자 배정
export interface BugReportAssignRequest {
  assigneeAdminId: number;
}
