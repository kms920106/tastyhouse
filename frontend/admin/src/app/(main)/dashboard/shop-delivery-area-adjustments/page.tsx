import { shopService } from "@/api/shop/shop.service";
import type { DeliveryAreaAdjustmentStatus } from "@/feature/shop/domain";
import { DELIVERY_AREA_ADJUSTMENT_MESSAGE } from "@/feature/shop/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt } from "@/lib/utils";

import { ShopDeliveryAreaAdjustments } from "./_components/shop-delivery-area-adjustments";

const ADJUSTMENT_STATUSES: readonly DeliveryAreaAdjustmentStatus[] = [
  "PENDING",
  "IN_PROGRESS",
  "COMPLETED",
  "REJECTED",
];

const MAX_PAGE_SIZE = 100;

// 화이트리스트 검증 — 임의 문자열이 그대로 서버 쿼리로 넘어가지 않게 한다.
function parseAdjustmentStatus(value: string | string[] | undefined): DeliveryAreaAdjustmentStatus | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  return ADJUSTMENT_STATUSES.includes(raw as DeliveryAreaAdjustmentStatus)
    ? (raw as DeliveryAreaAdjustmentStatus)
    : undefined;
}

/** 가게 ID 필터 — 양의 정수만 통과시킨다 */
function parseShopId(value: string | string[] | undefined): number | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  const parsed = Number(raw);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/shop-delivery-area-adjustments">) {
  const { page: pageParam, size: sizeParam, status: statusParam, shopId: shopIdParam } = await searchParams;

  const status = parseAdjustmentStatus(statusParam);
  const shopId = parseShopId(shopIdParam);
  const page = parseNonNegativeInt(pageParam, 0);
  const size = Math.min(parseNonNegativeInt(sizeParam, 10), MAX_PAGE_SIZE);

  const { error, data, pagination } = await shopService.getDeliveryAreaAdjustments({ status, shopId }, { page, size });

  if (error || !data || !pagination) {
    logger.error({ reason: error, data, pagination }, "배달지역 조정 신청 목록 조회 실패");
    throw new Error(DELIVERY_AREA_ADJUSTMENT_MESSAGE.LIST_LOAD_FAILED);
  }

  return (
    <ShopDeliveryAreaAdjustments
      requests={data}
      pagination={pagination}
      initialStatus={status}
      initialShopId={shopId}
    />
  );
}
