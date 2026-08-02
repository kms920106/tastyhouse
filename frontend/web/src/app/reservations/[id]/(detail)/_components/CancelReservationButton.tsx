'use client'

import type { CancelReservationCode } from '@/actions/reservation'
import { cancelReservation } from '@/actions/reservation'
import AppConfirmDialog from '@/components/ui/AppConfirmDialog'
import AppSubmitButton from '@/components/ui/AppSubmitButton'
import { toast } from '@/components/ui/AppToaster'
import type { ReservationStatus } from '@/domains/reservation'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import CancelReservationResultDialog from './CancelReservationResultDialog'

const CANCELABLE_STATUSES: ReservationStatus[] = ['PENDING', 'CONFIRMED']

interface Props {
  reservationId: number
  reservationStatus: ReservationStatus
}

export default function CancelReservationButton({ reservationId, reservationStatus }: Props) {
  const router = useRouter()

  const [isLoading, setIsLoading] = useState(false)
  const [resultCode, setResultCode] = useState<CancelReservationCode | null>(null)
  const [showCancelConfirm, setShowCancelConfirm] = useState(false)

  const handleCancelClick = () => {
    if (!CANCELABLE_STATUSES.includes(reservationStatus)) {
      toast('이 예약은 이미 취소되었습니다.\n고객센터에 문의해 주세요.')
      return
    }

    setShowCancelConfirm(true)
  }

  const handleCancelReservation = async () => {
    setShowCancelConfirm(false)

    setIsLoading(true)

    const result = await cancelReservation(reservationId)

    setIsLoading(false)

    setResultCode(result.code)
  }

  const handleResultClose = () => {
    const isSuccess = resultCode === 'SUCCESS'

    setResultCode(null)

    if (isSuccess) {
      router.refresh()
    }
  }

  return (
    <>
      <div className="px-[15px] py-5">
        <AppSubmitButton onClick={handleCancelClick} isSubmitting={isLoading} loadingText="취소 중">
          예약취소
        </AppSubmitButton>
      </div>
      <AppConfirmDialog
        open={showCancelConfirm}
        title="예약을 취소하시겠습니까?"
        description={`예약을 취소하시면 다시 되돌릴 수 없습니다.\n취소 후 재예약이 필요합니다.`}
        onConfirm={handleCancelReservation}
        onCancel={() => setShowCancelConfirm(false)}
        cancelLabel="취소"
        confirmLabel="확인"
      />
      <CancelReservationResultDialog resultCode={resultCode} onClose={handleResultClose} />
    </>
  )
}
