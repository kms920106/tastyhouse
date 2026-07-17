import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type { OrderDetail, OrderListItem } from "../../feature/order/domain";
import type { OrderListQueryRequest } from "./order.dto";
import { orderRepository } from "./order.repository";

export const orderService = {
  // 주문 목록 조회
  // 도메인 반환
  async getOrders(query: OrderListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<OrderListItem[]>> {
    const res = await orderRepository.getList(query, pageRequest);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        orderNumber: item.orderNumber,
        shopName: item.shopName,
        ordererName: item.ordererName,
        orderMethod: item.orderMethod,
        orderStatus: item.orderStatus,
        paymentStatus: item.paymentStatus,
        finalAmount: item.finalAmount,
        totalItemCount: item.totalItemCount,
        createdAt: item.createdAt,
      })),
    };
  },

  // 주문 상세 조회
  // 도메인 반환
  async getOrder(id: number): Promise<ApiResponse<OrderDetail>> {
    const res = await orderRepository.getDetail(id);
    if (!res.data) return { ...res, data: undefined };
    return {
      ...res,
      data: {
        id: res.data.id,
        orderNumber: res.data.orderNumber,
        orderMethod: res.data.orderMethod,
        paymentStatus: res.data.paymentStatus,
        shopName: res.data.shopName,
        shopPhoneNumber: res.data.shopPhoneNumber,
        ordererName: res.data.ordererName,
        ordererPhone: res.data.ordererPhone,
        ordererEmail: res.data.ordererEmail,
        totalProductAmount: res.data.totalProductAmount,
        productDiscountAmount: res.data.productDiscountAmount,
        couponDiscountAmount: res.data.couponDiscountAmount,
        pointDiscountAmount: res.data.pointDiscountAmount,
        totalDiscountAmount: res.data.totalDiscountAmount,
        finalAmount: res.data.finalAmount,
        usedPoint: res.data.usedPoint,
        earnedPoint: res.data.earnedPoint,
        orderProducts: res.data.orderProducts.map((product) => ({
          id: product.id,
          productId: product.productId,
          name: product.name,
          imageUrl: product.imageUrl,
          quantity: product.quantity,
          originalPrice: product.originalPrice,
          discountPrice: product.discountPrice,
          totalOptionPrice: product.totalOptionPrice,
          totalPrice: product.totalPrice,
          selectedOptions: product.selectedOptions.map((option) => ({
            groupId: option.groupId,
            groupName: option.groupName,
            optionId: option.optionId,
            optionName: option.optionName,
            additionalPrice: option.additionalPrice,
          })),
        })),
        payment: res.data.payment
          ? {
              id: res.data.payment.id,
              paymentMethod: res.data.payment.paymentMethod,
              paymentStatus: res.data.payment.paymentStatus,
              amount: res.data.payment.amount,
              cardCompany: res.data.payment.cardCompany,
              cardNumber: res.data.payment.cardNumber,
              approvedAt: res.data.payment.approvedAt,
              receiptUrl: res.data.payment.receiptUrl,
            }
          : null,
        approvedAt: res.data.approvedAt,
        createdAt: res.data.createdAt,
      },
    };
  },
};
