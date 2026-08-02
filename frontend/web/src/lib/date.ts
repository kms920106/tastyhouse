import dayjs from '@/lib/dayjs'

/**
 * Represents the difference between two points in time.
 */
export interface TimeDifference {
  days: number
  hours: number
  minutes: number
  seconds: number
  totalMilliseconds: number
}

/**
 * Calculates the time difference between now and a given end date.
 * @param end The future date (string or Date object).
 * @returns A TimeDifference object.
 */
export function getTimeDifference(end: string | Date): TimeDifference {
  // 음수가 되지 않도록 0으로 하한 고정 (이미 지난 시점은 0으로 취급)
  const totalMilliseconds = Math.max(0, dayjs(end).diff(dayjs()))
  const diff = dayjs.duration(totalMilliseconds)

  return {
    days: Math.floor(diff.asDays()),
    hours: diff.hours(),
    minutes: diff.minutes(),
    seconds: diff.seconds(),
    totalMilliseconds,
  }
}

/**
 * Supported date format types.
 */
export type DateFormat =
  | 'YYYY.MM.DD'
  | 'YY.MM.DD'
  | 'MM.DD'
  | 'YYYY-MM-DD'
  | 'MM-DD'
  | 'YYYY년 M월 D일'
  | 'YYYY-MM-DD HH:mm'
  | 'HH:mm'

/**
 * Formats a date according to the specified format string.
 * @param date - The date to format (string or Date object).
 * @param format - The desired format type.
 * @returns A formatted date string.
 * @throws Error if the date is invalid.
 */
export function formatDate(date: string | Date, format: DateFormat): string {
  const d = dayjs(date)

  if (!d.isValid()) {
    throw new Error('Invalid date provided to formatDate')
  }

  return d.format(format)
}

/**
 * Formats a TimeDifference object into a string like "X일 Y시간 Z분".
 * Only displays non-zero parts, up to the minute.
 * @param diff The TimeDifference object.
 * @returns A formatted string.
 */
export function formatRemainingTime(diff: TimeDifference): string {
  const parts = []
  if (diff.days > 0) {
    parts.push(`${diff.days}일`)
  }
  if (diff.hours > 0) {
    parts.push(`${diff.hours}시간`)
  }
  if (diff.minutes > 0) {
    parts.push(`${diff.minutes}분`)
  }

  if (parts.length === 0) {
    // If less than a minute, show "0분" or "방금"
    return '0분'
  }

  return parts.join(' ')
}

/**
 * A simple utility to get only the remaining days, for components that don't need detailed time.
 * @param end The future date (string or Date object).
 * @returns The number of full days remaining.
 */
export function getRemainingDays(end: string | Date): number {
  return getTimeDifference(end).days
}

/**
 * Returns the current epoch time in milliseconds.
 * Use instead of `Date.now()` so all time access flows through this module.
 */
export function getEpochMs(): number {
  return dayjs().valueOf()
}

/**
 * Returns today's date as a 'YYYY-MM-DD' string (e.g. for `<input type="date" min>`).
 */
export function getTodayString(): string {
  return dayjs().format('YYYY-MM-DD')
}

/**
 * Returns the current year and month (month is 0-indexed to match JS Date semantics).
 */
export function getCurrentYearMonth(): { year: number; month: number } {
  const now = dayjs()
  return { year: now.year(), month: now.month() }
}

/**
 * Shifts a year/month pair by a number of months, normalizing overflow.
 * @param ym The current year/month (month is 0-indexed).
 * @param delta The number of months to shift (can be negative).
 * @returns The resulting year/month (month is 0-indexed).
 */
export function shiftYearMonth(
  ym: { year: number; month: number },
  delta: number,
): { year: number; month: number } {
  const shifted = dayjs().year(ym.year).month(ym.month).add(delta, 'month')
  return { year: shifted.year(), month: shifted.month() }
}

/**
 * Returns the start (1st, 00:00:00) and end (last day, 23:59:59) of the given month.
 * @param year The full year.
 * @param month The month (0-indexed).
 * @returns A pair of Date objects for the month boundaries.
 */
export function getMonthRange(year: number, month: number): { startAt: Date; endAt: Date } {
  const base = dayjs().year(year).month(month)
  return {
    startAt: base.startOf('month').toDate(),
    endAt: base.endOf('month').toDate(),
  }
}

/**
 * Represents a single cell in a month calendar grid.
 */
export interface CalendarCell {
  date: string | null
  day: number | null
  weekday: number
  isPast: boolean
  isToday: boolean
}

/**
 * Builds a 7-column month grid (with leading/trailing padding) for a date picker.
 * @param year The full year.
 * @param month The month (0-indexed).
 * @returns An array of CalendarCell aligned to weeks (length is a multiple of 7).
 */
export function buildMonthGrid(year: number, month: number): CalendarCell[] {
  const today = dayjs().startOf('day')
  const firstDay = dayjs().year(year).month(month).startOf('month')
  const daysInMonth = firstDay.daysInMonth()

  const cells: CalendarCell[] = []

  // 앞 패딩 — 1일이 위치한 요일만큼 빈 칸 채우기
  const leadingPad = firstDay.day()
  for (let i = 0; i < leadingPad; i++) {
    cells.push({ date: null, day: null, weekday: i, isPast: false, isToday: false })
  }

  // 해당 월 날짜
  for (let d = 1; d <= daysInMonth; d++) {
    const dateObj = firstDay.date(d)
    cells.push({
      date: dateObj.format('YYYY-MM-DD'),
      day: d,
      weekday: dateObj.day(),
      isPast: dateObj.isBefore(today),
      isToday: dateObj.isSame(today, 'day'),
    })
  }

  // 뒤 패딩 (7의 배수 맞춤)
  const remainder = cells.length % 7
  if (remainder !== 0) {
    const lastWeekday = firstDay.date(daysInMonth).day()
    const padCount = 7 - remainder
    for (let i = 0; i < padCount; i++) {
      cells.push({
        date: null,
        day: null,
        weekday: (lastWeekday + 1 + i) % 7,
        isPast: false,
        isToday: false,
      })
    }
  }

  return cells
}

/**
 * Returns a timestamp (ms) for sorting comparisons.
 * @param date The date to convert (string or Date object).
 */
export function toTimestamp(date: string | Date): number {
  return dayjs(date).valueOf()
}

/**
 * Formats an epoch timestamp into a millisecond-precision log line.
 * e.g. "2026-06-07 14:30:05.123"
 * @param epochMs The epoch time in milliseconds.
 */
export function formatLogTimestamp(epochMs: number): string {
  return dayjs(epochMs).format('YYYY-MM-DD HH:mm:ss.SSS')
}

/**
 * Korean weekday labels indexed by day of week (0 = Sunday).
 */
const KOREAN_WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'] as const

/**
 * Formats a reservation date/time and party size into a single summary line.
 * e.g. "07.01(수) · 오후 12:00 · 2명"
 * @param date The reservation date (string or Date object).
 * @param partySize The number of people for the reservation.
 * @returns A formatted single-line summary string.
 */
export function formatReservationSummary(date: string | Date, partySize: number): string {
  const d = dayjs(date)
  const weekday = KOREAN_WEEKDAYS[d.day()]
  const meridiem = d.hour() < 12 ? '오전' : '오후'

  return `${d.format('MM.DD')}(${weekday}) · ${meridiem} ${d.format('hh:mm')} · ${partySize}명`
}

/**
 * Formats a date as a relative time string (e.g., "방금 전", "5분 전", "3시간 전").
 * @param date The past date (string or Date object).
 * @returns A formatted relative time string in Korean.
 */
export function formatTimeAgo(date: string | Date): string {
  const past = dayjs(date)
  const now = dayjs()
  const diffInMinutes = now.diff(past, 'minute')
  const diffInHours = now.diff(past, 'hour')
  const diffInDays = now.diff(past, 'day')

  if (diffInMinutes < 1) return '방금 전'
  if (diffInMinutes < 60) return `${diffInMinutes}분 전`
  if (diffInHours < 24) return `${diffInHours}시간 전`
  if (diffInDays < 7) return `${diffInDays}일 전`

  return past.format('M월 D일')
}
