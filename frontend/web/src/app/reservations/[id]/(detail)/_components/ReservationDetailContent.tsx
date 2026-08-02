import { getReservationDetail } from '@/actions/reservation'
import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import { formatDate } from '@/lib/date'
import { PAGE_PATHS } from '@/lib/paths'
import { notFound, redirect } from 'next/navigation'
import CancelReservationButton from './CancelReservationButton'
import ReservationInformationAccordion from './ReservationInformationAccordion'
import ReservationOrdererAccordion from './ReservationOrdererAccordion'
import ReservationRefundPolicySection from './ReservationRefundPolicySection'
import ReservationRequestAccordion from './ReservationRequestAccordion'
import ReservationShopSection from './ReservationShopSection'
import ReservationStatusHeader from './ReservationStatusHeader'

interface Props {
  reservationId: number
}

export default async function ReservationDetailContent({ reservationId }: Props) {
  const { error, status, data } = await getReservationDetail(reservationId)

  if (error && status === 401) {
    redirect(PAGE_PATHS.AUTH_LOGIN)
  }

  // 403(본인 예약 아님)은 리소스 존재를 노출하지 않도록 404와 동일하게 처리
  if ((error && (status === 403 || status === 404)) || !data) {
    notFound()
  }

  const {
    shopName,
    shopImageUrl,
    shopRoadAddress,
    shopLotAddress,
    reserverName,
    reserverPhoneNumber,
    reserverEmail,
    reservationAt,
    partySize,
    status: reservationStatus,
    request,
    createdAt,
  } = data

  const reservationNumber = `예약번호 ${reservationId}`
  const reservationDateText = formatDate(reservationAt, 'YYYY년 M월 D일')
  const reservationTimeText = formatDate(reservationAt, 'HH:mm')
  const createdAtText = formatDate(createdAt, 'YYYY-MM-DD HH:mm')

  return (
    <>
      <SectionStack>
        <BorderedSection>
          <ReservationStatusHeader
            reservationNumber={reservationNumber}
            status={reservationStatus}
          />
        </BorderedSection>
        <BorderedSection>
          <ReservationShopSection
            shopName={shopName}
            shopImageUrl={shopImageUrl}
            roadAddress={shopRoadAddress}
            lotAddress={shopLotAddress}
          />
        </BorderedSection>
        <BorderedSection>
          <ReservationOrdererAccordion
            ordererName={reserverName}
            ordererPhone={reserverPhoneNumber}
            ordererEmail={reserverEmail}
          />
        </BorderedSection>
        <BorderedSection>
          <ReservationInformationAccordion
            reservationDateText={reservationDateText}
            reservationTimeText={reservationTimeText}
            partySize={partySize}
            createdAtText={createdAtText}
          />
        </BorderedSection>
        {request && (
          <BorderedSection>
            <ReservationRequestAccordion request={request} />
          </BorderedSection>
        )}
        <BorderedSection>
          <ReservationRefundPolicySection />
        </BorderedSection>
      </SectionStack>
      <CancelReservationButton
        reservationId={reservationId}
        reservationStatus={reservationStatus}
      />
    </>
  )
}
