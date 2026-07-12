// 배너 유형
export type BannerType = "HOME" | "SIDEBAR";

import type { FileResponse } from "./image.dto";

// 배너 목록 조회
export interface BannerListQueryRequest {
  type?: BannerType;
  title?: string;
  visible?: boolean;
}

// 배너 목록 조회
export interface BannerListItemResponse {
  id: number;
  type: BannerType;
  title: string | null;
  file: FileResponse | null;
  linkUrl: string | null;
  startDate: string | null;
  endDate: string | null;
  sort: number;
  visible: boolean;
}

// 배너 등록
export interface BannerCreateRequest {
  type: BannerType;
  title?: string;
  imageFileId: number;
  linkUrl?: string;
  startDate?: string;
  endDate?: string;
  sort: number;
  visible?: boolean;
}

// 배너 상세 조회
export interface BannerDetailResponse {
  id: number;
  type: BannerType;
  title: string | null;
  image: FileResponse;
  linkUrl: string | null;
  startDate: string | null;
  endDate: string | null;
  sort: number;
  visible: boolean;
  createdAt: string;
  updatedAt: string;
}

// 배너 수정
export interface BannerUpdateRequest {
  type: BannerType;
  title?: string;
  imageFileId: number;
  linkUrl?: string;
  startDate?: string;
  endDate?: string;
  sort: number;
  visible: boolean;
}
