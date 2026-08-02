import { orderService } from "@/api/order/order.service";
import type { OrderMethod, OrderStatus, PaymentStatus } from "@/feature/order/domain";
import { ORDER_MESSAGE } from "@/feature/order/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseSearchString } from "@/lib/utils";

import { Orders } from "./_components/orders";

const ORDER_STATUSES: readonly OrderStatus[] = ["PENDING", "CONFIRMED", "PREPARING", "COMPLETED", "CANCELLED"];
const ORDER_METHODS: readonly OrderMethod[] = ["TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"];
const PAYMENT_STATUSES: readonly PaymentStatus[] = ["PENDING", "COMPLETED", "FAILED", "CANCELLED"];

const MAX_PAGE_SIZE = 100;

function parseOrderStatus(value: string | string[] | undefined): OrderStatus | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  return ORDER_STATUSES.includes(raw as OrderStatus) ? (raw as OrderStatus) : undefined;
}

function parseOrderMethod(value: string | string[] | undefined): OrderMethod | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  return ORDER_METHODS.includes(raw as OrderMethod) ? (raw as OrderMethod) : undefined;
}

function parsePaymentStatus(value: string | string[] | undefined): PaymentStatus | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  return PAYMENT_STATUSES.includes(raw as PaymentStatus) ? (raw as PaymentStatus) : undefined;
}

function parseShopId(value: string | string[] | undefined): number | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  const parsed = Number(raw);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/orders">) {
  const {
    page: pageParam,
    size: sizeParam,
    shopId: shopIdParam,
    orderStatus: orderStatusParam,
    orderMethod: orderMethodParam,
    paymentStatus: paymentStatusParam,
    orderNumber: orderNumberParam,
    ordererName: ordererNameParam,
    startDate: startDateParam,
    endDate: endDateParam,
  } = await searchParams;

  const shopId = parseShopId(shopIdParam);
  const orderStatus = parseOrderStatus(orderStatusParam);
  const orderMethod = parseOrderMethod(orderMethodParam);
  const paymentStatus = parsePaymentStatus(paymentStatusParam);
  const orderNumber = parseSearchString(orderNumberParam);
  const ordererName = parseSearchString(ordererNameParam);
  const startDate = parseSearchString(startDateParam);
  const endDate = parseSearchString(endDateParam);
  const page = parseNonNegativeInt(pageParam, 0);
  const size = Math.min(parseNonNegativeInt(sizeParam, 10), MAX_PAGE_SIZE);

  const { error, data, pagination } = await orderService.getOrders(
    { shopId, orderStatus, orderMethod, paymentStatus, orderNumber, ordererName, startDate, endDate },
    { page, size },
  );

  if (error || !data || !pagination) {
    logger.error({ reason: error, data, pagination }, "주문 목록 조회 실패");
    throw new Error(ORDER_MESSAGE.LIST_LOAD_FAILED);
  }

  return (
    <Orders
      orders={data}
      pagination={pagination}
      initialShopId={shopId}
      initialOrderStatus={orderStatus}
      initialOrderMethod={orderMethod}
      initialPaymentStatus={paymentStatus}
      initialOrderNumber={orderNumber}
      initialOrdererName={ordererName}
      initialStartDate={startDate}
      initialEndDate={endDate}
    />
  );
}
