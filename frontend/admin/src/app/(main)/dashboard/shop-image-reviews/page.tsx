import { shopService } from "@/api/shop/shop.service";
import { SHOP_MESSAGE } from "@/feature/shop/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseSearchString } from "@/lib/utils";

import { ShopImageReviews } from "./_components/shop-image-reviews";

const MAX_PAGE_SIZE = 100;

export default async function Page({ searchParams }: PageProps<"/dashboard/shop-image-reviews">) {
  const { page: pageParam, size: sizeParam, status: statusParam, imageType: imageTypeParam } = await searchParams;

  const status = parseSearchString(statusParam) as "PENDING" | "APPROVED" | "REJECTED" | undefined;
  const imageType = parseSearchString(imageTypeParam) as "TRADEMARK" | "THUMBNAIL" | undefined;
  const page = parseNonNegativeInt(pageParam, 0);
  const size = Math.min(parseNonNegativeInt(sizeParam, 10), MAX_PAGE_SIZE);

  const { error, data, pagination } = await shopService.getImageChangeRequests({ status, imageType }, { page, size });

  if (error || !data || !pagination) {
    logger.error({ reason: error, data, pagination }, "이미지 변경요청 목록 조회 실패");
    throw new Error(SHOP_MESSAGE.IMAGE_CHANGE_REQUESTS_LOAD_FAILED);
  }

  return (
    <ShopImageReviews requests={data} pagination={pagination} initialStatus={status} initialImageType={imageType} />
  );
}
