import AppBadge from '@/components/ui/AppBadge'
import type { ReservationStatus } from '@/domains/reservation'
import { getReservationStatusColor, getReservationStatusName } from '@/domains/reservation'

interface Props {
  reservationNumber: string
  status: ReservationStatus
}

export default function ReservationStatusHeader({ reservationNumber, status }: Props) {
  const statusColor = getReservationStatusColor(status)
  const statusName = getReservationStatusName(status)

  return (
    <div className="px-4 py-4 flex items-center justify-between">
      <span className="text-[13px] leading-[13px]">{reservationNumber}</span>
      <AppBadge
        className="px-[11px] py-[7px] text-[11px] leading-[11px] rounded-[12.5px] border-none"
        style={{ backgroundColor: statusColor }}
      >
        {statusName}
      </AppBadge>
    </div>
  )
}
