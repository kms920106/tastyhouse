'use client'

import {
  fetchAgeVerificationContent,
  fetchElectronicFinancialTransactionsContent,
  fetchPrivacyPolicyContent,
  fetchTermsOfServiceContent,
} from '@/actions/policies'
import OrderRequestField from '@/components/orders/OrderRequestField'
import OrderTermsSection, {
  ORDER_TERMS_LIST,
  type OrderTermsKey,
} from '@/components/orders/OrderTermsSection'
import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import AppTermsDialog from '@/components/ui/AppTermsDialog'
import { toast } from '@/components/ui/AppToaster'
import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import StickyFooter from '@/components/ui/StickyFooter'
import {
  useCreateReservation,
  useReservationAvailability,
} from '@/domains/reservation/reservation.hook'
import { getCurrentYearMonth } from '@/lib/date'
import { PAGE_PATHS } from '@/lib/paths'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { shiftMonth } from '../_lib/calendar'
import ReservationDateTime from './ReservationDateTime'
import ReservationGuestCounter from './ReservationGuestCounter'
import ReservationNotice from './ReservationNotice'

interface Props {
  shopId: number
}

const INITIAL_AGREED_TERMS: Record<OrderTermsKey, boolean> = {
  agreedTerms: false,
  agreedPrivacy: false,
  agreedFinance: false,
  agreedAge: false,
  agreedEvent: false,
}

const TERMS_FETCH_MAP: Partial<Record<OrderTermsKey, () => Promise<string>>> = {
  agreedTerms: fetchTermsOfServiceContent,
  agreedPrivacy: fetchPrivacyPolicyContent,
  agreedFinance: fetchElectronicFinancialTransactionsContent,
  agreedAge: fetchAgeVerificationContent,
}

export default function ReservationContentClient({ shopId }: Props) {
  const router = useRouter()

  const [viewMonth, setViewMonth] = useState(getCurrentYearMonth)
  const [selectedDate, setSelectedDate] = useState<string | null>(null)
  const [selectedTime, setSelectedTime] = useState<string | null>(null)
  const [guestCount, setGuestCount] = useState(1)
  const [request, setRequest] = useState('')

  const [agreedAll, setAgreedAll] = useState(false)
  const [agreedTerms, setAgreedTerms] =
    useState<Record<OrderTermsKey, boolean>>(INITIAL_AGREED_TERMS)

  const [termsDialog, setTermsDialog] = useState<{
    open: boolean
    title: string
    htmlContent: string
  }>({ open: false, title: '', htmlContent: '' })

  useEffect(() => {
    setAgreedAll(ORDER_TERMS_LIST.every(({ key }) => agreedTerms[key]))
  }, [agreedTerms])

  // 슬롯 가용성 조회 — 날짜 선택 시마다 호출 (enabled: !!selectedDate)
  const {
    data: availabilityData,
    isLoading: isLoadingSlots,
    isError: isErrorSlots,
  } = useReservationAvailability(shopId, selectedDate)

  const slots = availabilityData?.data?.slots ?? []
  // 선택한 날짜·매장에 이미 차단 예약이 있는지 (true면 그 날짜 슬롯 전체가 available:false)
  const hasMyReservation = availabilityData?.data?.hasMyReservation ?? false

  // 예약 생성 mutation — 성공 시 toast 없이 완료 페이지로 이동, 실패 toast는 hook이 책임 (도메인 가이드 §8.9)
  const { mutate: reserve, isPending } = useCreateReservation((reservationId) =>
    router.replace(PAGE_PATHS.RESERVATION_COMPLETE(reservationId)),
  )

  // 이미 예약이 있는 날짜 — 기존 예약 확인·변경은 예약 내역 페이지에서 처리
  const handleViewReservations = () => {
    router.push(PAGE_PATHS.RESERVATIONS)
  }

  const handleAgreedAll = (checked: boolean) => {
    setAgreedAll(checked)
    setAgreedTerms(
      Object.fromEntries(ORDER_TERMS_LIST.map(({ key }) => [key, checked])) as Record<
        OrderTermsKey,
        boolean
      >,
    )
  }

  const handleTermChange = (key: OrderTermsKey, checked: boolean) => {
    setAgreedTerms((prev) => ({ ...prev, [key]: checked }))
  }

  const handleOpenTermsDialog = async (key: OrderTermsKey, label: string) => {
    const fetcher = TERMS_FETCH_MAP[key]
    if (!fetcher) return
    try {
      const htmlContent = await fetcher()
      setTermsDialog({ open: true, title: label, htmlContent })
    } catch {
      toast('약관 내용을 불러오는데 실패했습니다.')
    }
  }

  // 날짜 변경 시 선택된 시간 리셋 (다른 날짜의 시간이 잔존하는 버그 방지)
  const handleSelectDate = (date: string) => {
    setSelectedDate(date)
    setSelectedTime(null)
  }

  const requiredTermsOk = ORDER_TERMS_LIST.filter((t) => t.required).every(
    ({ key }) => agreedTerms[key],
  )

  const handleReserve = () => {
    if (isPending) return

    if (!selectedDate) {
      toast('예약 날짜를 선택해주세요.')
      return
    }
    // 이미 그 날짜·매장에 예약이 있으면 제출 차단 (선제 가드 — 서버 409와 이중 방어)
    if (hasMyReservation) {
      toast('이미 이 날짜에 예약이 있어요.\n예약 내역에서 기존 예약을 취소한 뒤 다시 시도해 주세요.')
      return
    }
    if (!selectedTime) {
      toast('예약 시간을 선택해주세요.')
      return
    }
    if (!requiredTermsOk) {
      toast('필수 약관에 모두 동의해주세요.')
      return
    }

    reserve({
      shopId,
      reservationDate: selectedDate,
      reservationTime: selectedTime,
      partySize: guestCount,
      request: request.trim() || undefined,
      agreedRequiredTerms: requiredTermsOk,
    })
  }

  return (
    <>
      <SectionStack className="flex-1">
        <BorderedSection>
          <ReservationDateTime
            viewMonth={viewMonth}
            selectedDate={selectedDate}
            selectedTime={selectedTime}
            slots={slots}
            isLoadingSlots={isLoadingSlots}
            isErrorSlots={isErrorSlots}
            hasSelectedDate={!!selectedDate}
            hasMyReservation={hasMyReservation}
            onChangeMonth={(delta) => setViewMonth((prev) => shiftMonth(prev, delta))}
            onSelectDate={handleSelectDate}
            onSelectTime={setSelectedTime}
            onViewReservations={handleViewReservations}
          />
        </BorderedSection>
        <BorderedSection>
          <ReservationGuestCounter count={guestCount} onChange={setGuestCount} max={50} />
        </BorderedSection>
        <BorderedSection>
          <OrderRequestField value={request} onChange={setRequest} />
        </BorderedSection>
        <BorderedSection>
          <ReservationNotice />
        </BorderedSection>
        <BorderedSection>
          <OrderTermsSection
            agreedAll={agreedAll}
            agreedTerms={agreedTerms}
            onAgreedAllChange={handleAgreedAll}
            onTermChange={handleTermChange}
            onOpenTermsDialog={handleOpenTermsDialog}
          />
        </BorderedSection>
      </SectionStack>

      <StickyFooter>
        <div className="px-[15px] py-2.5">
          <AppPrimaryButton onClick={handleReserve} disabled={isPending}>
            {isPending ? '예약 처리 중...' : '예약하기'}
          </AppPrimaryButton>
        </div>
      </StickyFooter>

      <AppTermsDialog
        open={termsDialog.open}
        onOpenChange={(open) => setTermsDialog((prev) => ({ ...prev, open }))}
        title={termsDialog.title}
      >
        <div dangerouslySetInnerHTML={{ __html: termsDialog.htmlContent }} />
      </AppTermsDialog>
    </>
  )
}
