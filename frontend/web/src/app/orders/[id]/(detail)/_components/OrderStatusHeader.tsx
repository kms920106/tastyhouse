import AppBadge from '@/components/ui/AppBadge'
import { getPaymentStatusColor, getPaymentStatusName } from '@/domains/payment'
import type { PaymentStatus } from '@/domains/payment'

interface Props {
  orderNumber: string
  paymentStatus: PaymentStatus
}

export default function OrderStatusHeader({ orderNumber, paymentStatus }: Props) {
  const statusColor = getPaymentStatusColor(paymentStatus)
  const statusName = getPaymentStatusName(paymentStatus)

  return (
    <div className="px-4 py-4 flex items-center justify-between">
      <span className="text-[13px] leading-[13px]">{orderNumber}</span>
      <AppBadge
        className="px-[11px] py-[7px] text-[11px] leading-[11px] rounded-[12.5px] border-none"
        style={{ backgroundColor: statusColor }}
      >
        {statusName}
      </AppBadge>
    </div>
  )
}
