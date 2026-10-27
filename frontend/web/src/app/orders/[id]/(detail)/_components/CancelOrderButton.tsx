'use client'

import { cancelPayment } from '@/actions/payment'
import AppConfirmDialog from '@/components/ui/AppConfirmDialog'
import AppSubmitButton from '@/components/ui/AppSubmitButton'
import { toast } from '@/components/ui/AppToaster'
import type { PaymentStatus } from '@/domains/payment'
import { useCancelResultCheck } from '@/domains/payment/payment.hook'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import CancelResultDialog, { type CancelResultDialogCode } from './CancelResultDialog'

interface Props {
  orderId: number
  paymentId: number
  paymentStatus: PaymentStatus
  phoneNumber: string
}

export default function CancelOrderButton({
  orderId,
  paymentId,
  paymentStatus,
  phoneNumber,
}: Props) {
  const router = useRouter()

  const [isLoading, setIsLoading] = useState(false)
  const [isCheckingResult, setIsCheckingResult] = useState(false)
  const [isLocked, setIsLocked] = useState(false)
  const [cancelResultCode, setCancelResultCode] = useState<CancelResultDialogCode | null>(null)
  const [showCancelConfirm, setShowCancelConfirm] = useState(false)

  const checkStatus = useCancelResultCheck(orderId, isCheckingResult)

  useEffect(() => {
    if (checkStatus !== 'cancelled' && checkStatus !== 'unconfirmed') return

    setCancelResultCode(checkStatus === 'cancelled' ? 'SUCCESS' : 'RESULT_UNCONFIRMED')
    setIsCheckingResult(false)
    setIsLocked(true)
    router.refresh()
  }, [checkStatus, router])

  const handleCancelClick = () => {
    setShowCancelConfirm(true)
  }

  const handleCancelPayment = async () => {
    setShowCancelConfirm(false)

    setIsLoading(true)

    const result = await cancelPayment({
      paymentId,
      cancelReason: '사용자 결제 취소 요청',
    })

    setIsLoading(false)

    if (result.success) {
      setCancelResultCode(result.code)
      if (result.code === 'SUCCESS') {
        router.refresh()
      }
      return
    }

    // 서버가 취소를 끝냈을 수 있으므로 실패로 보이지 않고 결제 상태를 다시 조회한다
    if ('unconfirmed' in result) {
      setIsCheckingResult(true)
      return
    }

    toast(result.error ?? '결제 취소 중 오류가 발생했습니다.')
  }

  return (
    <>
      {paymentStatus !== 'CANCELLED' && (
        <div className="px-[15px] py-5">
          <AppSubmitButton
            onClick={handleCancelClick}
            isSubmitting={isLoading || isCheckingResult}
            disabled={isLocked}
            loadingText={isCheckingResult ? '취소 결과를 확인하고 있습니다' : '취소 중'}
          >
            결제취소
          </AppSubmitButton>
        </div>
      )}
      <AppConfirmDialog
        open={showCancelConfirm}
        title="주문을 취소하시겠습니까?"
        description={`주문을 취소하시면 결제 금액은 전액 환불됩니다.\n취소 이후에는 다시 되돌릴 수 없습니다.`}
        onConfirm={handleCancelPayment}
        onCancel={() => setShowCancelConfirm(false)}
        cancelLabel="취소"
        confirmLabel="확인"
      />
      <CancelResultDialog
        cancelResultCode={cancelResultCode}
        phoneNumber={phoneNumber}
        onClose={() => setCancelResultCode(null)}
      />
    </>
  )
}
