import { shopService } from "@/api/shop/shop.service";
import { SHOP_MANAGE_TABS, type ShopManageTab } from "@/feature/shop/constants";
import { SHOP_MESSAGE } from "@/feature/shop/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseSearchString } from "@/lib/utils";

import { ShopManage } from "./_components/shop-manage";

function parseTab(value: string | string[] | undefined): ShopManageTab {
  const raw = parseSearchString(value);
  return raw === SHOP_MANAGE_TABS.OPERATION ? SHOP_MANAGE_TABS.OPERATION : SHOP_MANAGE_TABS.BASIC;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/shop">) {
  const { shopId: shopIdParam, tab: tabParam } = await searchParams;

  const tab = parseTab(tabParam);
  const requestedShopId = parseNonNegativeInt(shopIdParam, 0);

  const shopsResult = await shopService.getMyShops({}, { page: 0, size: 100 });
  if (shopsResult.error || !shopsResult.data) {
    logger.error({ reason: shopsResult.error }, "내 가게 목록 조회 실패");
    throw new Error(SHOP_MESSAGE.SHOP_LIST_LOAD_FAILED);
  }

  const shops = shopsResult.data;
  if (shops.length === 0) {
    return <ShopManage shops={[]} tab={tab} />;
  }

  // shopId 미지정 또는 보유하지 않은 가게면 첫 가게로 대체한다.
  const selectedShop = shops.find((shop) => shop.id === requestedShopId) ?? shops[0];

  const [basicInfoResult, operationInfoResult] = await Promise.all([
    shopService.getShopBasicInfo(selectedShop.id),
    shopService.getShopOperationInfo(selectedShop.id),
  ]);

  if (basicInfoResult.error || !basicInfoResult.data) {
    logger.error({ reason: basicInfoResult.error, shopId: selectedShop.id }, "가게 기본정보 조회 실패");
    throw new Error(SHOP_MESSAGE.BASIC_INFO_LOAD_FAILED);
  }

  if (operationInfoResult.error || !operationInfoResult.data) {
    logger.error({ reason: operationInfoResult.error, shopId: selectedShop.id }, "가게 운영정보 조회 실패");
    throw new Error(SHOP_MESSAGE.OPERATION_INFO_LOAD_FAILED);
  }

  return (
    <ShopManage
      shops={shops}
      shopId={selectedShop.id}
      tab={tab}
      basicInfo={basicInfoResult.data}
      operationInfo={operationInfoResult.data}
    />
  );
}
