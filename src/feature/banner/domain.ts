export type BannerType = "HOME" | "SIDEBAR";

export interface BannerImage {
  id: number;
  name: string;
  url: string;
}

export interface BannerListItem {
  id: number;
  type: BannerType;
  title: string | null;
  file: BannerImage | null;
  linkUrl: string | null;
  startDate: string | null;
  endDate: string | null;
  sort: number;
  visible: boolean;
}

export interface BannerDetail {
  id: number;
  type: BannerType;
  title: string | null;
  image: BannerImage;
  linkUrl: string | null;
  startDate: string | null;
  endDate: string | null;
  sort: number;
  visible: boolean;
  createdAt: string;
  updatedAt: string;
}
