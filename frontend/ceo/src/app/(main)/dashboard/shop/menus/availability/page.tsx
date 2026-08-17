import { productRepository } from "@/api/product/product.repository";
import { shopService } from "@/api/shop/shop.service";
import { AVAILABILITY_KEYWORD_MAX_LENGTH, AVAILABILITY_TABS, MY_SHOP_LIST_SIZE } from "@/feature/product/constants";
import type { AvailabilityTab } from "@/feature/product/domain";
import { PRODUCT_MESSAGE } from "@/feature/product/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseOptionalBoolean, parseSearchString } from "@/lib/utils";

import { AvailabilityManage } from "./_components/availability-manage";

export default async function Page({ searchParams }: PageProps<"/dashboard/shop/menus/availability">) {
  const {
    shopId: shopIdParam,
    keyword: keywordParam,
    soldOutOnly: soldOutOnlyParam,
    hiddenOnly: hiddenOnlyParam,
    tab: tabParam,
  } = await searchParams;

  const requestedShopId = parseNonNegativeInt(shopIdParam, 0);

  const shopsResult = await shopService.getMyShops({}, { page: 0, size: MY_SHOP_LIST_SIZE });

  // 가게 목록이 없으면 어떤 가게의 메뉴인지 정할 수 없어 화면 자체가 성립하지 않는다.
  if (shopsResult.error || !shopsResult.data) {
    logger.error({ reason: shopsResult.error }, "내 가게 목록 조회 실패");
    throw new Error(PRODUCT_MESSAGE.SHOP_LIST_LOAD_FAILED);
  }

  const shops = shopsResult.data;

  const tab: AvailabilityTab =
    tabParam === AVAILABILITY_TABS.OPTION ? AVAILABILITY_TABS.OPTION : AVAILABILITY_TABS.MENU;
  // 서버가 100자로 제한하므로 넘치는 검색어는 잘라 400 대신 조용히 좁혀 조회한다.
  const keyword = parseSearchString(keywordParam)?.slice(0, AVAILABILITY_KEYWORD_MAX_LENGTH);
  const soldOutOnly = parseOptionalBoolean(soldOutOnlyParam);
  const hiddenOnly = parseOptionalBoolean(hiddenOnlyParam);

  const filters = { keyword, soldOutOnly, hiddenOnly, tab };

  if (shops.length === 0) {
    return <AvailabilityManage shops={[]} filters={filters} />;
  }

  // shopId 미지정(기본 진입)만 첫 가게로 대체한다. 지정했는데 내 목록에 없으면 조용히 바꿔치기하지
  // 않고 그대로 백엔드에 위임해 403/404 인가 검증이 드러나게 한다(리뷰 화면 선례).
  const isShopIdSpecified = parseSearchString(shopIdParam) !== undefined;
  const matchedShop = shops.find((shop) => shop.id === requestedShopId);
  const shopId = isShopIdSpecified ? requestedShopId : (matchedShop?.id ?? shops[0].id);

  const searchRequest = { shopId, keyword, soldOutOnly, hiddenOnly };

  // 탭마다 조회 엔드포인트가 다르고 서버가 필터링하므로, 보고 있는 탭만 조회한다.
  // 두 호출의 응답 타입이 달라 삼항으로 합치면 유니온이 되므로 분기별로 따로 받는다.
  const menuResult =
    tab === AVAILABILITY_TABS.MENU ? await productRepository.getAvailability(searchRequest) : undefined;
  const optionResult =
    tab === AVAILABILITY_TABS.OPTION ? await productRepository.getOptionAvailability(searchRequest) : undefined;

  const listResult = menuResult ?? optionResult;

  // 목록 실패는 throw 하지 않는다 — 검색바·필터·탭을 살려 사용자가 조건을 고쳐 재시도할 수 있게 한다.
  // (소유하지 않거나 존재하지 않는 shopId 는 여기서 403/404 로 드러난다.)
  if (!listResult?.data) {
    logger.error(
      { reason: listResult?.error, errorCode: listResult?.errorCode, shopId, tab },
      "품절·숨김 목록 조회 실패 — 필터바만 렌더",
    );
  }

  return (
    <AvailabilityManage
      shops={shops}
      shopId={shopId}
      filters={filters}
      menuGroups={menuResult?.data}
      optionGroups={optionResult?.data}
      errorCode={listResult?.errorCode}
      errorMessage={listResult?.error}
    />
  );
}
