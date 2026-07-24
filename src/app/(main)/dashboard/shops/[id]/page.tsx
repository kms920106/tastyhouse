import { shopService } from "@/api/shop/shop.service";
import { SHOP_MESSAGE } from "@/feature/shop/message";
import logger from "@/lib/logger";

import { ShopDetail } from "./_components/shop-detail";

export default async function Page({ params }: PageProps<"/dashboard/shops/[id]">) {
  const { id: idParam } = await params;
  const id = Number(idParam);

  if (!Number.isInteger(id) || id <= 0) {
    throw new Error(SHOP_MESSAGE.DETAIL_LOAD_FAILED);
  }

  const { error, data } = await shopService.getShop(id);

  if (error || !data) {
    logger.error({ reason: error, shopId: id }, "가게 상세 조회 실패");
    throw new Error(SHOP_MESSAGE.DETAIL_LOAD_FAILED);
  }

  return <ShopDetail shop={data} />;
}
