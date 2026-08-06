import { TIME_STEP_MINUTES } from "./constants";

const MINUTES_PER_DAY = 24 * 60;

/** "HH:mm" 또는 "HH:mm:ss" 를 자정 기준 분으로 변환한다. 형식이 아니면 null. */
export function parseTimeToMinutes(value: string): number | null {
  const match = /^(\d{2}):(\d{2})(?::(\d{2}))?$/.exec(value);
  if (!match) return null;

  const hours = Number(match[1]);
  const minutes = Number(match[2]);
  if (hours > 23 || minutes > 59) return null;

  return hours * 60 + minutes;
}

/** 자정 기준 분을 백엔드가 받는 "HH:mm:ss" 로 변환한다. */
export function formatMinutesToTime(minutes: number): string {
  const normalized = ((minutes % MINUTES_PER_DAY) + MINUTES_PER_DAY) % MINUTES_PER_DAY;
  const hh = String(Math.floor(normalized / 60)).padStart(2, "0");
  const mm = String(normalized % 60).padStart(2, "0");
  return `${hh}:${mm}:00`;
}

/** "HH:mm:ss" 를 화면 표시용 "HH:mm" 으로 줄인다. */
export function formatTimeLabel(value: string): string {
  return value.slice(0, 5);
}

export function isTimeStepValid(value: string): boolean {
  const minutes = parseTimeToMinutes(value);
  return minutes !== null && minutes % TIME_STEP_MINUTES === 0;
}

/**
 * 시작~종료 구간의 길이를 분으로 반환한다.
 * 시작과 종료가 같으면 길이 0(0분)으로 본다 — 자정을 넘긴 24시간 구간과 구분하기 위함.
 * 종료가 시작보다 작으면 자정을 넘긴 것으로 보고 +24h 로 정규화한다.
 */
export function getDurationMinutes(startTime: string, endTime: string): number | null {
  const start = parseTimeToMinutes(startTime);
  const end = parseTimeToMinutes(endTime);
  if (start === null || end === null) return null;
  if (end === start) return 0;

  return end > start ? end - start : end + MINUTES_PER_DAY - start;
}

/**
 * 대상 구간이 기준 구간 안에 완전히 포함되는지 판정한다.
 * 자정 넘김 구간은 시작 기준으로 펼쳐서 비교한다.
 * 길이 0(시작=종료)인 구간은 유효한 선택으로 보지 않아 항상 false.
 * 형식이 잘못된 입력은 null 을 반환해 "범위 밖" 과 구분한다.
 */
export function isRangeWithin(
  outerStart: string,
  outerEnd: string,
  innerStart: string,
  innerEnd: string,
): boolean | null {
  const outerFrom = parseTimeToMinutes(outerStart);
  const innerFrom = parseTimeToMinutes(innerStart);
  const outerLength = getDurationMinutes(outerStart, outerEnd);
  const innerLength = getDurationMinutes(innerStart, innerEnd);
  if (outerFrom === null || innerFrom === null || outerLength === null || innerLength === null) return null;
  if (innerLength === 0) return false;

  const innerOffset = innerFrom >= outerFrom ? innerFrom - outerFrom : innerFrom + MINUTES_PER_DAY - outerFrom;
  return innerOffset >= 0 && innerOffset + innerLength <= outerLength;
}

export function isSameRange(startA: string, endA: string, startB: string, endB: string): boolean {
  return (
    parseTimeToMinutes(startA) === parseTimeToMinutes(startB) && parseTimeToMinutes(endA) === parseTimeToMinutes(endB)
  );
}

/**
 * 두 시간대가 겹치는지 판정한다.
 * 자정 넘김 구간(예: 23:00~02:00)은 시작 시각 기준으로 펼쳐 하루를 넘어가는 절대 구간으로 비교하며,
 * 다음 날로 넘어간 부분이 상대 구간의 앞부분과 겹치는 경우까지 잡아내기 위해 하루를 더한 사본과도 비교한다.
 * 경계가 맞닿는 경우(A 종료 == B 시작)는 겹침이 아니다.
 * 형식이 잘못된 입력이나 길이 0 구간은 판정 대상이 아니라 false 를 반환한다.
 */
export function isRangeOverlapping(startA: string, endA: string, startB: string, endB: string): boolean {
  const fromA = parseTimeToMinutes(startA);
  const fromB = parseTimeToMinutes(startB);
  const lengthA = getDurationMinutes(startA, endA);
  const lengthB = getDurationMinutes(startB, endB);
  if (fromA === null || fromB === null || lengthA === null || lengthB === null) return false;
  if (lengthA === 0 || lengthB === 0) return false;

  // 자정을 넘겨 펼친 구간은 최대 2일 범위에 걸치므로, B 를 하루 뒤로 민 사본까지 비교해야 누락이 없다.
  return [fromB, fromB + MINUTES_PER_DAY, fromB - MINUTES_PER_DAY].some(
    (shiftedFromB) => fromA < shiftedFromB + lengthB && shiftedFromB < fromA + lengthA,
  );
}

/**
 * 영업시간이 축소되면 그 범위를 벗어난 휴게시간을 새 범위로 당겨 맞춘다.
 * 이미 범위 안이면 원본을 그대로 돌려준다.
 * 휴게시간은 영업시간과 완전히 동일할 수 없으므로, 클램프 결과가 영업시간 전체를 채우는 경우
 * 최소 TIME_STEP_MINUTES 만큼 짧게 만들어 "영업시간과 동일" 오류로 재차 막히지 않게 한다.
 */
export function clampRangeToBusinessHours(
  openTime: string,
  closeTime: string,
  startTime: string,
  endTime: string,
): { startTime: string; endTime: string } {
  if (isRangeWithin(openTime, closeTime, startTime, endTime)) {
    return { startTime, endTime };
  }

  const openFrom = parseTimeToMinutes(openTime);
  const businessLength = getDurationMinutes(openTime, closeTime);
  const innerLength = getDurationMinutes(startTime, endTime);
  const innerFrom = parseTimeToMinutes(startTime);
  if (openFrom === null || businessLength === null || innerLength === null || innerFrom === null) {
    return { startTime: openTime, endTime: closeTime };
  }

  const maxLength = Math.max(businessLength - TIME_STEP_MINUTES, TIME_STEP_MINUTES);
  const length = Math.min(innerLength, maxLength);
  const rawOffset = innerFrom >= openFrom ? innerFrom - openFrom : innerFrom + MINUTES_PER_DAY - openFrom;
  const offset = Math.min(Math.max(rawOffset, 0), businessLength - length);

  return {
    startTime: formatMinutesToTime(openFrom + offset),
    endTime: formatMinutesToTime(openFrom + offset + length),
  };
}

/** 5분 단위 시(hour) 옵션 — Select 용 00~23 */
export const HOUR_OPTIONS = Array.from({ length: 24 }, (_, index) => String(index).padStart(2, "0"));

/** 5분 단위 분(minute) 옵션 — Select 용 00,05,...,55 */
export const MINUTE_OPTIONS = Array.from({ length: 60 / TIME_STEP_MINUTES }, (_, index) =>
  String(index * TIME_STEP_MINUTES).padStart(2, "0"),
);

/** "YYYY-MM-DD" 가 실제 존재하는 달력 날짜인지 검증한다 (2월 30일 등의 롤오버 방지). */
export function isValidCalendarDate(value: string): boolean {
  const date = new Date(`${value}T00:00:00`);
  if (Number.isNaN(date.getTime())) return false;

  const [year, month, day] = value.split("-").map(Number);
  return date.getFullYear() === year && date.getMonth() + 1 === month && date.getDate() === day;
}

/** 두 날짜(YYYY-MM-DD) 사이의 일수를 시작일 포함으로 센다. 존재하지 않는 날짜는 null. */
export function countInclusiveDays(startDate: string, endDate: string): number | null {
  if (!isValidCalendarDate(startDate) || !isValidCalendarDate(endDate)) return null;

  const start = new Date(`${startDate}T00:00:00`);
  const end = new Date(`${endDate}T00:00:00`);

  return Math.floor((end.getTime() - start.getTime()) / (24 * 60 * 60 * 1000)) + 1;
}

/**
 * `<input type="datetime-local">` 이 주는 "YYYY-MM-DDTHH:mm"(초 없음) 값을
 * 스펙이 요구하는 `LocalDateTime` 형식("YYYY-MM-DDTHH:mm:ss", 오프셋 없음)으로 맞춘다.
 * 오프셋을 붙이면 서버(Jackson)의 LocalDateTime 역직렬화가 실패해 400(요청 본문을 읽을 수 없습니다)이 발생한다 — 붙이지 말 것.
 */
export function toLocalDateTimeString(datetimeLocal: string): string {
  return `${datetimeLocal}:00`;
}
