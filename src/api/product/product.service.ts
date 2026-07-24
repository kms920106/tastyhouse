import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";
import type { ProductCategory, ProductDetail, ProductListItem, ProductOptionGroups } from "@/feature/product/domain";

import type { ProductListQueryRequest } from "./product.dto";
import { productRepository } from "./product.repository";

export const productService = {
  // 상품 목록 조회 — 도메인 반환
  async getProducts(
    query: ProductListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<ProductListItem[]>> {
    const res = await productRepository.getList(query, pageRequest);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        shopName: item.shopName,
        name: item.name,
        originalPrice: item.originalPrice,
        discountPrice: item.discountPrice,
        discountRate: item.discountRate,
        representative: item.representative,
        soldOut: item.soldOut,
        visible: item.visible,
        sort: item.sort,
      })),
    };
  },

  // 상품 상세 조회 — 도메인 반환
  async getProduct(id: number): Promise<ApiResponse<ProductDetail>> {
    const res = await productRepository.getDetail(id);
    if (!res.data) return { ...res, data: undefined };
    return {
      ...res,
      data: {
        id: res.data.id,
        shopId: res.data.shopId,
        productCategoryId: res.data.productCategoryId,
        name: res.data.name,
        description: res.data.description,
        originalPrice: res.data.originalPrice,
        discountPrice: res.data.discountPrice,
        discountRate: res.data.discountRate,
        rating: res.data.rating,
        reviewCount: res.data.reviewCount,
        representative: res.data.representative,
        spiciness: res.data.spiciness,
        soldOut: res.data.soldOut,
        visible: res.data.visible,
        sort: res.data.sort,
        createdAt: res.data.createdAt,
        updatedAt: res.data.updatedAt,
      },
    };
  },

  // 상품 옵션 조회 — 도메인 반환
  async getProductOptions(id: number): Promise<ApiResponse<ProductOptionGroups>> {
    const res = await productRepository.getOptions(id);
    if (!res.data) return { ...res, data: undefined };
    return {
      ...res,
      data: {
        optionGroups: res.data.optionGroups.map((group) => ({
          id: group.id,
          name: group.name,
          description: group.description,
          required: group.required,
          multipleSelect: group.multipleSelect,
          minSelect: group.minSelect,
          maxSelect: group.maxSelect,
          common: group.common,
          options: group.options.map((option) => ({
            id: option.id,
            name: option.name,
            additionalPrice: option.additionalPrice,
            soldOut: option.soldOut,
          })),
        })),
      },
    };
  },

  // 상품 이미지 조회 — 이미지 URL 목록 반환
  async getProductImages(id: number): Promise<ApiResponse<string[]>> {
    const res = await productRepository.getImages(id);
    return { ...res, data: res.data?.imageUrls };
  },

  // 상품 카테고리 목록 조회 — 도메인 반환
  async getCategories(shopId: number): Promise<ApiResponse<ProductCategory[]>> {
    const res = await productRepository.getCategories(shopId);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        shopId: item.shopId,
        name: item.name,
        sort: item.sort,
        visible: item.visible,
      })),
    };
  },
};
