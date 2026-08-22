"use server";

import { revalidatePath } from "next/cache";

import { fileRepository } from "@/api/file/file.repository";
import { productRepository } from "@/api/product/product.repository";
import { productService } from "@/api/product/product.service";
import type { ProductCategory, ProductDetail, ProductOptionGroups } from "@/feature/product/domain";

import { PRODUCT_MESSAGE } from "./message";
import {
  type CategoryFormValues,
  categorySchema,
  type OptionFormValues,
  type OptionGroupFormValues,
  optionGroupSchema,
  optionSchema,
  type ProductFormValues,
  type ProductImageFormValues,
  type ProductRejectFormValues,
  productFormSchema,
  productImageSchema,
  productRejectSchema,
} from "./schema";

const PRODUCTS_PATH = "/dashboard/products";

type ActionResult = {
  success: boolean;
  message?: string;
  id?: number;
};

type ProductDetailResult = {
  success: boolean;
  message?: string;
  data?: ProductDetail;
};

type ProductOptionsResult = {
  success: boolean;
  message?: string;
  data?: ProductOptionGroups;
};

type ProductImagesResult = {
  success: boolean;
  message?: string;
  data?: string[];
};

type CategoriesResult = {
  success: boolean;
  message?: string;
  data?: ProductCategory[];
};

type ImageUploadResult = {
  success: boolean;
  message?: string;
  /** 업로드된 파일의 ID(fileId) */
  fileId?: number;
};

// 등록 요청 body (shopId 포함)
function toCreateBody(values: ProductFormValues) {
  return {
    shopId: values.shopId,
    productCategoryId: values.productCategoryId,
    name: values.name,
    description: values.description,
    originalPrice: values.originalPrice,
    discountPrice: values.discountPrice,
    discountRate: values.discountRate,
    rating: values.rating,
    reviewCount: values.reviewCount,
    representative: values.representative,
    spiciness: values.spiciness,
    soldOut: values.soldOut,
    visible: values.visible,
    sort: values.sort,
  };
}

// 수정 요청 body (shopId 제외)
function toUpdateBody(values: ProductFormValues) {
  return {
    productCategoryId: values.productCategoryId,
    name: values.name,
    description: values.description,
    originalPrice: values.originalPrice,
    discountPrice: values.discountPrice,
    discountRate: values.discountRate,
    rating: values.rating,
    reviewCount: values.reviewCount,
    representative: values.representative,
    spiciness: values.spiciness,
    soldOut: values.soldOut,
    visible: values.visible,
    sort: values.sort,
  };
}

// 상품 등록
export async function createProductAction(values: ProductFormValues): Promise<ActionResult> {
  const parsed = productFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? PRODUCT_MESSAGE.INVALID_INPUT,
    };
  }

  const { error, data } = await productRepository.create(toCreateBody(parsed.data));
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(PRODUCTS_PATH);
  return { success: true, id: data };
}

// 상품 상세 조회
export async function fetchProductAction(id: number): Promise<ProductDetailResult> {
  const { error, data } = await productService.getProduct(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 상품 수정
export async function updateProductAction(id: number, values: ProductFormValues): Promise<ActionResult> {
  const parsed = productFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? PRODUCT_MESSAGE.INVALID_INPUT,
    };
  }

  const { error } = await productRepository.update(id, toUpdateBody(parsed.data));
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(PRODUCTS_PATH);
  return { success: true };
}

// 상품 품절 처리
export async function soldOutProductAction(id: number): Promise<ActionResult> {
  const { error } = await productRepository.soldOut(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(PRODUCTS_PATH);
  return { success: true };
}

// 상품 비활성화
export async function deactivateProductAction(id: number): Promise<ActionResult> {
  const { error } = await productRepository.deactivate(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(PRODUCTS_PATH);
  return { success: true };
}

// 상품 옵션 조회
export async function fetchProductOptionsAction(id: number): Promise<ProductOptionsResult> {
  const { error, data } = await productService.getProductOptions(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 옵션 그룹 등록 (상품 ID 기준)
export async function createOptionGroupAction(productId: number, values: OptionGroupFormValues): Promise<ActionResult> {
  const parsed = optionGroupSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? PRODUCT_MESSAGE.INVALID_INPUT,
    };
  }

  const { error, data } = await productRepository.createOptionGroup(productId, parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  // 옵션은 시트 내부에서 재조회하므로 revalidatePath 불필요.
  return { success: true, id: data };
}

// 옵션 등록 (옵션 그룹 ID 기준)
export async function createOptionAction(groupId: number, values: OptionFormValues): Promise<ActionResult> {
  const parsed = optionSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? PRODUCT_MESSAGE.INVALID_INPUT,
    };
  }

  const { error, data } = await productRepository.createOption(groupId, parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  return { success: true, id: data };
}

// 상품 이미지 조회
export async function fetchProductImagesAction(id: number): Promise<ProductImagesResult> {
  const { error, data } = await productService.getProductImages(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 상품 이미지 업로드 (파일 → fileId)
export async function uploadProductImageAction(formData: FormData): Promise<ImageUploadResult> {
  const file = formData.get("file");
  if (!(file instanceof File)) {
    return { success: false, message: PRODUCT_MESSAGE.IMAGE_UPLOAD_FAILED };
  }

  const { error, data } = await fileRepository.uploadImage(file);
  if (error !== undefined || data == null) {
    return { success: false, message: error ?? PRODUCT_MESSAGE.IMAGE_UPLOAD_FAILED };
  }

  return { success: true, fileId: data };
}

// 상품 이미지 등록 (업로드된 fileId 연결)
export async function addProductImageAction(productId: number, values: ProductImageFormValues): Promise<ActionResult> {
  const parsed = productImageSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? PRODUCT_MESSAGE.INVALID_INPUT,
    };
  }

  const { error, data } = await productRepository.createImage(productId, parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  return { success: true, id: data };
}

// 상품 카테고리 목록 조회 (드롭다운/시트 재조회용)
export async function fetchCategoriesAction(shopId: number): Promise<CategoriesResult> {
  const { error, data } = await productService.getCategories(shopId);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 상품 카테고리 등록
export async function createCategoryAction(values: CategoryFormValues): Promise<ActionResult> {
  const parsed = categorySchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? PRODUCT_MESSAGE.INVALID_INPUT,
    };
  }

  const { error, data } = await productRepository.createCategory(parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  return { success: true, id: data };
}

// ===== 메뉴 검수 (이미지 변경 요청 · 채식 설정 요청) =====

const PRODUCT_APPROVALS_PATH = "/dashboard/product-approvals";

// 메뉴 이미지 변경 요청 승인
export async function approveProductImageChangeAction(requestId: number): Promise<ActionResult> {
  const { error } = await productRepository.approveImageChangeRequest(requestId);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(PRODUCT_APPROVALS_PATH);
  return { success: true };
}

// 메뉴 이미지 변경 요청 반려
export async function rejectProductImageChangeAction(
  requestId: number,
  values: ProductRejectFormValues,
): Promise<ActionResult> {
  const parsed = productRejectSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? PRODUCT_MESSAGE.INVALID_INPUT,
    };
  }

  const { error } = await productRepository.rejectImageChangeRequest(requestId, parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(PRODUCT_APPROVALS_PATH);
  return { success: true };
}

// 메뉴 채식 설정 요청 승인
export async function approveProductVegetarianAction(requestId: number): Promise<ActionResult> {
  const { error } = await productRepository.approveVegetarianRequest(requestId);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(PRODUCT_APPROVALS_PATH);
  return { success: true };
}

// 메뉴 채식 설정 요청 반려
export async function rejectProductVegetarianAction(
  requestId: number,
  values: ProductRejectFormValues,
): Promise<ActionResult> {
  const parsed = productRejectSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? PRODUCT_MESSAGE.INVALID_INPUT,
    };
  }

  const { error } = await productRepository.rejectVegetarianRequest(requestId, parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(PRODUCT_APPROVALS_PATH);
  return { success: true };
}
