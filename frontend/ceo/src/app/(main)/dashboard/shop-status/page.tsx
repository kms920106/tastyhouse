import { shopRepository } from "@/api/shop/shop.repository";
import { shopService } from "@/api/shop/shop.service";
import type { Suspension } from "@/feature/shop/domain";
import { SHOP_MESSAGE } from "@/feature/shop/message";
import logger from "@/lib/logger";

import { ShopStatusOverview, type ShopStatusRow } from "./_components/shop-status-overview";

export default async function Page() {
  const shopsResult = await shopService.getMyShops({}, { page: 0, size: 100 });

  if (shopsResult.error || !shopsResult.data) {
    logger.error({ reason: shopsResult.error }, "내 가게 목록 조회 실패");
    throw new Error(SHOP_MESSAGE.SHOP_LIST_LOAD_FAILED);
  }

  const shops = shopsResult.data;

  // 스펙에는 전체현황 요약 엔드포인트가 없어, 가게별 임시중지 목록을 병렬 조회해 직접 집계한다.
  const suspensionResults = await Promise.all(shops.map((shop) => shopRepository.getSuspensions(shop.id)));

  const rows: ShopStatusRow[] = shops.map((shop, index) => {
    const result = suspensionResults[index];
    if (result.error !== undefined) {
      // 일부 가게 조회 실패가 화면 전체를 막지 않도록, 해당 가게만 상태 미확인으로 표시한다.
      logger.error({ reason: result.error, shopId: shop.id }, "가게 임시중지 조회 실패");
    }

    const suspensions: Suspension[] = (result.data ?? []).map((item) => ({
      id: item.id,
      shopId: item.shopId,
      reason: item.reason,
      orderMethod: item.orderMethod,
      startAt: item.startAt,
      endAt: item.endAt,
      releasedAt: item.releasedAt,
    }));

    return {
      shopId: shop.id,
      shopName: shop.name,
      permanentlyClosed: shop.permanentlyClosed,
      loadFailed: result.error !== undefined,
      // 해제되지 않은(releasedAt === null) 건이 하나라도 있으면 임시중지 상태로 본다.
      activeSuspensions: suspensions.filter((item) => item.releasedAt === null),
    };
  });

  return <ShopStatusOverview rows={rows} />;
}
