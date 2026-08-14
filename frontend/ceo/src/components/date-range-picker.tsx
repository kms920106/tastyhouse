"use client";

import * as React from "react";

import { format, subDays } from "date-fns";
import type { DateRange } from "react-day-picker";

import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";

/**
 * `yyyy-MM-dd` 문자열을 로컬 자정 Date 로 되돌린다.
 *
 * `new Date("2026-08-12")` 는 UTC 자정으로 파싱돼 KST 에서 하루 앞의 날짜가 되므로
 * 쓰지 않고 연·월·일을 직접 넘긴다(`formatDate` 가 `toISOString` 을 피하는 것과 같은 이유).
 */
function parseLocalDate(value: string | undefined): Date | undefined {
  if (!value) return undefined;
  const [year, month, day] = value.split("-").map(Number);
  return new Date(year, month - 1, day);
}

interface DateRangePickerProps {
  value?: DateRange;
  onChange?: (value: DateRange | undefined) => void;
  /** 조회 전환 중처럼 입력을 잠가야 할 때 트리거를 비활성화한다 */
  disabled?: boolean;
  /** 트리거 버튼에 표시할 문구. 선택된 기간이 없을 때만 쓰인다 */
  placeholder?: string;
  /**
   * 선택 가능한 최소·최대 날짜(`yyyy-MM-dd`). 보관 기간이 있는 이력 조회 화면에서 쓴다.
   *
   * 캘린더에서 범위를 벗어난 날짜를 비활성화하는 **브라우저 1차 방어**이며, 진짜 방어선은
   * 서버 검증이다 — URL 을 직접 조작하면 서버가 400 을 내린다. 둘 다 생략하면 제한이 없다.
   */
  minDate?: string;
  maxDate?: string;
}

export function DateRangePicker({ value, onChange, disabled, placeholder, minDate, maxDate }: DateRangePickerProps) {
  const [open, setOpen] = React.useState(false);
  const [internalDateRange, setInternalDateRange] = React.useState<DateRange | undefined>(() => {
    const to = new Date();
    const from = subDays(to, 29);
    return { from, to };
  });
  const dateRange = value ?? internalDateRange;

  const minSelectable = parseLocalDate(minDate);
  const maxSelectable = parseLocalDate(maxDate);
  // DayPicker 의 disabled 는 matcher 이므로 범위 밖 날짜를 before/after 로 잠근다.
  // startMonth/endMonth 를 함께 주어 잠긴 달로 넘겨보는 것 자체를 막는다.
  const disabledDays =
    minSelectable || maxSelectable
      ? [...(minSelectable ? [{ before: minSelectable }] : []), ...(maxSelectable ? [{ after: maxSelectable }] : [])]
      : undefined;

  const handleDateChange = (nextValue: DateRange | undefined) => {
    if (!value) {
      setInternalDateRange(nextValue);
    }
    onChange?.(nextValue);
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button variant="outline" id="date" className="font-normal" disabled={disabled}>
          {dateRange?.from
            ? dateRange.to
              ? `${format(dateRange.from, "d MMM yyyy")} - ${format(dateRange.to, "d MMM yyyy")}`
              : format(dateRange.from, "d MMM yyyy")
            : (placeholder ?? "Select date")}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-auto overflow-hidden p-0" align="end">
        <Calendar
          mode="range"
          defaultMonth={dateRange?.from}
          selected={dateRange}
          onSelect={handleDateChange}
          numberOfMonths={2}
          disabled={disabledDays}
          startMonth={minSelectable}
          endMonth={maxSelectable}
        />
      </PopoverContent>
    </Popover>
  );
}
