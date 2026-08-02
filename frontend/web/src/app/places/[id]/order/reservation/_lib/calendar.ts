import { buildMonthGrid, shiftYearMonth, type CalendarCell } from '@/lib/date'

export type { CalendarCell }
export { buildMonthGrid }

export function formatYearMonth(year: number, month: number): string {
  const m = String(month + 1).padStart(2, '0')
  return `${year}. ${m}`
}

export function shiftMonth(
  ym: { year: number; month: number },
  delta: number,
): { year: number; month: number } {
  return shiftYearMonth(ym, delta)
}
