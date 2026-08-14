import { reviewBlindRequestService } from "@/api/review-blind-request/review-blind-request.service";
import {
  REVIEW_BLIND_REASON_OPTIONS,
  REVIEW_BLIND_REQUEST_STATUS_OPTIONS,
} from "@/feature/review-blind-request/constants";
import type {
  ReviewBlindReason,
  ReviewBlindRequestDetail,
  ReviewBlindRequestStatus,
} from "@/feature/review-blind-request/domain";
import { REVIEW_BLIND_REQUEST_MESSAGE } from "@/feature/review-blind-request/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseSearchString } from "@/lib/utils";

import { ReviewBlindRequests } from "./_components/review-blind-requests";

const MAX_PAGE_SIZE = 100;

/** 심사 대기 큐가 목적인 화면이라 상태 필터를 지정하지 않으면 PENDING 을 본다. */
const DEFAULT_STATUS: ReviewBlindRequestStatus = "PENDING";

function parseStatus(value: string | string[] | undefined): ReviewBlindRequestStatus {
  const raw = parseSearchString(value);
  return REVIEW_BLIND_REQUEST_STATUS_OPTIONS.includes(raw as ReviewBlindRequestStatus)
    ? (raw as ReviewBlindRequestStatus)
    : DEFAULT_STATUS;
}

function parseReason(value: string | string[] | undefined): ReviewBlindReason | undefined {
  const raw = parseSearchString(value);
  return REVIEW_BLIND_REASON_OPTIONS.includes(raw as ReviewBlindReason) ? (raw as ReviewBlindReason) : undefined;
}

/** 쿼리스트링의 양수 ID. 정수가 아니거나 0 이하면 무시한다(잘못된 필터는 에러가 아니라 무시). */
function parsePositiveInt(value: string | string[] | undefined): number | undefined {
  const raw = parseSearchString(value);
  const parsed = Number(raw);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/review-blind-requests">) {
  const {
    page: pageParam,
    size: sizeParam,
    status: statusParam,
    shopId: shopIdParam,
    reason: reasonParam,
    startDate: startDateParam,
    endDate: endDateParam,
    requestId: requestIdParam,
  } = await searchParams;

  const status = parseStatus(statusParam);
  const shopId = parsePositiveInt(shopIdParam);
  const reason = parseReason(reasonParam);
  const startDate = parseSearchString(startDateParam);
  const endDate = parseSearchString(endDateParam);
  const requestId = parsePositiveInt(requestIdParam);
  const page = parseNonNegativeInt(pageParam, 0);
  const size = Math.min(parseNonNegativeInt(sizeParam, 10), MAX_PAGE_SIZE);

  const { error, data, pagination } = await reviewBlindRequestService.getBlindRequests(
    { status, shopId, reason, startDate, endDate },
    { page, size },
  );

  if (error || !data || !pagination) {
    logger.error({ reason: error, data, pagination }, "게시중단 요청 목록 조회 실패");
    throw new Error(REVIEW_BLIND_REQUEST_MESSAGE.LIST_LOAD_FAILED);
  }

  // repository 가 server-only 라 클라이언트에서 상세를 fetch 할 수 없다.
  // ?requestId= 를 여기서 읽어 서버에서 조회한 뒤 prop 으로 내린다.
  let detail: ReviewBlindRequestDetail | undefined;
  let detailError: string | undefined;

  if (requestId !== undefined) {
    const detailResponse = await reviewBlindRequestService.getBlindRequest(requestId);
    if (detailResponse.error || !detailResponse.data) {
      logger.error({ reason: detailResponse.error, requestId }, "게시중단 요청 상세 조회 실패");
      detailError = detailResponse.message ?? REVIEW_BLIND_REQUEST_MESSAGE.DETAIL_LOAD_FAILED;
    } else {
      detail = detailResponse.data;
    }
  }

  return (
    <ReviewBlindRequests
      blindRequests={data}
      pagination={pagination}
      initialStatus={status}
      initialShopId={shopId}
      initialReason={reason}
      initialStartDate={startDate}
      initialEndDate={endDate}
      detailRequestId={requestId}
      detail={detail}
      detailError={detailError}
    />
  );
}
