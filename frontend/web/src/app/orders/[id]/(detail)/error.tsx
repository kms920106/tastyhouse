'use client'

import ErrorRenderSection from '@/components/ui/ErrorRenderSection'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'

export default function OrderDetailError() {
  return <ErrorRenderSection title="결제 내역" message={COMMON_ERROR_MESSAGES.RENDER_ERROR} />
}
