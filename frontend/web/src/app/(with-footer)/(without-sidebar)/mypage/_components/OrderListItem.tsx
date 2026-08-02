import ImageContainer from '@/components/ui/ImageContainer'
import { getPaymentStatusColor, getPaymentStatusName } from '@/domains/payment'
import { PaymentStatus } from '@/domains/payment'
import { formatDate } from '@/lib/date'
import { formatNumber } from '@/lib/number'
import { formatOrderSummary } from '@/lib/order'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'

interface Props {
  id: number
  shopThumbnailImageUrl: string
  shopName: string
  firstProductName: string
  totalItemCount: number
  price: number
  date: string
  paymentStatus: PaymentStatus
}

export default function OrderListItem({
  id,
  shopThumbnailImageUrl,
  shopName,
  firstProductName,
  totalItemCount,
  price,
  date,
  paymentStatus,
}: Props) {
  const statusColor = getPaymentStatusColor(paymentStatus)
  const statusName = getPaymentStatusName(paymentStatus)
  const formattedDate = formatDate(date, 'YY.MM.DD')

  return (
    <Link href={PAGE_PATHS.ORDER_DETAIL(id)} className="block">
      <div className="flex items-center justify-between py-[15px]">
        <div className="flex items-center gap-[15px]">
          <ImageContainer src={shopThumbnailImageUrl} alt={shopName} size={60} />
          <div className="flex-1 flex flex-col min-w-0">
            <p className="text-[11px] leading-[11px] text-[#888888] truncate">{shopName}</p>
            <p className="text-sm leading-[14px] mt-[7px]">
              {formatOrderSummary(firstProductName, totalItemCount)}
            </p>
            <p className="text-sm leading-[14px] mt-2.5">{formatNumber(price)}원</p>
          </div>
        </div>
        <div className="flex flex-col items-end gap-[7px] justify-between">
          <p className="text-[11px] leading-[11px] text-[#aaaaaa]">{formattedDate}</p>
          <p className="text-[11px] leading-[11px]" style={{ color: statusColor }}>
            {statusName}
          </p>
        </div>
      </div>
    </Link>
  )
}
