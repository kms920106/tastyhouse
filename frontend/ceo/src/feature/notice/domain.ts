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
