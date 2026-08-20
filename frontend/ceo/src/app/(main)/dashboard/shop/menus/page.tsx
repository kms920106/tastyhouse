import { productRepository } from "@/api/product/product.repository";
import { shopService } from "@/api/shop/shop.service";
import { MY_SHOP_LIST_SIZE } from "@/feature/product/constants";
import { PRODUCT_MESSAGE } from "@/feature/product/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseSearchString } from "@/lib/utils";

import { MenuBoardManage } from "./_components/menu-board-manage";

export default async function Page({ searchParams }: PageProps<"/dashboard/shop/menus">) {
  const { shopId: shopIdParam } = await searchParams;

  const requestedShopId = parseNonNegativeInt(shopIdParam, 0);

  const shopsResult = await shopService.getMyShops({}, { page: 0, size: MY_SHOP_LIST_SIZE });

  // 가게 목록이 없으면 어떤 가게의 메뉴판인지 정할 수 없어 화면 자체가 성립하지 않는다.
  if (shopsResult.error || !shopsResult.data) {
    logger.error({ reason: shopsResult.error }, "내 가게 목록 조회 실패");
    throw new Error(PRODUCT_MESSAGE.SHOP_LIST_LOAD_FAILED);
  }

  const shops = shopsResult.data;

  if (shops.length === 0) {
    return <MenuBoardManage shops={[]} />;
  }

  // shopId 미지정(기본 진입)만 첫 가게로 대체한다. 지정했는데 내 목록에 없으면 조용히 바꿔치기하지
  // 않고 그대로 백엔드에 위임해 403/404 인가 검증이 드러나게 한다(품절·숨김 화면 선례).
  const isShopIdSpecified = parseSearchString(shopIdParam) !== undefined;
  const matchedShop = shops.find((shop) => shop.id === requestedShopId);
  const shopId = isShopIdSpecified ? requestedShopId : (matchedShop?.id ?? shops[0].id);

  // 메뉴판은 그룹 목록과 메뉴 목록을 함께 보여준다. 메뉴가 하나도 없는 빈 그룹은 메뉴 조회 응답에
  // 나타나지 않으므로(그룹은 메뉴를 품은 형태로 내려온다) 그룹 목록을 따로 받아 합친다 —
  // 그러지 않으면 방금 만든 빈 메뉴그룹이 화면에서 사라져 [메뉴그룹 추가]가 먹지 않는 것처럼 보인다.
  const [categoriesResult, boardResult] = await Promise.all([
    productRepository.getCategories(shopId),
    productRepository.getAvailability({ shopId }),
  ]);

  // 목록 실패는 throw 하지 않는다 — 가게 선택기를 살려 다른 가게로 옮기거나 재시도할 수 있게 한다.
  // (소유하지 않거나 존재하지 않는 shopId 는 여기서 403/404 로 드러난다.)
  if (!boardResult.data || !categoriesResult.data) {
    logger.error(
      {
        reason: boardResult.error ?? categoriesResult.error,
        errorCode: boardResult.errorCode ?? categoriesResult.errorCode,
        shopId,
      },
      "메뉴판 목록 조회 실패 — 셸만 렌더",
    );
  }

  return (
    <MenuBoardManage
      shops={shops}
      shopId={shopId}
      categories={categoriesResult.data}
      groups={boardResult.data}
      errorCode={boardResult.errorCode ?? categoriesResult.errorCode}
      errorMessage={boardResult.error ?? categoriesResult.error}
    />
  );
}
