"use server";

import { revalidatePath } from "next/cache";

import { orderRepository } from "@/api/order/order.repository";
import { orderService } from "@/api/order/order.service";
import type { OrderDetail } from "@/feature/order/domain";

import { ORDER_MESSAGE } from "./message";
import { type OrderStatusUpdateValues, orderStatusUpdateSchema } from "./schema";

const ORDERS_PATH = "/dashboard/orders";

type ActionResult = {
  success: boolean;
  message?: string;
};

type OrderDetailResult = {
  success: boolean;
  message?: string;
  data?: OrderDetail;
};

// 주문 상세 조회
export async function fetchOrderAction(id: number): Promise<OrderDetailResult> {
  const { error, data } = await orderService.getOrder(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 주문 상태 변경
export async function updateOrderStatusAction(id: number, values: OrderStatusUpdateValues): Promise<ActionResult> {
  const parsed = orderStatusUpdateSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? ORDER_MESSAGE.INVALID_INPUT,
    };
  }

  const { error } = await orderRepository.updateStatus(id, { status: parsed.data.status });
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(ORDERS_PATH);
  return { success: true };
}

// 주문 삭제 (Soft Delete)
export async function deleteOrderAction(id: number): Promise<ActionResult> {
  const { error } = await orderRepository.remove(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(ORDERS_PATH);
  return { success: true };
}
