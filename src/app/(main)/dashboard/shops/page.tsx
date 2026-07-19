import { shopService } from "@/api/shop/shop.service";
import { SHOP_MESSAGE } from "@/feature/shop/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseOptionalBoolean, parseSearchString } from "@/lib/utils";

import { Shops } from "./_components/shops";

const MAX_PAGE_SIZE = 100;

function parsePositiveInt(value: string | string[] | undefined): number | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  const parsed = Number(raw);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/shops">) {
  const {
    page: pageParam,
    size: sizeParam,
    name: nameParam,
    stationId: stationIdParam,
    permanentlyClosed: permanentlyClosedParam,
  } = await searchParams;

  const name = parseSearchString(nameParam);
  const stationId = parsePositiveInt(stationIdParam);
  const permanentlyClosed = parseOptionalBoolean(permanentlyClosedParam);
  const page = parseNonNegativeInt(pageParam, 0);
  const size = Math.min(parseNonNegativeInt(sizeParam, 10), MAX_PAGE_SIZE);

  const { error, data, pagination } = await shopService.getShops(
    { name, stationId, permanentlyClosed },
    { page, size },
  );

  if (error || !data || !pagination) {
    logger.error({ reason: error, data, pagination }, "가게 목록 조회 실패");
    throw new Error(SHOP_MESSAGE.LIST_LOAD_FAILED);
  }

  return (
    <Shops
      shops={data}
      pagination={pagination}
      initialName={name}
      initialStationId={stationId}
      initialPermanentlyClosed={permanentlyClosed}
    />
  );
}
