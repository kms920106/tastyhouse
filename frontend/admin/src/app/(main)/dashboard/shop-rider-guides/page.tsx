import { shopService } from "@/api/shop/shop.service";
import { SHOP_RIDER_GUIDE_MESSAGE } from "@/feature/shop/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseSearchString } from "@/lib/utils";

import { ShopRiderGuides } from "./_components/shop-rider-guides";

const MAX_PAGE_SIZE = 100;

export default async function Page({ searchParams }: PageProps<"/dashboard/shop-rider-guides">) {
  const {
    page: pageParam,
    size: sizeParam,
    shopName: shopNameParam,
    hasVisitGuide: hasVisitGuideParam,
  } = await searchParams;

  const shopName = parseSearchString(shopNameParam);
  // 미지정이면 전체 조회이므로, "true" 일 때만 필터를 건다.
  const hasVisitGuide = parseSearchString(hasVisitGuideParam) === "true" ? true : undefined;
  const page = parseNonNegativeInt(pageParam, 0);
  const size = Math.min(parseNonNegativeInt(sizeParam, 10), MAX_PAGE_SIZE);

  const { error, data, pagination } = await shopService.getRiderGuides({ shopName, hasVisitGuide }, { page, size });

  if (error || !data || !pagination) {
    logger.error({ reason: error, data, pagination }, "라이더 안내 목록 조회 실패");
    throw new Error(SHOP_RIDER_GUIDE_MESSAGE.LIST_LOAD_FAILED);
  }

  return (
    <ShopRiderGuides
      riderGuides={data}
      pagination={pagination}
      initialShopName={shopName}
      initialHasVisitGuide={hasVisitGuide === true}
    />
  );
}
