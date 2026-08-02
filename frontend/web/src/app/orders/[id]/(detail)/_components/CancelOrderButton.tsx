'use client'

import type { CancelPaymentResult } from '@/actions/payment'
import { cancelPayment } from '@/actions/payment'
import AppConfirmDialog from '@/components/ui/AppConfirmDialog'
import AppSubmitButton from '@/components/ui/AppSubmitButton'
import { toast } from '@/components/ui/AppToaster'
import type { PaymentStatus } from '@/domains/payment'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import CancelResultDialog from './CancelResultDialog'

interface Props {
  paymentId: number
  paymentStatus: PaymentStatus
  phoneNumber: string
}

export default function CancelOrderButton({ paymentId, paymentStatus, phoneNumber }: Props) {
  const router = useRouter()

  const [isLoading, setIsLoading] = useState(false)
  const [cancelResult, setCancelResult] = useState<CancelPaymentResult | null>(null)
  const [showCancelConfirm, setShowCancelConfirm] = useState(false)

  const handleCancelClick = () => {
    if (paymentStatus === 'CANCELLED') {
      setCancelResult({ success: true, code: 'ALREADY_CANCELLED' })
      return
    }

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

    if (!result.success) {
      toast('결제 취소 중 오류가 발생했습니다.')
      return
    }

    setCancelResult(result)
    if (result.code === 'SUCCESS') {
      router.refresh()
    }
  }

  return (
    <>
      <div className="px-[15px] py-5">
        <AppSubmitButton onClick={handleCancelClick} isSubmitting={isLoading} loadingText="취소 중">
          결제취소
        </AppSubmitButton>
      </div>
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
        cancelResultCode={cancelResult?.success ? cancelResult.code : null}
        phoneNumber={phoneNumber}
        onClose={() => setCancelResult(null)}
      />
    </>
  )
}
