export interface FaqCategory {
  id: number;
  name: string;
  sort: number;
  visible: boolean;
  createdAt: string;
}

export interface FaqCategoryDetail {
  id: number;
  name: string;
  sort: number;
  visible: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface FaqListItem {
  id: number;
  faqCategoryId: number;
  question: string;
  sort: number;
  visible: boolean;
  createdAt: string;
}

export interface FaqDetail {
  id: number;
  faqCategoryId: number;
  question: string;
  answer: string;
  sort: number;
  visible: boolean;
  createdAt: string;
  updatedAt: string;
}
