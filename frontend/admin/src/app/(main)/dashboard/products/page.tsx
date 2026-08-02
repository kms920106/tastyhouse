import { productService } from "@/api/product/product.service";
import { PRODUCT_MESSAGE } from "@/feature/product/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseOptionalBoolean, parseSearchString } from "@/lib/utils";

import { Products } from "./_components/products";

const MAX_PAGE_SIZE = 100;

function parsePositiveInt(value: string | string[] | undefined): number | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  const parsed = Number(raw);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/products">) {
  const {
    page: pageParam,
    size: sizeParam,
    shopId: shopIdParam,
    productCategoryId: productCategoryIdParam,
    name: nameParam,
    visible: visibleParam,
    soldOut: soldOutParam,
  } = await searchParams;

  const shopId = parsePositiveInt(shopIdParam);
  const productCategoryId = parsePositiveInt(productCategoryIdParam);
  const name = parseSearchString(nameParam);
  const visible = parseOptionalBoolean(visibleParam);
  const soldOut = parseOptionalBoolean(soldOutParam);
  const page = parseNonNegativeInt(pageParam, 0);
  const size = Math.min(parseNonNegativeInt(sizeParam, 10), MAX_PAGE_SIZE);

  const { error, data, pagination } = await productService.getProducts(
    { shopId, productCategoryId, name, visible, soldOut },
    { page, size },
  );

  if (error || !data || !pagination) {
    logger.error({ reason: error, data, pagination }, "상품 목록 조회 실패");
    throw new Error(PRODUCT_MESSAGE.LIST_LOAD_FAILED);
  }

  return (
    <Products
      products={data}
      pagination={pagination}
      initialShopId={shopId}
      initialName={name}
      initialVisible={visible}
      initialSoldOut={soldOut}
    />
  );
}
