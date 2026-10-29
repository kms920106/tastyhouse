import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import Icon from '@/components/ui/Icon'
import { orderRepository } from '@/domains/order/order.repository'
import { isOnSitePaymentMethod } from '@/domains/payment'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import { notFound, redirect } from 'next/navigation'

interface Props {
  orderId: number
}

export default async function OrderCompleteContent({ orderId }: Props) {
  const { error, status, data } = await orderRepository.getOrderDetail(orderId)

  if (error && status === 401) {
    redirect(PAGE_PATHS.AUTH_LOGIN)
  }

  if ((error && status === 404) || !data) {
    notFound()
  }

  const { orderNumber, payment } = data
  const awaitingOnSitePayment =
    payment?.paymentStatus === 'PENDING' && isOnSitePaymentMethod(payment.paymentMethod)

  return (
    <>
      <div className="flex-1 flex flex-col items-center justify-center">
        <div className="relative w-[95px] h-[95px]">
          <Icon name="circle-red" fill className="object-contain" />
          <div className="absolute inset-0 flex items-center justify-center">
            <Icon name="check-red" alt="결제완료" />
          </div>
        </div>
        <div className="flex flex-col items-center text-center">
          <p className="mt-[30px] text-sm leading-[14px] text-[#666666]">
            주문번호 : {orderNumber}
          </p>
          <h2 className="mt-[15px] text-[23px] leading-[23px]">
            {awaitingOnSitePayment ? '주문이 접수되었습니다.' : '결제가 완료되었습니다.'}
          </h2>
          {awaitingOnSitePayment && (
            <p className="mt-[21px] text-sm leading-relaxed text-[#666666]">
              결제는 매장에서 진행해 주세요.
              <br />
              매장에서 결제를 확인하면 포인트가 적립됩니다.
            </p>
          )}
          <p className="mt-[21px] text-sm leading-relaxed text-[#999999]">
            결제 취소는 상세 페이지에서 가능합니다.
          </p>
        </div>
      </div>
      <div className="px-[15px] py-2.5">
        <Link href={PAGE_PATHS.ORDER_DETAIL(orderId)}>
          <AppPrimaryButton>주문 내역 보기</AppPrimaryButton>
        </Link>
      </div>
    </>
  )
}
