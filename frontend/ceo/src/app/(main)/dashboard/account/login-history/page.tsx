import { ceoLoginHistoryService } from "@/api/ceo-login-history/ceo-login-history.service";
import { CEO_LOGIN_HISTORY_RETENTION_DAYS, CEO_LOGIN_RESULT_OPTIONS } from "@/feature/ceo/constants";
import { CEO_LOGIN_HISTORY_COPY } from "@/feature/ceo/message";
import { formatDate } from "@/lib/date";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseSearchString } from "@/lib/utils";

import { LoginHistoryView } from "./_components/login-history-view";

const PAGE_SIZE = 10;
const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

/**
 * `yyyy-MM-dd` 형식이 아니거나, 형식은 맞아도 실존하지 않는 날짜(예: `2026-13-99`)면 필터를 걸지 않는다.
 *
 * 보관 기간(90일) 초과·시작일 > 종료일 판정은 서버가 하고(400 `CEO_LOGIN_HISTORY_DATE_OUT_OF_RANGE` /
 * `SHOP_REQUEST_DATE_RANGE_INVALID`), 여기서는 형식·유효성만 본다 — 범위를 프론트에서 잘라내면
 * 사용자에게 "왜 비었는지"가 안 보인다. 정규식은 자릿수만 검사하므로 `Date.UTC` 왕복 비교로
 * 존재하는 날짜인지 재확인한다(`new Date(2026, 12, 99)` 처럼 월/일 오버플로가 조용히 보정되는 것을 막는다).
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
 * 프론트 카탈로그(`CEO_LOGIN_RESULT_OPTIONS`)에 있는 코드만 통과시킨다.
 *
 * URL 을 직접 편집해 임의 문자열이 들어오면 백엔드가 400(`CEO_LOGIN_RESULT_UNKNOWN`)을 던져
 * 목록이 통째로 실패하는데, 그 경우까지 에러로 보낼 이유가 없으므로 필터를 무시(전체로 취급)한다.
 */
function parseResult(value: string | string[] | undefined): string | undefined {
  const raw = parseSearchString(value);
  return raw && CEO_LOGIN_RESULT_OPTIONS.some((option) => option.code === raw) ? raw : undefined;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/account/login-history">) {
  const { result: resultParam, startDate: startDateParam, endDate: endDateParam, page: pageParam } = await searchParams;

  const page = parseNonNegativeInt(pageParam, 0);

  const filters = {
    result: parseResult(resultParam),
    startDate: parseDate(startDateParam),
    endDate: parseDate(endDateParam),
  };

  // 피커의 선택 가능 범위 — 보관 기간의 브라우저 1차 방어이고, 진짜 방어선은 서버다.
  const today = new Date();
  const minDate = new Date(today);
  minDate.setDate(minDate.getDate() - CEO_LOGIN_HISTORY_RETENTION_DAYS);

  const listResult = await ceoLoginHistoryService.getLoginHistories(filters, { page, size: PAGE_SIZE });

  // 목록 조회 실패는 필터바를 죽이지 않는다 — 목록만 undefined 로 넘겨 뷰에서 에러 문구를 띄운다.
  if (listResult.error || !listResult.data) {
    logger.error(
      { reason: listResult.error, errorCode: listResult.errorCode },
      `${CEO_LOGIN_HISTORY_COPY.TITLE} 조회 실패 — 필터바만 렌더`,
    );
  }

  return (
    <LoginHistoryView
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
