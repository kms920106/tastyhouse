import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type { FaqCategory, FaqCategoryDetail, FaqDetail, FaqListItem } from "../../feature/faq/domain";
import type { FaqListQueryRequest } from "./faq.dto";
import { faqRepository } from "./faq.repository";

export const faqService = {
  // FAQ 카테고리 목록 조회
  // 도메인 반환
  async getCategories(): Promise<ApiResponse<FaqCategory[]>> {
    const res = await faqRepository.getCategories();
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        name: item.name,
        sort: item.sort,
        visible: item.visible,
        createdAt: item.createdAt,
      })),
    };
  },

  // FAQ 카테고리 상세 조회
  // 도메인 반환
  async getCategory(categoryId: number): Promise<ApiResponse<FaqCategoryDetail>> {
    const res = await faqRepository.getCategory(categoryId);
    if (!res.data) return { ...res, data: undefined };
    return {
      ...res,
      data: {
        id: res.data.id,
        name: res.data.name,
        sort: res.data.sort,
        visible: res.data.visible,
        createdAt: res.data.createdAt,
        updatedAt: res.data.updatedAt,
      },
    };
  },

  // FAQ 목록 조회
  // 도메인 반환
  async getFaqs(query: FaqListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<FaqListItem[]>> {
    const res = await faqRepository.getList(query, pageRequest);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        faqCategoryId: item.faqCategoryId,
        question: item.question,
        sort: item.sort,
        visible: item.visible,
        createdAt: item.createdAt,
      })),
    };
  },

  // FAQ 상세 조회
  // 도메인 반환
  async getFaq(id: number): Promise<ApiResponse<FaqDetail>> {
    const res = await faqRepository.getDetail(id);
    if (!res.data) return { ...res, data: undefined };
    return {
      ...res,
      data: {
        id: res.data.id,
        faqCategoryId: res.data.faqCategoryId,
        question: res.data.question,
        answer: res.data.answer,
        sort: res.data.sort,
        visible: res.data.visible,
        createdAt: res.data.createdAt,
        updatedAt: res.data.updatedAt,
      },
    };
  },
};
