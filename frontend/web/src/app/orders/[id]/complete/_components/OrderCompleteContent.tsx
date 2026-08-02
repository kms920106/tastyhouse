import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import Icon from '@/components/ui/Icon'
import { orderRepository } from '@/domains/order/order.repository'
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

  const { orderNumber } = data

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
          <h2 className="mt-[15px] text-[23px] leading-[23px]">결제가 완료되었습니다.</h2>
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
