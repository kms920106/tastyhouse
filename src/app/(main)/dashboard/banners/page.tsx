import { bannerService } from "@/api/banner/banner.service";
import type { BannerType } from "@/feature/banner/domain";
import { BANNER_MESSAGE } from "@/feature/banner/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseOptionalBoolean, parseSearchString } from "@/lib/utils";

import { Banners } from "./_components/banners";

function parseBannerType(value: string | undefined): BannerType | undefined {
  return value === "HOME" || value === "SIDEBAR" ? value : undefined;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/banners">) {
  const {
    page: pageParam,
    size: sizeParam,
    type: typeParam,
    title: titleParam,
    visible: visibleParam,
  } = await searchParams;

  const type = parseBannerType(parseSearchString(typeParam));
  const title = parseSearchString(titleParam);
  const visible = parseOptionalBoolean(visibleParam);
  const page = parseNonNegativeInt(pageParam, 0);
  const size = parseNonNegativeInt(sizeParam, 10);

  const { error, data, pagination } = await bannerService.getBanners({ type, title, visible }, { page, size });

  if (error || !data || !pagination) {
    logger.error({ reason: error, data, pagination }, "배너 목록 조회 실패");
    throw new Error(BANNER_MESSAGE.LIST_LOAD_FAILED);
  }

  return (
    <Banners banners={data} pagination={pagination} initialType={type} initialTitle={title} initialVisible={visible} />
  );
}
