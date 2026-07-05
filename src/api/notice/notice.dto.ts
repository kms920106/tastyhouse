// 공지사항 목록 조회
export interface NoticeListQueryRequest {
  title?: string;
  content?: string;
  visible?: boolean;
}

// 공지사항 목록 조회
export interface NoticeListItemResponse {
  id: number;
  title: string;
  content: string;
  visible: boolean;
  createdAt: string;
}

// 공지사항 등록
export interface NoticeCreateRequest {
  title: string;
  content: string;
  visible: boolean;
}

// 공지사항 상세 조회
export interface NoticeDetailResponse {
  id: number;
  title: string;
  content: string;
  visible: boolean;
  createdAt: string;
  updatedAt: string;
}

// 공지사항 수정
export interface NoticeUpdateRequest {
  title: string;
  content: string;
  visible: boolean;
}
