import { couponService } from "@/api/coupon/coupon.service";
import type { DiscountType } from "@/feature/coupon/domain";
import { COUPON_MESSAGE } from "@/feature/coupon/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseOptionalBoolean, parseSearchString } from "@/lib/utils";

import { Coupons } from "./_components/coupons";

function parseDiscountType(value: string | string[] | undefined): DiscountType | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  return raw === "AMOUNT" || raw === "RATE" ? raw : undefined;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/coupons">) {
  const {
    page: pageParam,
    size: sizeParam,
    name: nameParam,
    discountType: discountTypeParam,
    visible: visibleParam,
  } = await searchParams;

  const name = parseSearchString(nameParam);
  const discountType = parseDiscountType(discountTypeParam);
  const visible = parseOptionalBoolean(visibleParam);
  const page = parseNonNegativeInt(pageParam, 0);
  const size = parseNonNegativeInt(sizeParam, 10);

  const { error, data, pagination } = await couponService.getCoupons({ name, discountType, visible }, { page, size });

  if (error || !data || !pagination) {
    logger.error({ reason: error, data, pagination }, "쿠폰 목록 조회 실패");
    throw new Error(COUPON_MESSAGE.LIST_LOAD_FAILED);
  }

  return (
    <Coupons
      coupons={data}
      pagination={pagination}
      initialName={name}
      initialDiscountType={discountType}
      initialVisible={visible}
    />
  );
}
