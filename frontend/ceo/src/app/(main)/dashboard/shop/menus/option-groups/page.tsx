import { productRepository } from "@/api/product/product.repository";
import { shopService } from "@/api/shop/shop.service";
import { MY_SHOP_LIST_SIZE } from "@/feature/product/constants";
import { PRODUCT_MESSAGE } from "@/feature/product/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseSearchString } from "@/lib/utils";

import { OptionGroupManage } from "./_components/option-group-manage";

export default async function Page({ searchParams }: PageProps<"/dashboard/shop/menus/option-groups">) {
  const { shopId: shopIdParam } = await searchParams;

  const requestedShopId = parseNonNegativeInt(shopIdParam, 0);

  const shopsResult = await shopService.getMyShops({}, { page: 0, size: MY_SHOP_LIST_SIZE });

  // 가게 목록이 없으면 어떤 가게의 옵션그룹인지 정할 수 없어 화면 자체가 성립하지 않는다.
  if (shopsResult.error || !shopsResult.data) {
    logger.error({ reason: shopsResult.error }, "내 가게 목록 조회 실패");
    throw new Error(PRODUCT_MESSAGE.SHOP_LIST_LOAD_FAILED);
  }

  const shops = shopsResult.data;

  if (shops.length === 0) {
    return <OptionGroupManage shops={[]} />;
  }

  // shopId 미지정(기본 진입)만 첫 가게로 대체한다. 지정했는데 내 목록에 없으면 조용히 바꿔치기하지
  // 않고 그대로 백엔드에 위임해 403/404 인가 검증이 드러나게 한다(`availability/page.tsx` 선례).
  const isShopIdSpecified = parseSearchString(shopIdParam) !== undefined;
  const matchedShop = shops.find((shop) => shop.id === requestedShopId);
  const shopId = isShopIdSpecified ? requestedShopId : (matchedShop?.id ?? shops[0].id);

  const listResult = await productRepository.getOptionGroups(shopId);

  // 목록 실패는 throw 하지 않는다 — 가게 선택기와 [옵션그룹 추가] 를 살려 사용자가 가게를 바꾸거나
  // 재시도할 수 있게 한다. (소유하지 않거나 존재하지 않는 shopId 는 여기서 403/404 로 드러난다.)
  if (!listResult.data) {
    logger.error({ reason: listResult.error, errorCode: listResult.errorCode, shopId }, "옵션그룹 목록 조회 실패");
  }

  return (
    <OptionGroupManage
      shops={shops}
      shopId={shopId}
      optionGroups={listResult.data}
      errorCode={listResult.errorCode}
      errorMessage={listResult.error}
    />
  );
}
