import { productRepository } from "@/api/product/product.repository";
import { shopRepository } from "@/api/shop/shop.repository";
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
    return <OptionGroupManage shops={[]} linkableProducts={[]} />;
  }

  // shopId 미지정(기본 진입)만 첫 가게로 대체한다. 지정했는데 내 목록에 없으면 조용히 바꿔치기하지
  // 않고 그대로 백엔드에 위임해 403/404 인가 검증이 드러나게 한다(`availability/page.tsx` 선례).
  const isShopIdSpecified = parseSearchString(shopIdParam) !== undefined;
  const matchedShop = shops.find((shop) => shop.id === requestedShopId);
  const shopId = isShopIdSpecified ? requestedShopId : (matchedShop?.id ?? shops[0].id);

  // 네 조회를 병렬로 돌린다 — 서로 의존하지 않고, 합치기 배너·유형 선택·메뉴 후보 목록은 그룹
  // 목록 렌더를 막을 만한 정보가 아니라 실패해도 화면이 성립해야 한다.
  const [listResult, suggestionsResult, shopDetailResult, availabilityResult] = await Promise.all([
    productRepository.getOptionGroups(shopId),
    productRepository.getOptionGroupMergeSuggestions(shopId),
    shopRepository.getDetail(shopId),
    productRepository.getAvailability({ shopId }),
  ]);

  // 목록 실패는 throw 하지 않는다 — 가게 선택기와 [옵션그룹 추가] 를 살려 사용자가 가게를 바꾸거나
  // 재시도할 수 있게 한다. (소유하지 않거나 존재하지 않는 shopId 는 여기서 403/404 로 드러난다.)
  if (!listResult.data) {
    logger.error({ reason: listResult.error, errorCode: listResult.errorCode, shopId }, "옵션그룹 목록 조회 실패");
  }

  // 등록 시 연결할 메뉴 후보 — 실패해도 등록 폼이 "메뉴가 없다"로 보일 뿐 화면 전체를 막지 않는다.
  if (!availabilityResult.data) {
    logger.warn(
      { reason: availabilityResult.error, errorCode: availabilityResult.errorCode, shopId },
      "메뉴 목록 조회 실패 — 옵션그룹 등록 폼의 메뉴 선택지가 비어 보임",
    );
  }
  const linkableProducts = (availabilityResult.data ?? []).flatMap((group) =>
    group.products.map((product) => ({ id: product.id, name: product.name })),
  );

  // 추천·가게상세 실패는 배너와 유형 선택을 숨기는 것으로 흡수한다 — 부가 정보가 없다고 옵션그룹
  // 관리 자체를 막으면 손해가 더 크다.
  if (!suggestionsResult.data) {
    logger.warn(
      { reason: suggestionsResult.error, errorCode: suggestionsResult.errorCode, shopId },
      "옵션그룹 합치기 추천 조회 실패 — 배너 숨김",
    );
  }

  /**
   * 감춘(삭제한) 그룹·옵션을 걸러 낸다.
   *
   * 서버는 소프트 삭제를 쓰고 이 목록에 `visible: false` 행도 함께 내려준다 — 합치기 미리보기가
   * "합치면 무엇이 남는가"를 보여주려면 감춘 것까지 알아야 하기 때문이다. 관리 화면은 반대로
   * 살아 있는 것만 보여야 하므로 여기서 걸러내지 않으면 삭제한 옵션이 되살아난 것처럼 보인다.
   */
  const visibleGroups = listResult.data
    ?.filter((group) => group.visible)
    .map((group) => ({ ...group, options: group.options.filter((option) => option.visible) }));

  return (
    <OptionGroupManage
      shops={shops}
      shopId={shopId}
      optionGroups={visibleGroups}
      linkableProducts={linkableProducts}
      errorCode={listResult.errorCode}
      errorMessage={listResult.error}
      mergeSuggestionCount={suggestionsResult.data?.length}
      cupDepositEnabled={shopDetailResult.data?.cupDepositEnabled}
    />
  );
}
