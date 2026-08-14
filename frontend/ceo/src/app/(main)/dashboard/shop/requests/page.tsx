import { shopService } from "@/api/shop/shop.service";
import { shopRequestService } from "@/api/shop-request/shop-request.service";
import type { ShopRequestStatusOption, ShopRequestTypeOption } from "@/feature/shop/domain";
import { SHOP_MESSAGE, SHOP_REQUEST_COPY } from "@/feature/shop/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseSearchString } from "@/lib/utils";

import { ShopRequestView } from "./_components/shop-request-view";

const PAGE_SIZE = 10;
const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

/**
 * `yyyy-MM-dd` 형식이 아니거나, 형식은 맞아도 실존하지 않는 날짜(예: `2026-13-99`)면 필터를 걸지 않는다.
 *
 * 시작일 > 종료일 같은 범위 판정은 서버가 하고(400 `SHOP_REQUEST_DATE_RANGE_INVALID`),
 * 여기서는 형식·유효성만 본다 — 범위를 프론트에서 잘라내면 사용자에게 "왜 비었는지"가 안 보인다.
 * 정규식은 자릿수만 검사하므로, `Date.UTC` 왕복 비교로 존재하는 날짜인지 재확인한다
 * (`new Date(2026, 12, 99)`처럼 월/일 오버플로를 다음 달/해로 조용히 보정하는 것을 막기 위함).
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
 * 서버 카탈로그에 존재하는 코드만 통과시킨다. URL 을 직접 편집해 임의 문자열이 들어오면
 * 백엔드가 400(`SHOP_REQUEST_TYPE_UNKNOWN`)을 던져 목록이 통째로 실패하는데,
 * 그 경우까지 에러로 보낼 이유가 없으므로 필터를 무시(전체로 취급)한다.
 */
function parseRequestType(value: string | string[] | undefined, requestTypes: ShopRequestTypeOption[]) {
  const raw = parseSearchString(value);
  return raw && requestTypes.some((requestType) => requestType.code === raw) ? raw : undefined;
}

function parseStatus(value: string | string[] | undefined, statuses: ShopRequestStatusOption[]) {
  const raw = parseSearchString(value);
  return raw && statuses.some((status) => status.code === raw) ? raw : undefined;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/shop/requests">) {
  const {
    shopId: shopIdParam,
    requestId: requestIdParam,
    requestType: requestTypeParam,
    status: statusParam,
    startDate: startDateParam,
    endDate: endDateParam,
    page: pageParam,
  } = await searchParams;

  const requestedShopId = parseNonNegativeInt(shopIdParam, 0);
  const page = parseNonNegativeInt(pageParam, 0);

  // 가게 목록과 요청 유형 카탈로그는 서로 의존이 없다.
  const [shopsResult, catalogResult] = await Promise.all([
    shopService.getMyShops({}, { page: 0, size: 100 }),
    shopRequestService.getRequestTypes(),
  ]);

  if (shopsResult.error || !shopsResult.data) {
    logger.error({ reason: shopsResult.error }, "내 가게 목록 조회 실패");
    throw new Error(SHOP_MESSAGE.SHOP_LIST_LOAD_FAILED);
  }

  // 카탈로그가 없으면 필터를 만들 수 없어 화면이 성립하지 않는다.
  if (catalogResult.error || !catalogResult.data) {
    logger.error({ reason: catalogResult.error }, "요청 유형 카탈로그 조회 실패");
    throw new Error(SHOP_REQUEST_COPY.CATALOG_LOAD_FAILED);
  }

  const shops = shopsResult.data;
  const { requestTypes, statuses } = catalogResult.data;

  const filters = {
    requestType: parseRequestType(requestTypeParam, requestTypes),
    status: parseStatus(statusParam, statuses),
    startDate: parseDate(startDateParam),
    endDate: parseDate(endDateParam),
  };

  if (shops.length === 0) {
    return <ShopRequestView shops={[]} requestTypes={requestTypes} statuses={statuses} filters={filters} page={page} />;
  }

  // shopId 미지정(기본 진입)만 첫 가게로 대체한다. shopId 를 지정했는데 내 목록에 없으면
  // — 소유하지 않은 가게이거나 존재하지 않는 가게이므로 — 조용히 다른 가게로 바꿔치기하지 않고
  // 지정된 shopId 그대로 백엔드에 조회를 위임해 403/404 인가 검증이 그대로 드러나게 한다.
  const isShopIdSpecified = parseSearchString(shopIdParam) !== undefined;
  const matchedShop = shops.find((shop) => shop.id === requestedShopId);
  const selectedShopId = isShopIdSpecified ? requestedShopId : (matchedShop?.id ?? shops[0].id);

  const listResult = await shopRequestService.getList(selectedShopId, filters, { page, size: PAGE_SIZE });

  // 목록 조회 실패는 필터바를 죽이지 않는다 — 목록만 undefined 로 넘겨 뷰에서 에러 문구를 띄운다.
  // (소유하지 않거나 존재하지 않는 shopId 는 여기서 403/404 로 드러난다.)
  if (listResult.error || !listResult.data) {
    logger.error(
      { reason: listResult.error, errorCode: listResult.errorCode, shopId: selectedShopId },
      "가게 요청 목록 조회 실패 — 필터바만 렌더",
    );
  }

  // 상세는 클라이언트에서 가져올 수 없다(repository·service 가 server-only) — `?requestId=` 를
  // 서버가 읽어 상세와 문의 스레드를 함께 조회한 뒤 Sheet 로 렌더한다.
  const requestId = parseSearchString(requestIdParam) ? parseNonNegativeInt(requestIdParam, 0) : 0;
  const [detailResult, commentsResult] =
    requestId > 0
      ? await Promise.all([
          shopRequestService.getDetail(selectedShopId, requestId),
          shopRequestService.getComments(selectedShopId, requestId),
        ])
      : [undefined, undefined];

  if (detailResult && (detailResult.error || !detailResult.data)) {
    logger.error(
      { reason: detailResult.error, errorCode: detailResult.errorCode, requestId },
      "가게 요청 상세 조회 실패 — 시트를 열지 않는다",
    );
  }

  return (
    <ShopRequestView
      shops={shops}
      shopId={selectedShopId}
      requestTypes={requestTypes}
      statuses={statuses}
      filters={filters}
      page={page}
      items={listResult.data}
      totalPages={listResult.pagination?.totalPages ?? 0}
      errorCode={listResult.errorCode}
      errorMessage={listResult.error}
      detail={detailResult?.data}
      comments={commentsResult?.data}
      detailErrorCode={detailResult?.errorCode}
      detailErrorMessage={detailResult?.error}
    />
  );
}
