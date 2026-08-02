'use client'

import AppAlertDialog from '@/components/ui/AppAlertDialog'
import type { CancelReservationCode } from '@/actions/reservation'

interface CancelReservationResultConfig {
  title: string
  description: string
}

const CANCEL_RESULT_CONFIG: Record<CancelReservationCode, CancelReservationResultConfig> = {
  SUCCESS: {
    title: '알림',
    description: '예약이 정상적으로 취소되었습니다.',
  },
  ALREADY_PROCESSED: {
    title: '알림',
    description: '이미 취소되었거나 처리된 예약입니다.\n예약 상태를 다시 확인해 주세요.',
  },
  UNAUTHORIZED: {
    title: '알림',
    description: '예약을 취소할 수 있는 권한이 없습니다.\n로그인 상태를 확인해 주세요.',
  },
  NOT_FOUND: {
    title: '알림',
    description: '예약 정보를 찾을 수 없습니다.\n잠시 후 다시 시도해 주세요.',
  },
  UNKNOWN: {
    title: '알림',
    description: '예약 취소 중 오류가 발생했습니다.\n잠시 후 다시 시도해 주세요.',
  },
}

interface Props {
  resultCode: CancelReservationCode | null
  onClose: () => void
}

export default function CancelReservationResultDialog({ resultCode, onClose }: Props) {
  if (!resultCode) return null

  const config = CANCEL_RESULT_CONFIG[resultCode]

  return (
    <AppAlertDialog
      open
      onOpenChange={(open) => !open && onClose()}
      title={config.title}
      description={config.description}
      onConfirm={onClose}
      confirmLabel="확인"
    />
  )
}
