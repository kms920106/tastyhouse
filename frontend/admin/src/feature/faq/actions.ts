"use server";

import { revalidatePath } from "next/cache";

import { faqRepository } from "@/api/faq/faq.repository";
import { faqService } from "@/api/faq/faq.service";
import type { FaqCategory, FaqCategoryDetail, FaqDetail } from "@/feature/faq/domain";

import { FAQ_CATEGORY_MESSAGE, FAQ_MESSAGE } from "./message";
import { type FaqCategoryFormValues, type FaqFormValues, faqCategoryFormSchema, faqFormSchema } from "./schema";

const FAQS_PATH = "/dashboard/faqs";

type ActionResult = {
  success: boolean;
  message?: string;
  id?: number;
};

type FaqDetailResult = {
  success: boolean;
  message?: string;
  data?: FaqDetail;
};

type FaqCategoriesResult = {
  success: boolean;
  message?: string;
  data?: FaqCategory[];
};

type FaqCategoryDetailResult = {
  success: boolean;
  message?: string;
  data?: FaqCategoryDetail;
};

// FAQ 등록
export async function createFaqAction(values: FaqFormValues): Promise<ActionResult> {
  const parsed = faqFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? FAQ_MESSAGE.INVALID_INPUT,
    };
  }

  const { error, data } = await faqRepository.create(parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(FAQS_PATH);
  return { success: true, id: data };
}

// FAQ 상세 조회
export async function fetchFaqAction(id: number): Promise<FaqDetailResult> {
  const { error, data } = await faqService.getFaq(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// FAQ 수정
export async function updateFaqAction(id: number, values: FaqFormValues): Promise<ActionResult> {
  const parsed = faqFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? FAQ_MESSAGE.INVALID_INPUT,
    };
  }

  const { error } = await faqRepository.update(id, parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(FAQS_PATH);
  return { success: true };
}

// FAQ 삭제
export async function deleteFaqAction(id: number): Promise<ActionResult> {
  const { error } = await faqRepository.remove(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(FAQS_PATH);
  return { success: true };
}

// FAQ 카테고리 목록 조회
export async function fetchFaqCategoriesAction(): Promise<FaqCategoriesResult> {
  const { error, data } = await faqService.getCategories();
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// FAQ 카테고리 등록
export async function createFaqCategoryAction(values: FaqCategoryFormValues): Promise<ActionResult> {
  const parsed = faqCategoryFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? FAQ_CATEGORY_MESSAGE.INVALID_INPUT,
    };
  }

  const { error, data } = await faqRepository.createCategory(parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(FAQS_PATH);
  return { success: true, id: data };
}

// FAQ 카테고리 상세 조회
export async function fetchFaqCategoryAction(categoryId: number): Promise<FaqCategoryDetailResult> {
  const { error, data } = await faqService.getCategory(categoryId);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// FAQ 카테고리 수정
export async function updateFaqCategoryAction(
  categoryId: number,
  values: FaqCategoryFormValues,
): Promise<ActionResult> {
  const parsed = faqCategoryFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? FAQ_CATEGORY_MESSAGE.INVALID_INPUT,
    };
  }

  const { error } = await faqRepository.updateCategory(categoryId, parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(FAQS_PATH);
  return { success: true };
}

// FAQ 카테고리 삭제
export async function deleteFaqCategoryAction(categoryId: number): Promise<ActionResult> {
  const { error } = await faqRepository.removeCategory(categoryId);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(FAQS_PATH);
  return { success: true };
}
