import { shopService } from "@/api/shop/shop.service";
import { shopChangeHistoryService } from "@/api/shop-change-history/shop-change-history.service";
import type { ShopChangeCategoryOption } from "@/feature/shop/domain";
import { SHOP_CHANGE_HISTORY_COPY, SHOP_MESSAGE } from "@/feature/shop/message";
import { formatDate } from "@/lib/date";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseSearchString } from "@/lib/utils";

import { ChangeHistoryView } from "./_components/change-history-view";

const PAGE_SIZE = 10;
const RETENTION_MONTHS = 6;
const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

/**
 * `yyyy-MM-dd` 형식이 아니면 오늘로 폴백한다. 범위(최근 6개월) 판정은 서버가 하므로
 * 여기서는 형식만 본다 — 범위를 프론트에서 잘라내면 사용자에게 "왜 비었는지"가 안 보인다.
 */
function parseChangedDate(value: string | string[] | undefined, today: string): string {
  const raw = parseSearchString(value);
  return raw && DATE_PATTERN.test(raw) ? raw : today;
}

/**
 * 서버 카탈로그에 존재하는 코드만 통과시킨다. URL 을 직접 편집해 임의 문자열이 들어오면
 * 백엔드가 400(`SHOP_CHANGE_CATEGORY_UNKNOWN`)을 던져 화면 전체가 에러 상태가 되는데,
 * 그 경우까지 에러 화면으로 보낼 이유가 없으므로 필터를 무시한다.
 */
function parseCategory(value: string | string[] | undefined, categories: ShopChangeCategoryOption[]) {
  const raw = parseSearchString(value);
  return raw && categories.some((category) => category.code === raw) ? raw : undefined;
}

function parseChangeType(value: string | string[] | undefined, categories: ShopChangeCategoryOption[]) {
  const raw = parseSearchString(value);
  const exists = categories.some((category) => category.changeTypes.some((changeType) => changeType.code === raw));
  return raw && exists ? raw : undefined;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/shop/change-history">) {
  const {
    shopId: shopIdParam,
    category: categoryParam,
    changeType: changeTypeParam,
    changedDate: changedDateParam,
    page: pageParam,
  } = await searchParams;

  const requestedShopId = parseNonNegativeInt(shopIdParam, 0);
  const page = parseNonNegativeInt(pageParam, 0);

  const today = new Date();
  const todayValue = formatDate(today);
  const minDate = new Date(today);
  minDate.setMonth(minDate.getMonth() - RETENTION_MONTHS);

  const changedDate = parseChangedDate(changedDateParam, todayValue);

  // 가게 목록과 분류 카탈로그는 서로 의존이 없다.
  const [shopsResult, categoriesResult] = await Promise.all([
    shopService.getMyShops({}, { page: 0, size: 100 }),
    shopChangeHistoryService.getChangeTypes(),
  ]);

  if (shopsResult.error || !shopsResult.data) {
    logger.error({ reason: shopsResult.error }, "내 가게 목록 조회 실패");
    throw new Error(SHOP_MESSAGE.SHOP_LIST_LOAD_FAILED);
  }

  // 카탈로그가 없으면 필터를 만들 수 없어 화면이 성립하지 않는다.
  if (categoriesResult.error || !categoriesResult.data) {
    logger.error({ reason: categoriesResult.error }, "변경이력 분류 카탈로그 조회 실패");
    throw new Error(SHOP_CHANGE_HISTORY_COPY.CATALOG_LOAD_FAILED);
  }

  const shops = shopsResult.data;
  const categories = categoriesResult.data;

  const filters = {
    category: parseCategory(categoryParam, categories),
    changeType: parseChangeType(changeTypeParam, categories),
    changedDate,
  };

  if (shops.length === 0) {
    return (
      <ChangeHistoryView
        shops={[]}
        categories={categories}
        filters={filters}
        page={page}
        minDate={formatDate(minDate)}
        maxDate={todayValue}
      />
    );
  }

  // 보유하지 않은 가게를 찍고 들어와도 첫 가게로 대체한다 — 가게 관리 라우트와 같은 규칙.
  // 다만 조용히 바꾸면 사용자가 "요청한 가게가 아닌 다른 가게"를 보고 있는 것을 알 수 없으므로,
  // 실제로 대체가 일어났으면 뷰에 알려 안내 문구를 띄운다.
  // shopId 미지정(기본 진입)은 대체가 아니라 기본 선택이므로 안내 대상이 아니다.
  const matchedShop = shops.find((shop) => shop.id === requestedShopId);
  const selectedShop = matchedShop ?? shops[0];
  const isShopFallback = parseSearchString(shopIdParam) !== undefined && matchedShop === undefined;

  const listResult = await shopChangeHistoryService.getList(selectedShop.id, filters, { page, size: PAGE_SIZE });

  // 목록 조회 실패는 필터바를 죽이지 않는다 — 목록만 undefined 로 넘겨 뷰에서 에러 문구를 띄운다.
  if (listResult.error || !listResult.data) {
    logger.error(
      { reason: listResult.error, errorCode: listResult.errorCode, shopId: selectedShop.id },
      "가게 변경이력 조회 실패 — 필터바만 렌더",
    );
  }

  return (
    <ChangeHistoryView
      shops={shops}
      shopId={selectedShop.id}
      isShopFallback={isShopFallback}
      categories={categories}
      filters={filters}
      page={page}
      minDate={formatDate(minDate)}
      maxDate={todayValue}
      items={listResult.data}
      totalPages={listResult.pagination?.totalPages ?? 0}
      errorCode={listResult.errorCode}
      errorMessage={listResult.error}
    />
  );
}
