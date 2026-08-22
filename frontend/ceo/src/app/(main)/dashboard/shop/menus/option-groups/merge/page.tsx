import { productRepository } from "@/api/product/product.repository";
import { shopService } from "@/api/shop/shop.service";
import { MY_SHOP_LIST_SIZE, OPTION_GROUP_MERGE_MODES } from "@/feature/product/constants";
import type { OptionGroupMergeMode } from "@/feature/product/domain";
import { PRODUCT_MESSAGE } from "@/feature/product/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseSearchString } from "@/lib/utils";

import { MergeManage } from "./_components/merge-manage";

/** `?mode=` 를 검증한다 — 알 수 없는 값이면 추천(기본)으로 떨어뜨린다 */
function parseMode(value: string | undefined): OptionGroupMergeMode {
  return value === OPTION_GROUP_MERGE_MODES.MANUAL
    ? OPTION_GROUP_MERGE_MODES.MANUAL
    : OPTION_GROUP_MERGE_MODES.RECOMMENDED;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/shop/menus/option-groups/merge">) {
  const { shopId: shopIdParam, mode: modeParam, keyword: keywordParam } = await searchParams;

  const requestedShopId = parseNonNegativeInt(shopIdParam, 0);
  const mode = parseMode(parseSearchString(modeParam));
  const keyword = parseSearchString(keywordParam);

  const shopsResult = await shopService.getMyShops({}, { page: 0, size: MY_SHOP_LIST_SIZE });

  // 가게 목록이 없으면 어떤 가게의 옵션그룹을 합칠지 정할 수 없어 화면 자체가 성립하지 않는다.
  if (shopsResult.error || !shopsResult.data) {
    logger.error({ reason: shopsResult.error }, "내 가게 목록 조회 실패");
    throw new Error(PRODUCT_MESSAGE.SHOP_LIST_LOAD_FAILED);
  }

  const shops = shopsResult.data;

  if (shops.length === 0) {
    return <MergeManage shops={[]} mode={mode} />;
  }

  // shopId 미지정만 첫 가게로 대체한다. 지정했는데 내 목록에 없으면 조용히 바꿔치기하지 않고
  // 백엔드에 위임해 403/404 인가 검증이 드러나게 한다(옵션그룹 관리 화면 선례).
  const isShopIdSpecified = parseSearchString(shopIdParam) !== undefined;
  const matchedShop = shops.find((shop) => shop.id === requestedShopId);
  const shopId = isShopIdSpecified ? requestedShopId : (matchedShop?.id ?? shops[0].id);

  // 두 모드가 서로 다른 데이터를 쓰지만 둘 다 미리 받아 둔다 — 탭 전환마다 서버 왕복을 넣으면
  // 사용자가 추천/직접을 오가며 비교하는 이 화면의 조작이 매번 끊긴다.
  const [suggestionsResult, groupsResult] = await Promise.all([
    productRepository.getOptionGroupMergeSuggestions(shopId),
    productRepository.getOptionGroups(shopId),
  ]);

  if (!suggestionsResult.data) {
    logger.error(
      { reason: suggestionsResult.error, errorCode: suggestionsResult.errorCode, shopId },
      "옵션그룹 합치기 추천 조회 실패",
    );
  }

  /**
   * 검색은 **서버가 아니라 여기서** 걸러 낸다.
   *
   * `GET /option-groups` 에 `keyword` 파라미터가 없고(품절·숨김 조회와 달리 가게의 옵션그룹 전체를
   * 그대로 내려준다) 이 화면은 그 전체 목록을 이미 손에 들고 있다. 서버에 파라미터를 추가하는 대신
   * 여기서 거르는 편이 왕복도 없고 선택 상태(체크박스)도 유지된다.
   *
   * 대소문자 구분은 없애지만 한글은 영향이 없다 — 영문 옵션그룹명을 위한 처리다.
   */
  // 감춘(삭제한) 그룹은 애초에 후보가 아니다 — 고르게 두면 서버가
  // `PRODUCT_OPTION_GROUP_MERGE_HIDDEN_TARGET` 으로 거부하므로 선택 단계에서 뺀다.
  const selectableGroups = groupsResult.data?.filter((group) => group.visible);

  const filteredGroups =
    selectableGroups === undefined || keyword === undefined
      ? selectableGroups
      : selectableGroups.filter((group) => group.name.toLowerCase().includes(keyword.toLowerCase()));

  return (
    <MergeManage
      shops={shops}
      shopId={shopId}
      mode={mode}
      keyword={keyword}
      suggestions={suggestionsResult.data}
      optionGroups={filteredGroups}
      allOptionGroups={selectableGroups}
      errorCode={suggestionsResult.errorCode ?? groupsResult.errorCode}
      errorMessage={suggestionsResult.error ?? groupsResult.error}
    />
  );
}
