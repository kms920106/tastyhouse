import { shopService } from "@/api/shop/shop.service";
import { SHOP_MESSAGE } from "@/feature/shop/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseOptionalBoolean, parseSearchString } from "@/lib/utils";

import { ShopContentBoards } from "./_components/shop-content-boards";

const MAX_PAGE_SIZE = 100;

function parsePositiveInt(value: string | string[] | undefined): number | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  const parsed = Number(raw);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/shop-content-boards">) {
  const {
    page: pageParam,
    size: sizeParam,
    shopId: shopIdParam,
    hidden: hiddenParam,
    contentType: contentTypeParam,
  } = await searchParams;

  const shopId = parsePositiveInt(shopIdParam);
  const hidden = parseOptionalBoolean(hiddenParam);
  const contentType = parseSearchString(contentTypeParam) as "IMAGE" | "GIF" | "VIDEO" | undefined;
  const page = parseNonNegativeInt(pageParam, 0);
  const size = Math.min(parseNonNegativeInt(sizeParam, 10), MAX_PAGE_SIZE);

  const { error, data, pagination } = await shopService.getContentBoards(
    { shopId, hidden, contentType },
    { page, size },
  );

  if (error || !data || !pagination) {
    logger.error({ reason: error, data, pagination }, "콘텐츠보드 목록 조회 실패");
    throw new Error(SHOP_MESSAGE.CONTENT_BOARDS_LOAD_FAILED);
  }

  return (
    <ShopContentBoards
      contentBoards={data}
      pagination={pagination}
      initialShopId={shopId}
      initialHidden={hidden}
      initialContentType={contentType}
    />
  );
}
