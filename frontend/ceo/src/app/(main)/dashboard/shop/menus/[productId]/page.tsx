import { productRepository } from "@/api/product/product.repository";
import { shopService } from "@/api/shop/shop.service";
import { MY_SHOP_LIST_SIZE } from "@/feature/product/constants";
import type { MenuCategory, MenuDetail, MenuOptionGroup } from "@/feature/product/domain";
import { PRODUCT_MESSAGE } from "@/feature/product/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt } from "@/lib/utils";

import { MenuDetailManage } from "./_components/menu-detail-manage";

export default async function Page({ params, searchParams }: PageProps<"/dashboard/shop/menus/[productId]">) {
  const { productId: productIdParam } = await params;
  const { shopId: shopIdParam } = await searchParams;

  const productId = parseNonNegativeInt(productIdParam, 0);
  // shopId 는 동적 세그먼트가 아니라 searchParam 이다(이 앱의 확립된 규칙).
  // 지정된 값이 내 가게가 아니어도 조용히 첫 가게로 바꿔치기하지 않는다 — 그대로 백엔드에 위임해
  // 403/404 인가 검증이 드러나게 한다(`availability/page.tsx` 선례).
  const shopId = parseNonNegativeInt(shopIdParam, 0);

  const shopsResult = await shopService.getMyShops({}, { page: 0, size: MY_SHOP_LIST_SIZE });

  // 가게 목록이 없으면 어떤 가게의 메뉴인지 판단할 수 없어 화면 자체가 성립하지 않는다.
  if (shopsResult.error || !shopsResult.data) {
    logger.error({ reason: shopsResult.error }, "내 가게 목록 조회 실패");
    throw new Error(PRODUCT_MESSAGE.SHOP_LIST_LOAD_FAILED);
  }

  const detailResult = await productRepository.getProductDetail(productId, shopId);

  // 상세 실패는 throw 하지 않는다 — Sheet 를 열지 않고 인라인 안내만 남긴다(`frontend.md` §7).
  if (!detailResult.data) {
    logger.error(
      { reason: detailResult.error, errorCode: detailResult.errorCode, productId, shopId },
      "메뉴 상세 조회 실패 — 인라인 안내만 렌더",
    );

    return <MenuDetailManage productId={productId} shopId={shopId} errorMessage={detailResult.error} />;
  }

  // 메뉴그룹 Select 와 옵션그룹 연결 후보는 상세와 독립적으로 실패할 수 있다.
  // 한쪽이 비어도 나머지 행은 계속 조작할 수 있어야 하므로 빈 배열로 떨어뜨린다.
  const [categoriesResult, optionGroupsResult] = await Promise.all([
    productRepository.getCategories(shopId),
    productRepository.getOptionGroups(shopId),
  ]);

  if (categoriesResult.error) {
    logger.error({ reason: categoriesResult.error, shopId }, "메뉴그룹 목록 조회 실패");
  }
  if (optionGroupsResult.error) {
    logger.error({ reason: optionGroupsResult.error, shopId }, "옵션그룹 목록 조회 실패");
  }

  const detail: MenuDetail = detailResult.data;
  const categories: MenuCategory[] = categoriesResult.data ?? [];
  const optionGroups: MenuOptionGroup[] = optionGroupsResult.data ?? [];

  // 이 메뉴에 어떤 그룹이 연결됐는지를 알려주는 단건 엔드포인트가 없어, 가게의 그룹마다
  // "이 그룹을 쓰는 메뉴 목록"(§5-2)을 읽어 역으로 판정한다. 이 응답은 해제 전 영향 안내
  // (다른 메뉴 N개에서 사용 중)에도 그대로 쓰이므로 조회가 두 번 필요하지 않다.
  // 그룹마다 개별 조회하면 옵션그룹이 많은 가게에서 N+1이 되므로, 가게 단위 벌크 조회 한 번으로 받는다.
  const linkedProductsResult = await productRepository.getOptionGroupsLinkedProducts(shopId);
  if (linkedProductsResult.error) {
    logger.error({ reason: linkedProductsResult.error, shopId }, "옵션그룹 연결 메뉴 벌크 조회 실패");
  }

  const linkedProductsByGroupId = Object.fromEntries(
    optionGroups.map((group) => [
      group.id,
      (linkedProductsResult.data ?? []).find((item) => item.optionGroupId === group.id)?.products ?? [],
    ]),
  );

  return (
    <MenuDetailManage
      productId={productId}
      shopId={shopId}
      detail={detail}
      categories={categories}
      optionGroups={optionGroups}
      linkedProductsByGroupId={linkedProductsByGroupId}
    />
  );
}
