import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import Icon from '@/components/ui/Icon'
import { reservationRepository } from '@/domains/reservation/reservation.repository'
import { formatReservationSummary } from '@/lib/date'
import { PAGE_PATHS } from '@/lib/paths'
import Image from 'next/image'
import Link from 'next/link'
import { notFound, redirect } from 'next/navigation'

interface Props {
  reservationId: number
}

export default async function ReservationCompleteContent({ reservationId }: Props) {
  const { error, status, data } = await reservationRepository.getReservationComplete(reservationId)

  if (error && status === 401) {
    redirect(PAGE_PATHS.AUTH_LOGIN)
  }

  if ((error && status === 404) || !data) {
    notFound()
  }

  const { shopName, shopImageUrl, reservationAt, partySize } = data

  return (
    <>
      <div className="flex-1 flex flex-col items-center justify-center gap-[30px] px-[15px]">
        <div className="relative w-[95px] h-[95px]">
          <Icon name="circle-red" fill className="object-contain" />
          <div className="absolute inset-0 flex items-center justify-center">
            <Icon name="check-red" alt="예약완료" />
          </div>
        </div>
        <div className="flex flex-col items-center text-center gap-[15px]">
          <h2 className="text-[23px] leading-[23px]">예약이 완료되었습니다.</h2>
          <p className="text-sm leading-relaxed text-[#999999]">
            예약 취소는 상세 페이지에서 가능합니다.
          </p>
        </div>
        <div className="w-full rounded-xl border border-line p-[20px]">
          <div className="flex items-center gap-[12px]">
            <div className="relative w-[60px] h-[60px] rounded-lg overflow-hidden shrink-0">
              <Image src={shopImageUrl} alt={shopName} fill className="object-cover" />
            </div>
            <div className="flex flex-col gap-2">
              <p className="text-sm leading-[14px] text-[#888888] truncate">{shopName}</p>
              <p className="text-sm leading-[14px]">
                {formatReservationSummary(reservationAt, partySize)}
              </p>
            </div>
          </div>
        </div>
      </div>
      <div className="px-[15px] py-2.5">
        <Link href={PAGE_PATHS.RESERVATION_DETAIL(reservationId)}>
          <AppPrimaryButton>예약 내역 보기</AppPrimaryButton>
        </Link>
      </div>
    </>
  )
}
