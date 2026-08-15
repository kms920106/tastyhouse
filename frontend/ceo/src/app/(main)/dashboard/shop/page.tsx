import { shopService } from "@/api/shop/shop.service";
import { shopNoticeService } from "@/api/shop-notice/shop-notice.service";
import { SHOP_MANAGE_TABS, type ShopManageTab } from "@/feature/shop/constants";
import { SHOP_MESSAGE } from "@/feature/shop/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseSearchString } from "@/lib/utils";

import { ShopManage } from "./_components/shop-manage";

function parseTab(value: string | string[] | undefined): ShopManageTab {
  const raw = parseSearchString(value);
  if (raw === SHOP_MANAGE_TABS.OPERATION) return SHOP_MANAGE_TABS.OPERATION;
  if (raw === SHOP_MANAGE_TABS.ORDER) return SHOP_MANAGE_TABS.ORDER;
  return SHOP_MANAGE_TABS.BASIC;
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

  // shopId 미지정(기본 진입)만 첫 가게로 대체한다. shopId 를 지정했는데 내 목록에 없으면
  // — 소유하지 않은 가게이거나 권한이 말소된 가게이므로 — 조용히 다른 가게로 바꿔치기하지 않고
  // 지정된 shopId 그대로 백엔드에 조회를 위임해 403/404 인가 검증이 그대로 드러나게 한다.
  // (`requests/`·`change-history/` 와 같은 규칙 — 조용한 폴백은 "왜 다른 가게가 보이는지"를 감춘다.)
  const isShopIdSpecified = parseSearchString(shopIdParam) !== undefined;
  const matchedShop = shops.find((shop) => shop.id === requestedShopId);
  const selectedShopId = isShopIdSpecified ? requestedShopId : (matchedShop?.id ?? shops[0].id);

  const [basicInfoResult, operationInfoResult, orderAvailabilityResult, noticesResult] = await Promise.all([
    shopService.getShopBasicInfo(selectedShopId),
    shopService.getShopOperationInfo(selectedShopId),
    shopService.getShopOrderAvailability(selectedShopId),
    shopNoticeService.getNotices(selectedShopId),
  ]);

  // 접근 권한이 없거나 존재하지 않는 가게면 화면 전체를 에러로 덮지 않고 인라인 안내만 띄운다 —
  // 가게 선택기를 살려 두어야 사용자가 자기 가게로 되돌아갈 수 있다.
  const accessErrorCode = basicInfoResult.errorCode ?? operationInfoResult.errorCode;
  if (accessErrorCode === "SHOP_ACCESS_DENIED" || accessErrorCode === "SHOP_NOT_FOUND") {
    logger.warn({ errorCode: accessErrorCode, shopId: selectedShopId }, "접근할 수 없는 가게 — 인라인 안내로 렌더");
    return <ShopManage shops={shops} tab={tab} errorCode={accessErrorCode} />;
  }

  if (basicInfoResult.error || !basicInfoResult.data) {
    logger.error({ reason: basicInfoResult.error, shopId: selectedShopId }, "가게 기본정보 조회 실패");
    throw new Error(SHOP_MESSAGE.BASIC_INFO_LOAD_FAILED);
  }

  if (operationInfoResult.error || !operationInfoResult.data) {
    logger.error({ reason: operationInfoResult.error, shopId: selectedShopId }, "가게 운영정보 조회 실패");
    throw new Error(SHOP_MESSAGE.OPERATION_INFO_LOAD_FAILED);
  }

  // 주문가능 상태 조회 실패는 화면 전체를 막지 않는다 — 주문정보 탭에서만 실패 문구를 보여준다.
  if (orderAvailabilityResult.error || !orderAvailabilityResult.data) {
    logger.error(
      { reason: orderAvailabilityResult.error, shopId: selectedShopId },
      "가게 주문가능 상태 조회 실패 — 주문정보 탭만 실패로 렌더",
    );
  }

  // 공지 조회 실패도 화면을 막지 않는다 — 빈 목록으로 시트를 열어 새 공지를 등록할 수 있게 둔다.
  if (noticesResult.error || !noticesResult.data) {
    logger.error(
      { reason: noticesResult.error, shopId: selectedShopId },
      "가게 사장님 공지 조회 실패 — 빈 목록으로 렌더",
    );
  }

  return (
    <ShopManage
      shops={shops}
      shopId={selectedShopId}
      tab={tab}
      basicInfo={basicInfoResult.data}
      operationInfo={operationInfoResult.data}
      orderAvailability={orderAvailabilityResult.data}
      notices={noticesResult.data}
    />
  );
}
