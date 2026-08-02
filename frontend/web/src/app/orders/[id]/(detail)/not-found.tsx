import ErrorStateSection from '@/components/ui/ErrorStateSection'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'

export default function NotFound() {
  return <ErrorStateSection title="결제 내역" message={COMMON_ERROR_MESSAGES.FETCH_ERROR('주문')} />
}
