'use client'

import Icon from '@/components/ui/Icon'
import { Skeleton } from '@/components/ui/shadcn/skeleton'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import type { ReservationSlot } from '@/domains/reservation/reservation.model'
import { cn } from '@/lib/utils'
import { buildMonthGrid, formatYearMonth } from '../_lib/calendar'

interface Props {
  viewMonth: { year: number; month: number }
  selectedDate: string | null
  selectedTime: string | null
  slots: ReservationSlot[]
  isLoadingSlots: boolean
  isErrorSlots: boolean
  hasSelectedDate: boolean
  // 선택한 날짜·매장에 이미 차단 예약이 있는지 (있으면 슬롯 대신 안내를 노출)
  hasMyReservation: boolean
  onChangeMonth: (delta: number) => void
  onSelectDate: (date: string) => void
  onSelectTime: (time: string) => void
  // 예약 내역 페이지로 이동 (기존 예약 확인·변경 동선)
  onViewReservations: () => void
}

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']

interface TimeSlotButtonProps {
  time: string
  selected: boolean
  onSelect: (time: string) => void
  disabled?: boolean
}

function formatSlotLabel(time: string) {
  return time.slice(0, 5)
}

function TimeSlotButton({ time, selected, onSelect, disabled }: TimeSlotButtonProps) {
  return (
    <button
      onClick={() => !disabled && onSelect(time)}
      disabled={disabled}
      className={cn(
        'py-3 text-[13px] border rounded-[1px]',
        disabled
          ? 'border-line bg-[#eeeeee] text-[#cccccc] cursor-not-allowed'
          : selected
            ? 'border-main bg-[#f8f5f4] text-main'
            : 'border-line text-[#cccccc]',
      )}
    >
      {formatSlotLabel(time)}
    </button>
  )
}

export default function ReservationDateTime({
  viewMonth,
  selectedDate,
  selectedTime,
  slots,
  isLoadingSlots,
  isErrorSlots,
  hasSelectedDate,
  hasMyReservation,
  onChangeMonth,
  onSelectDate,
  onSelectTime,
  onViewReservations,
}: Props) {
  const cells = buildMonthGrid(viewMonth.year, viewMonth.month)

  // 오전/오후 분리 — 24시간제 기준 (API는 HH:mm 형식으로 내려줌)
  const amSlots = slots.filter((s) => Number(s.time.split(':')[0]) < 12)
  const pmSlots = slots.filter((s) => Number(s.time.split(':')[0]) >= 12)

  const renderSlots = () => {
    // 1. 날짜 미선택 (enabled:false라 isLoading/isError 모두 false인 구멍을 hasSelectedDate로 차단)
    if (!hasSelectedDate) {
      return <p className="text-sm text-[#999999] py-4 text-center">날짜를 먼저 선택해 주세요.</p>
    }

    // 2. 로딩
    if (isLoadingSlots) {
      return (
        <div className="space-y-4">
          <Skeleton className="h-4 w-12" />
          <div className="grid grid-cols-4 gap-3">
            {Array.from({ length: 4 }).map((_, i) => (
              <Skeleton key={i} className="h-[46px]" />
            ))}
          </div>
          <Skeleton className="h-4 w-12" />
          <div className="grid grid-cols-4 gap-3">
            {Array.from({ length: 8 }).map((_, i) => (
              <Skeleton key={i} className="h-[46px]" />
            ))}
          </div>
        </div>
      )
    }

    // 3. 에러
    if (isErrorSlots) {
      return (
        <p className="text-sm text-[#999999] py-4 text-center whitespace-pre-line">
          {COMMON_ERROR_MESSAGES.API_FETCH_ERROR}
        </p>
      )
    }

    // 4. 이미 그 날짜·매장에 예약이 있음 — 슬롯 대신 안내 + 예약 내역 동선
    //    (제약이 "그 날 그 가게 1건"이므로 날짜 통째로 막는다. 변경은 예약 내역에서 취소 후 재예약)
    if (hasMyReservation) {
      return (
        <div className="flex flex-col items-center gap-4 py-6 text-center">
          <p className="text-sm text-[#999999] whitespace-pre-line">
            {
              '이미 이 날짜에 예약이 있어요.\n다른 시간으로 변경하려면 예약 내역에서 기존 예약을 취소해 주세요.'
            }
          </p>
          <button
            type="button"
            onClick={onViewReservations}
            className="px-4 py-2.5 text-[13px] text-main border border-main rounded-[1px]"
          >
            예약 내역 보기
          </button>
        </div>
      )
    }

    // 5. 정상 렌더
    return (
      <>
        {amSlots.length > 0 && (
          <>
            <h2 className="mb-2.5 text-sm leading-[14px]">오전</h2>
            <div className="grid grid-cols-4 gap-3 mb-8">
              {amSlots.map((slot) => (
                <TimeSlotButton
                  key={slot.time}
                  time={slot.time}
                  selected={selectedTime === slot.time}
                  onSelect={onSelectTime}
                  disabled={!slot.available}
                />
              ))}
            </div>
          </>
        )}

        {pmSlots.length > 0 && (
          <>
            <h2 className="mb-2.5 text-sm leading-[14px]">오후</h2>
            <div className="grid grid-cols-4 gap-3">
              {pmSlots.map((slot) => (
                <TimeSlotButton
                  key={slot.time}
                  time={slot.time}
                  selected={selectedTime === slot.time}
                  onSelect={onSelectTime}
                  disabled={!slot.available}
                />
              ))}
            </div>
          </>
        )}
      </>
    )
  }

  return (
    <div className="px-[15px] py-[30px]">
      {/* 달력 */}
      <div>
        <div className="bg-[#fcfcfc] border border-line rounded-[5px] shadow-[0.5px_0_4.5px_rgba(153,153,153,0.1)] p-5">
          <div className="flex items-center justify-center gap-4 mb-6">
            <button
              onClick={() => onChangeMonth(-1)}
              aria-label="이전 달"
              className="flex items-center justify-center w-[18px] h-[18px] rounded-[1.5px] bg-white border-[0.5px] border-[#eeeeee] shadow-[0.5px_0_4.5px_rgba(153,153,153,0.1)]"
            >
              <Icon name="calendar-prev" />
            </button>
            <span className="text-lg leading-[18px]">
              {formatYearMonth(viewMonth.year, viewMonth.month)}
            </span>
            <button
              onClick={() => onChangeMonth(1)}
              aria-label="다음 달"
              className="flex items-center justify-center w-[18px] h-[18px] rounded-[1.5px] bg-white border-[0.5px] border-[#eeeeee] shadow-[0.5px_0_4.5px_rgba(153,153,153,0.1)]"
            >
              <Icon name="calendar-next" />
            </button>
          </div>

          <div className="grid grid-cols-7 mb-3">
            {WEEKDAYS.map((day) => (
              <div key={day} className="text-sm leading-[14px] text-center text-black py-2">
                {day}
              </div>
            ))}
          </div>

          <div className="grid grid-cols-7 gap-y-2">
            {cells.map((cell, index) => {
              if (!cell.date || cell.day === null) {
                return <div key={index} />
              }

              const isSelected = cell.date === selectedDate
              const isDisabled = cell.isPast

              return (
                <button
                  key={cell.date}
                  onClick={() => !isDisabled && onSelectDate(cell.date!)}
                  disabled={isDisabled}
                  aria-label={`${viewMonth.year}년 ${viewMonth.month + 1}월 ${cell.day}일`}
                  aria-pressed={isSelected}
                  aria-current={isSelected ? 'date' : undefined}
                  className="flex items-center justify-center h-[35px] w-full text-sm leading-[14px] disabled:cursor-not-allowed"
                >
                  <span
                    className={cn(
                      'flex items-center justify-center w-[35px] h-[35px] rounded-[1px]',
                      isSelected &&
                        'bg-main text-white shadow-[0.5px_0_4.5px_rgba(153,153,153,0.1)]',
                      cell.isToday &&
                        !isSelected &&
                        'underline underline-offset-4 decoration-black',
                      isDisabled && 'text-[#cccccc]',
                      !isDisabled && !isSelected && cell.weekday === 0 && 'text-[#ed5151]',
                      !isDisabled && !isSelected && cell.weekday === 6 && 'text-[#499eff]',
                      !isDisabled &&
                        !isSelected &&
                        cell.weekday !== 0 &&
                        cell.weekday !== 6 &&
                        'text-black',
                    )}
                  >
                    {cell.day}
                  </span>
                </button>
              )
            })}
          </div>
        </div>

        <div className="flex items-center justify-end gap-[15px] mt-4">
          <div className="flex items-center gap-[7px]">
            <div className="w-[13px] h-[13px] bg-main rounded-[1.5px]" />
            <span className="text-xs leading-[12px]">선택</span>
          </div>
          <div className="flex items-center gap-[7px]">
            <div className="w-[13px] h-[13px] bg-[#f5f5f5] rounded-[1.5px]" />
            <span className="text-xs leading-[12px]">예약불가</span>
          </div>
        </div>
      </div>

      {/* 시간 슬롯 */}
      <div className="mt-[30px]">{renderSlots()}</div>
    </div>
  )
}
