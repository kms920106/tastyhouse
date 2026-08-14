import { ceoShopAccessHistoryService } from "@/api/ceo-shop-access-history/ceo-shop-access-history.service";
import { shopService } from "@/api/shop/shop.service";
import { CEO_SHOP_ACCESS_ACTION_TYPE_OPTIONS, CEO_SHOP_ACCESS_HISTORY_RETENTION_YEARS } from "@/feature/ceo/constants";
import { CEO_SHOP_ACCESS_HISTORY_COPY } from "@/feature/ceo/message";
import { formatDate } from "@/lib/date";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseSearchString } from "@/lib/utils";

import { ShopAccessHistoryView } from "./_components/shop-access-history-view";

const PAGE_SIZE = 10;
const SHOP_LIST_SIZE = 100;
const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

/**
 * `yyyy-MM-dd` 형식이 아니거나, 형식은 맞아도 실존하지 않는 날짜(예: `2026-13-99`)면 필터를 걸지 않는다.
 *
 * 보관 기간(5년) 초과·시작일 > 종료일 판정은 서버가 하고(400
 * `CEO_SHOP_ACCESS_HISTORY_DATE_OUT_OF_RANGE` / `SHOP_REQUEST_DATE_RANGE_INVALID`),
 * 여기서는 형식·유효성만 본다 — 범위를 프론트에서 잘라내면 사용자에게 "왜 비었는지"가 안 보인다.
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
 * 프론트 카탈로그에 있는 코드만 통과시킨다. URL 을 직접 편집해 임의 문자열이 들어오면
 * 백엔드가 400(`SHOP_CEO_ASSIGNMENT_ACTION_UNKNOWN`)을 던져 목록이 통째로 실패하는데,
 * 그 경우까지 에러로 보낼 이유가 없으므로 필터를 무시(전체로 취급)한다.
 */
function parseActionType(value: string | string[] | undefined): string | undefined {
  const raw = parseSearchString(value);
  return raw && CEO_SHOP_ACCESS_ACTION_TYPE_OPTIONS.some((option) => option.code === raw) ? raw : undefined;
}

/**
 * `shopId` 는 좁히기용 선택 필터다. 숫자가 아니면 파라미터를 버리고 전체 조회한다.
 *
 * 값이 있어도 소유권을 검증하지 않는다 — 서버가 토큰의 `ceoId` 로만 필터하므로 남의 가게 id 는
 * 빈 목록이 되고 가게 존재 여부가 새지 않는다(`docs/tasks/backend.md` §2-2 인가).
 */
function parseShopId(value: string | string[] | undefined): number | undefined {
  if (parseSearchString(value) === undefined) return undefined;
  const parsed = parseNonNegativeInt(value, 0);
  return parsed > 0 ? parsed : undefined;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/account/shop-access-history">) {
  const {
    actionType: actionTypeParam,
    shopId: shopIdParam,
    startDate: startDateParam,
    endDate: endDateParam,
    page: pageParam,
  } = await searchParams;

  const page = parseNonNegativeInt(pageParam, 0);

  const filters = {
    actionType: parseActionType(actionTypeParam),
    shopId: parseShopId(shopIdParam),
    startDate: parseDate(startDateParam),
    endDate: parseDate(endDateParam),
  };

  // 피커의 선택 가능 범위 — 보관 기간의 브라우저 1차 방어이고, 진짜 방어선은 서버다.
  const today = new Date();
  const minDate = new Date(today);
  minDate.setFullYear(minDate.getFullYear() - CEO_SHOP_ACCESS_HISTORY_RETENTION_YEARS);

  // 가게 목록은 보조 필터의 선택지일 뿐이므로 이력 조회와 서로 의존이 없다.
  const [listResult, shopsResult] = await Promise.all([
    ceoShopAccessHistoryService.getShopAccessHistories(filters, { page, size: PAGE_SIZE }),
    shopService.getMyShops({}, { page: 0, size: SHOP_LIST_SIZE }),
  ]);

  // 목록 조회 실패는 필터바를 죽이지 않는다 — 목록만 undefined 로 넘겨 뷰에서 에러 문구를 띄운다.
  if (listResult.error || !listResult.data) {
    logger.error(
      { reason: listResult.error, errorCode: listResult.errorCode },
      `${CEO_SHOP_ACCESS_HISTORY_COPY.TITLE} 조회 실패 — 필터바만 렌더`,
    );
  }

  // 가게 목록 실패는 가게 필터만 비활성화한다 — 보조 필터 하나 때문에 화면 전체를 죽이지 않는다.
  if (shopsResult.error || !shopsResult.data) {
    logger.warn({ reason: shopsResult.error }, "내 가게 목록 조회 실패 — 가게 필터만 비활성화");
  }

  return (
    <ShopAccessHistoryView
      shops={shopsResult.data}
      filters={filters}
      page={page}
      minDate={formatDate(minDate)}
      maxDate={formatDate(today)}
      items={listResult.data}
      totalPages={listResult.pagination?.totalPages ?? 0}
      errorCode={listResult.errorCode}
      errorMessage={listResult.error}
    />
  );
}
