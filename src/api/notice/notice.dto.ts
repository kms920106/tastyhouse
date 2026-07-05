export interface NoticeListItem {
  id: number;
  title: string;
  content: string;
  visible: boolean;
  createdAt: string;
}

export interface NoticeDetail {
  id: number;
  title: string;
  content: string;
  visible: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface NoticeListQueryRequest {
  title?: string;
  content?: string;
  visible?: boolean;
}

export interface NoticeCreateRequest {
  title: string;
  content: string;
  visible: boolean;
}

export interface NoticeUpdateRequest {
  title: string;
  content: string;
  visible: boolean;
}
