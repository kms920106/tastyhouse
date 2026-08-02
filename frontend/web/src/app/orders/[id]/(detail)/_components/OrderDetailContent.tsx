import CancelOrderButton from '@/app/orders/[id]/(detail)/_components/CancelOrderButton'
import OrderStatusHeader from '@/app/orders/[id]/(detail)/_components/OrderStatusHeader'
import OrderedProductList from '@/app/orders/[id]/(detail)/_components/OrderedProductList'
import OrdererInformationAccordion from '@/app/orders/[id]/(detail)/_components/OrdererInformationAccordion'
import PaymentBreakdownAccordion from '@/app/orders/[id]/(detail)/_components/PaymentBreakdownAccordion'
import PaymentInformationAccordion from '@/app/orders/[id]/(detail)/_components/PaymentInformationAccordion'
import RefundPolicySection from '@/app/orders/[id]/(detail)/_components/RefundPolicySection'
import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import { orderRepository } from '@/domains/order/order.repository'
import { PAGE_PATHS } from '@/lib/paths'
import { notFound, redirect } from 'next/navigation'

interface Props {
  orderId: number
}

export default async function OrderDetailContent({ orderId }: Props) {
  const { error, status, data } = await orderRepository.getOrderDetail(orderId)

  if (error && status === 401) {
    redirect(PAGE_PATHS.AUTH_LOGIN)
  }

  if ((error && status === 404) || !data) {
    notFound()
  }

  const {
    orderNumber,
    paymentStatus,
    shopName,
    shopPhoneNumber,
    orderProducts,
    ordererName,
    ordererPhone,
    ordererEmail,
    totalProductAmount,
    productDiscountAmount,
    couponDiscountAmount,
    pointDiscountAmount,
    totalDiscountAmount,
    finalAmount,
    payment,
  } = data

  return (
    <>
      <SectionStack>
        <BorderedSection>
          <OrderStatusHeader orderNumber={orderNumber} paymentStatus={paymentStatus} />
        </BorderedSection>
        <BorderedSection>
          <OrderedProductList shopName={shopName} orderProducts={orderProducts} />
        </BorderedSection>
        <BorderedSection>
          <OrdererInformationAccordion
            ordererName={ordererName}
            ordererPhone={ordererPhone}
            ordererEmail={ordererEmail}
          />
        </BorderedSection>
        <BorderedSection>
          <PaymentInformationAccordion payment={payment} />
        </BorderedSection>
        <BorderedSection>
          <PaymentBreakdownAccordion
            totalProductAmount={totalProductAmount}
            productDiscountAmount={productDiscountAmount}
            couponDiscountAmount={couponDiscountAmount}
            pointDiscountAmount={pointDiscountAmount}
            totalDiscountAmount={totalDiscountAmount}
            finalAmount={finalAmount}
          />
        </BorderedSection>
        <BorderedSection>
          <RefundPolicySection />
        </BorderedSection>
      </SectionStack>
      <CancelOrderButton
        paymentId={payment.id}
        paymentStatus={paymentStatus}
        phoneNumber={shopPhoneNumber}
      />
    </>
  )
}
