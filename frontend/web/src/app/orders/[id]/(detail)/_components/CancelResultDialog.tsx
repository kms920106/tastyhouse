'use client'

import AppAlertDialog from '@/components/ui/AppAlertDialog'
import { Button } from '@/components/ui/shadcn/button'
import type { PaymentCancelCode } from '@/domains/payment'
import { formatPhoneNumber } from '@/lib/utils'
import { useRouter } from 'next/navigation'
import { BiSupport } from 'react-icons/bi'
import { IoIosCall } from 'react-icons/io'

interface CancelResultModalConfig {
  title: string
  description: string
  showContactStore: boolean
  showContactSupport: boolean
}

const CANCEL_RESULT_CONFIG: Record<PaymentCancelCode, CancelResultModalConfig> = {
  SUCCESS: {
    title: '알림',
    description:
      '주문이 정상적으로 취소되었습니다.\n카드 승인이 취소 후 환불되며, 카드사의 사정에 따라 승인 취소 후 환불이 지연될 수 있습니다.',
    showContactStore: false,
    showContactSupport: false,
  },
  ALREADY_PREPARING: {
    title: '알림',
    description:
      '이미 조리가 시작되어 바로 취소가 어렵습니다.\n아래 방법으로 도와드릴 수 있습니다.',
    showContactStore: true,
    showContactSupport: true,
  },
  ALREADY_CANCELLED: {
    title: '알림',
    description:
      '이 주문은 이미 취소되었습니다.\n결제 금액은 카드사 환불 진행 중이거나 이미 환불 완료된 상태입니다.',
    showContactStore: false,
    showContactSupport: false,
  },
  ORDER_COMPLETED: {
    title: '알림',
    description: '이 주문은 이미 완료되었습니다.\n고객센터에 문의해 주세요.',
    showContactStore: false,
    showContactSupport: true,
  },
  CANCEL_FAILED: {
    title: '알림',
    description: '결제 취소 중 오류가 발생했습니다.\n고객센터에 문의해 주세요.',
    showContactStore: false,
    showContactSupport: true,
  },
}

interface Props {
  cancelResultCode: PaymentCancelCode | null
  phoneNumber: string
  onClose: () => void
}

export default function CancelResultDialog({
  cancelResultCode,
  phoneNumber,
  onClose,
}: Props) {
  const router = useRouter()

  if (!cancelResultCode) return null

  const config = CANCEL_RESULT_CONFIG[cancelResultCode]
  const showContact = config.showContactStore || config.showContactSupport

  const handleContactStore = () => {
    onClose()
    window.location.href = `tel:${phoneNumber}`
  }

  const handleContactSupport = () => {
    onClose()
    router.push('/customer-center')
  }

  return (
    <AppAlertDialog
      open
      onOpenChange={(open) => !open && onClose()}
      title={config.title}
      description={config.description}
      descriptionComponent={
        showContact ? (
          <div className="flex flex-col gap-2">
            {config.showContactStore && (
              <Button variant="outline" size="sm" onClick={handleContactStore}>
                <IoIosCall />
                가게로 전화하기 ({formatPhoneNumber(phoneNumber)})
              </Button>
            )}
            {config.showContactSupport && (
              <Button variant="outline" size="sm" onClick={handleContactSupport}>
                <BiSupport />
                고객센터로 문의하기
              </Button>
            )}
          </div>
        ) : undefined
      }
      onConfirm={onClose}
      confirmLabel="확인"
    />
  )
}
