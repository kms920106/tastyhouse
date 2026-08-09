import { shopService } from "@/api/shop/shop.service";
import { fetchDeliveryAreaPolygonAction, getDeliveryAreasAction } from "@/feature/shop/actions";
import { SHOP_MESSAGE } from "@/feature/shop/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt } from "@/lib/utils";

import { DeliveryAreaEditor } from "./_components/delivery-area-editor";

/**
 * 배달지역 설정 전용 라우트.
 *
 * `shopId` 는 동적 세그먼트가 아니라 `searchParams` 로 받는다 — `shop/page.tsx` 가 이미
 * 그렇게 읽고 `shop-manage.tsx` 가 `URLSearchParams` 로 push 하는 확립된 컨벤션이다.
 */
export default async function Page({ searchParams }: PageProps<"/dashboard/shop/delivery-area">) {
  const { shopId: shopIdParam } = await searchParams;
  const requestedShopId = parseNonNegativeInt(shopIdParam, 0);

  const shopsResult = await shopService.getMyShops({}, { page: 0, size: 100 });
  if (shopsResult.error || !shopsResult.data) {
    logger.error({ reason: shopsResult.error }, "내 가게 목록 조회 실패");
    throw new Error(SHOP_MESSAGE.SHOP_LIST_LOAD_FAILED);
  }

  const shops = shopsResult.data;
  if (shops.length === 0) {
    throw new Error(SHOP_MESSAGE.SHOP_LIST_LOAD_FAILED);
  }

  // 보유하지 않은 가게를 찍고 들어와도 첫 가게로 대체한다 — 운영정보 탭과 같은 규칙.
  const selectedShop = shops.find((shop) => shop.id === requestedShopId) ?? shops[0];

  const [basicInfoResult, operationInfoResult, deliveryAreasResult, polygonResult] = await Promise.all([
    shopService.getShopBasicInfo(selectedShop.id),
    shopService.getShopOperationInfo(selectedShop.id),
    getDeliveryAreasAction(selectedShop.id),
    fetchDeliveryAreaPolygonAction(selectedShop.id),
  ]);

  if (basicInfoResult.error || !basicInfoResult.data) {
    logger.error({ reason: basicInfoResult.error, shopId: selectedShop.id }, "가게 기본정보 조회 실패");
    throw new Error(SHOP_MESSAGE.BASIC_INFO_LOAD_FAILED);
  }

  if (operationInfoResult.error || !operationInfoResult.data) {
    logger.error({ reason: operationInfoResult.error, shopId: selectedShop.id }, "가게 운영정보 조회 실패");
    throw new Error(SHOP_MESSAGE.OPERATION_INFO_LOAD_FAILED);
  }

  if (!deliveryAreasResult.success || !deliveryAreasResult.data) {
    logger.error({ reason: deliveryAreasResult.message, shopId: selectedShop.id }, "배달가능지역 조회 실패");
    throw new Error(SHOP_MESSAGE.DELIVERY_AREA_LOAD_FAILED);
  }

  // 도형 조회 실패는 화면을 막지 않는다 — 도형 없이도 검색·트리·반경 편집은 가능하다.
  if (!polygonResult.success) {
    logger.error(
      { reason: polygonResult.message, shopId: selectedShop.id },
      "배달지역 도형 조회 실패 — 도형 없이 렌더",
    );
  }

  const basicInfo = basicInfoResult.data;

  return (
    <DeliveryAreaEditor
      shopId={selectedShop.id}
      shopName={selectedShop.name}
      shop={{ latitude: basicInfo.latitude, longitude: basicInfo.longitude }}
      deliveryAreas={deliveryAreasResult.data}
      polygon={polygonResult.data ?? null}
      tipRegions={operationInfoResult.data.deliveryTip.regions}
    />
  );
}
