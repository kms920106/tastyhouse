import AppBadge from '@/components/ui/AppBadge'
import { getPaymentStatusColor, getPaymentStatusName } from '@/domains/payment'
import type { PaymentStatus } from '@/domains/payment'
import { formatScheduledPickupLabel } from '@/lib/order'

interface Props {
  orderNumber: string
  paymentStatus: PaymentStatus
  /** 수령 예약 시각. null이면 즉시 주문이라 배지를 노출하지 않는다 */
  scheduledAt: string | null
}

export default function OrderStatusHeader({ orderNumber, paymentStatus, scheduledAt }: Props) {
  const statusColor = getPaymentStatusColor(paymentStatus)
  const statusName = getPaymentStatusName(paymentStatus)
  const scheduledPickupLabel = formatScheduledPickupLabel(scheduledAt)

  return (
    <div className="px-4 py-4 flex flex-col gap-2.5">
      <div className="flex items-center justify-between">
        <span className="text-[13px] leading-[13px]">{orderNumber}</span>
        <AppBadge
          className="px-[11px] py-[7px] text-[11px] leading-[11px] rounded-[12.5px] border-none"
          style={{ backgroundColor: statusColor }}
        >
          {statusName}
        </AppBadge>
      </div>
      {scheduledPickupLabel && (
        <AppBadge className="self-start px-[11px] py-[7px] text-[11px] leading-[11px] rounded-[12.5px] border-line bg-transparent text-main">
          {scheduledPickupLabel}
        </AppBadge>
      )}
    </div>
  )
}
