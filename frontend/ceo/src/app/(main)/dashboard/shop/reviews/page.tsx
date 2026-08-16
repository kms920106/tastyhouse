import { ceoReplyPhraseService } from "@/api/ceo-reply-phrase/ceo-reply-phrase.service";
import { shopService } from "@/api/shop/shop.service";
import { shopReviewService } from "@/api/shop-review/shop-review.service";
import {
  ORDER_METHOD_OPTIONS,
  RATING_FILTER_OPTIONS,
  REVIEW_SORT_TYPE_OPTIONS,
  SHOP_REVIEW_PAGE_SIZE,
  SHOP_REVIEW_TAB_OPTIONS,
  SHOP_REVIEW_TABS,
} from "@/feature/shop-review/constants";
import type { ShopReviewTab } from "@/feature/shop-review/domain";
import { SHOP_REVIEW_MESSAGE } from "@/feature/shop-review/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseOptionalBoolean, parseSearchString } from "@/lib/utils";

import { ShopReviewView } from "./_components/shop-review-view";

const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

/**
 * `yyyy-MM-dd` 형식이 아니거나, 형식은 맞아도 실존하지 않는 날짜(예: `2026-13-99`)면 필터를 걸지 않는다.
 *
 * 시작일 > 종료일 같은 범위 판정은 서버가 하고(400 `REVIEW_DATE_RANGE_INVALID`),
 * 여기서는 형식·유효성만 본다 — 범위를 프론트에서 잘라내면 사용자에게 "왜 비었는지"가 안 보인다.
 * 정규식은 자릿수만 검사하므로 `Date.UTC` 왕복 비교로 존재하는 날짜인지 재확인한다
 * (`new Date(2026, 12, 99)` 처럼 월/일 오버플로를 다음 달/해로 조용히 보정하는 것을 막기 위함).
 */
function parseDate(value: string | string[] | undefined): string | undefined {
  const raw = parseSearchString(value);
  if (!raw || !DATE_PATTERN.test(raw)) return undefined;

  const [year, month, day] = raw.split("-").map(Number);
  const timestamp = Date.UTC(year, month - 1, day);
  const isRealDate = new Date(timestamp).toISOString().slice(0, 10) === raw;

  return isRealDate ? raw : undefined;
}

/**
 * 카탈로그에 있는 코드만 통과시킨다.
 *
 * URL 을 직접 편집해 임의 문자열이 들어오면 백엔드가 400(`REVIEW_TAB_UNKNOWN` 등)을 던져
 * 목록이 통째로 실패하는데, 그 경우까지 에러로 보낼 이유가 없으므로 **조용히 무시**한다
 * (기존 `shop/requests` 관례).
 */
function parseEnum(value: string | string[] | undefined, catalog: readonly string[]): string | undefined {
  const raw = parseSearchString(value);
  return raw && catalog.includes(raw) ? raw : undefined;
}

/** 1~5 범위의 별점 필터. 범위를 벗어나거나 정수가 아니면 필터를 걸지 않는다 */
function parseRating(value: string | string[] | undefined): number | undefined {
  const raw = parseSearchString(value);
  if (raw === undefined) return undefined;

  const parsed = Number(raw);
  return RATING_FILTER_OPTIONS.some((rating) => rating === parsed) ? parsed : undefined;
}

const TAB_CODES = SHOP_REVIEW_TAB_OPTIONS.map((option) => option.value);
const SORT_TYPE_CODES = REVIEW_SORT_TYPE_OPTIONS.map((option) => option.value);
const ORDER_METHOD_CODES = ORDER_METHOD_OPTIONS.map((option) => option.value);

export default async function Page({ searchParams }: PageProps<"/dashboard/shop/reviews">) {
  const {
    shopId: shopIdParam,
    reviewId: reviewIdParam,
    tab: tabParam,
    startDate: startDateParam,
    endDate: endDateParam,
    rating: ratingParam,
    orderMethod: orderMethodParam,
    hasImage: hasImageParam,
    sortType: sortTypeParam,
    page: pageParam,
  } = await searchParams;

  const requestedShopId = parseNonNegativeInt(shopIdParam, 0);
  const page = parseNonNegativeInt(pageParam, 0);

  const shopsResult = await shopService.getMyShops({}, { page: 0, size: 100 });

  // 가게 목록이 없으면 어떤 가게의 리뷰인지 정할 수 없어 화면 자체가 성립하지 않는다.
  if (shopsResult.error || !shopsResult.data) {
    logger.error({ reason: shopsResult.error }, "내 가게 목록 조회 실패");
    throw new Error(SHOP_REVIEW_MESSAGE.SHOP_LIST_LOAD_FAILED);
  }

  const shops = shopsResult.data;

  const tab = (parseEnum(tabParam, TAB_CODES) ?? SHOP_REVIEW_TABS.ALL) as ShopReviewTab;
  const filters = {
    tab,
    startDate: parseDate(startDateParam),
    endDate: parseDate(endDateParam),
    rating: parseRating(ratingParam),
    orderMethod: parseEnum(orderMethodParam, ORDER_METHOD_CODES),
    hasImage: parseOptionalBoolean(hasImageParam),
    sortType: parseEnum(sortTypeParam, SORT_TYPE_CODES),
  };

  if (shops.length === 0) {
    return <ShopReviewView shops={[]} filters={filters} page={page} blindReasons={[]} phrases={[]} />;
  }

  // shopId 미지정(기본 진입)만 첫 가게로 대체한다. shopId 를 지정했는데 내 목록에 없으면
  // — 소유하지 않은 가게이거나 존재하지 않는 가게이므로 — 조용히 다른 가게로 바꿔치기하지 않고
  // 지정된 shopId 그대로 백엔드에 조회를 위임해 403/404 인가 검증이 그대로 드러나게 한다.
  const isShopIdSpecified = parseSearchString(shopIdParam) !== undefined;
  const matchedShop = shops.find((shop) => shop.id === requestedShopId);
  const selectedShopId = isShopIdSpecified ? requestedShopId : (matchedShop?.id ?? shops[0].id);

  // 목록·통계·정렬설정·사유 카탈로그는 서로 의존이 없다. `Promise.all` 이 아니라 `allSettled` 로
  // 받는 이유는 **하나가 실패해도 나머지를 보여주기 위함**이다 — 통계만 실패했다고 목록까지
  // 사라지면 점주가 할 수 있는 일이 없어진다.
  const [listResult, statisticsResult, sortTypeResult, blindReasonsResult, phrasesResult] = await Promise.allSettled([
    shopReviewService.getList(selectedShopId, filters, { page, size: SHOP_REVIEW_PAGE_SIZE }),
    shopReviewService.getStatistics(selectedShopId),
    shopReviewService.getSortType(selectedShopId),
    shopReviewService.getBlindReasons(),
    // 문구 조회 실패가 리뷰 화면 전체를 죽이지 않게 한다 — 실패하면 문구 선택 영역만 사라진다.
    ceoReplyPhraseService.getPhrases(),
  ]);

  const list = listResult.status === "fulfilled" ? listResult.value : undefined;
  const statistics = statisticsResult.status === "fulfilled" ? statisticsResult.value : undefined;
  const sortTypeSetting = sortTypeResult.status === "fulfilled" ? sortTypeResult.value : undefined;
  const blindReasons = blindReasonsResult.status === "fulfilled" ? blindReasonsResult.value : undefined;
  const phrases = phrasesResult.status === "fulfilled" ? phrasesResult.value : undefined;

  // 목록 조회 실패는 필터바를 죽이지 않는다 — 목록만 undefined 로 넘겨 뷰에서 에러 문구를 띄운다.
  // (소유하지 않거나 존재하지 않는 shopId 는 여기서 403/404 로 드러난다.)
  if (!list?.data) {
    logger.error(
      { reason: list?.error, errorCode: list?.errorCode, shopId: selectedShopId },
      "가게 리뷰 목록 조회 실패 — 필터바만 렌더",
    );
  }

  if (!statistics?.data) {
    logger.error({ reason: statistics?.error, shopId: selectedShopId }, "리뷰 통계 조회 실패 — 목록은 그대로 렌더");
  }

  // 상세는 클라이언트에서 가져올 수 없다(repository·service 가 server-only) — `?reviewId=` 를
  // 서버가 읽어 상세를 조회한 뒤 뷰에 내려주고, 뷰는 그 값이 있을 때만 Sheet 를 연다.
  const reviewId = parseSearchString(reviewIdParam) ? parseNonNegativeInt(reviewIdParam, 0) : 0;
  const detailResult = reviewId > 0 ? await shopReviewService.getDetail(selectedShopId, reviewId) : undefined;

  if (detailResult && !detailResult.data) {
    logger.error(
      { reason: detailResult.error, errorCode: detailResult.errorCode, reviewId },
      "리뷰 상세 조회 실패 — 시트를 열지 않는다",
    );
  }

  return (
    <ShopReviewView
      shops={shops}
      shopId={selectedShopId}
      filters={filters}
      page={page}
      items={list?.data}
      totalPages={list?.pagination?.totalPages ?? 0}
      errorCode={list?.errorCode}
      errorMessage={list?.error}
      statistics={statistics?.data}
      statisticsFailed={!statistics?.data}
      sortTypeSetting={sortTypeSetting?.data}
      blindReasons={blindReasons?.data ?? []}
      phrases={phrases?.data ?? []}
      detail={detailResult?.data}
      detailErrorMessage={detailResult?.error}
    />
  );
}
