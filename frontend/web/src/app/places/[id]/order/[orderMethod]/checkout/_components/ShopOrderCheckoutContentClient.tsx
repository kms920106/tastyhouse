'use client'

import { createOrder } from '@/actions/order'
import { completeOnSitePayment, createPayment } from '@/actions/payment'
import OrderRequestField from '@/components/orders/OrderRequestField'
import { toast } from '@/components/ui/AppToaster'
import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import type { MemberCoupon, MemberPersonalInfo } from '@/domains/member'
import type { OrderMethodType } from '@/domains/order'
import type { PaymentMethod } from '@/domains/payment'
import { Shop } from '@/domains/shop'
import { useCartInfo } from '@/hooks/useCartInfo'
import { useTossPayments } from '@/hooks/useTossPayments'
import { PAGE_PATHS } from '@/lib/paths'
import { calculatePaymentSummary } from '@/lib/paymentCalculation'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import CustomerInfoSection from './CustomerInfoSection'
import DiscountApplicationSection from './DiscountApplicationSection'
import OrderInfoSection from './OrderInfoSection'
import OrderTermsAgreement from './OrderTermsAgreement'
import PaymentActionBar from './PaymentActionBar'
import PaymentMethodSelector from './PaymentMethodSelector'
import PaymentSummarySection from './PaymentSummarySection'

interface Props {
  shop: Shop
  member: MemberPersonalInfo
  availableCoupons: MemberCoupon[]
  usablePoints: number
  orderMethod: OrderMethodType
}

const MAX_REQUEST_LENGTH = 200

export default function ShopOrderCheckoutContentClient({
  shop,
  member,
  availableCoupons,
  usablePoints,
  orderMethod,
}: Props) {
  const router = useRouter()

  const { id: shopId, name: shopName } = shop
  const { fullName, phoneNumber, email } = member

  const { items, firstProductName, totalItemCount, totalProductAmount, totalProductDiscount } =
    useCartInfo()

  const [selectedCoupon, setSelectedCoupon] = useState<MemberCoupon | null>(null)
  const [pointInput, setPointInput] = useState('')
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState<PaymentMethod | null>(null)
  const [agreedToTerms, setAgreedToTerms] = useState(false)
  const [request, setRequest] = useState('')

  const { tossPayment } = useTossPayments()

  const { totalDiscountAmount, couponDiscount, pointsUsed, paymentAmount } =
    calculatePaymentSummary(totalProductAmount, totalProductDiscount, selectedCoupon, pointInput)

  const handlePayment = async () => {
    if (!agreedToTerms) {
      toast('약관에 동의해 주세요.')
      return
    }

    if (!selectedPaymentMethod) {
      toast('결제 수단을 선택해 주세요.')
      return
    }

    const trimmedRequest = request.trim()

    // 1. 주문 생성 (PENDING)
    // 장바구니 표시용 아이템(OrderProduct)을 서버 요청 DTO(OrderProductRequest)로 변환한다.
    // 서버는 productId / quantity / options(groupId, optionId)만 신뢰하며,
    // 금액·옵션 가격은 DB에서 재계산하므로 표시용 필드는 전송하지 않는다.
    const orderProducts = items.map((item) => ({
      productId: item.productId,
      quantity: item.quantity,
      options: item.options.map((option) => ({
        groupId: option.groupId,
        optionId: option.optionId,
      })),
    }))

    const orderResult = await createOrder({
      shopId,
      orderMethod,
      orderProducts,
      memberCouponId: selectedCoupon?.id ?? null,
      usePoint: pointsUsed,
      totalProductAmount,
      totalDiscountAmount,
      productDiscountAmount: totalProductDiscount,
      couponDiscountAmount: couponDiscount,
      finalAmount: paymentAmount,
      request: trimmedRequest,
    })

    if (orderResult.error) {
      toast(orderResult.error)
      return
    }

    const orderId = orderResult.data?.id
    if (!orderId) {
      toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
      return
    }

    // 2. 결제 생성 (PENDING)
    const paymentResult = await createPayment({
      orderId,
      paymentMethod: selectedPaymentMethod,
    })

    if (paymentResult.error) {
      toast(paymentResult.error)
      return
    }

    if (!paymentResult.data) {
      toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
      return
    }

    const paymentId = paymentResult.data.id
    if (!paymentId) {
      toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
      return
    }

    // 3-A. 현장결제 완료 처리 (COMPLETED)
    if (selectedPaymentMethod === 'CASH_ON_SITE' || selectedPaymentMethod === 'CARD_ON_SITE') {
      const completeResult = await completeOnSitePayment(paymentId)

      if (completeResult.error) {
        toast(completeResult.error)
        return
      }

      // [이전 방식] push: 히스토리 스택에 결제완료 페이지 추가 → 뒤로가기 시 결제하기 페이지로 돌아옴
      // router.push(PAGE_PATHS.ORDER_COMPLETE(orderId))

      // [현재 방식] replace: 결제하기 페이지를 히스토리에서 교체 → 뒤로가기 시 결제하기 페이지로 돌아오지 않음
      router.replace(PAGE_PATHS.ORDER_COMPLETE(orderId))
      return
    }

    // 3-B. 신용카드 결제 - PG 결제창 호출
    if (selectedPaymentMethod === 'CREDIT_CARD') {
      if (!tossPayment) {
        toast('결제 모듈을 불러오는 중입니다. 잠시 후 다시 시도해 주세요.')
        return
      }

      const orderName =
        totalItemCount > 1 ? `${firstProductName} 외 ${totalItemCount - 1}건` : firstProductName

      await tossPayment.requestPayment({
        method: 'CARD',
        amount: {
          currency: 'KRW',
          value: paymentAmount,
        },
        orderId: paymentResult.data.pgOrderId,
        orderName,
        successUrl: `${window.location.origin}/api/payments/tosspayments/success`,
        failUrl: `${window.location.origin}/api/payments/tosspayments/fail`,
        customerEmail: member.email,
        customerName: member.fullName,
        customerMobilePhone: member.phoneNumber,
        card: {
          useEscrow: false,
          flowMode: 'DEFAULT',
          useCardPoint: false,
          useAppCardOnly: false,
        },
      })
    }
  }

  return (
    <>
      <SectionStack>
        <BorderedSection>
          <OrderInfoSection
            shopName={shopName}
            orderProducts={items}
            firstProductName={firstProductName}
            totalItemCount={totalItemCount}
          />
        </BorderedSection>
        <BorderedSection>
          <CustomerInfoSection fullName={fullName} phoneNumber={phoneNumber} email={email} />
        </BorderedSection>
        <BorderedSection>
          <OrderRequestField value={request} onChange={setRequest} maxLength={MAX_REQUEST_LENGTH} />
        </BorderedSection>
        <BorderedSection>
          <DiscountApplicationSection
            availableCoupons={availableCoupons}
            totalProductAmount={totalProductAmount}
            totalProductDiscountAmount={totalProductDiscount}
            selectedCoupon={selectedCoupon}
            onCouponSelect={setSelectedCoupon}
            availablePoints={usablePoints}
            pointInput={pointInput}
            onPointInputChange={setPointInput}
          />
        </BorderedSection>
        <BorderedSection>
          <PaymentSummarySection
            totalProductAmount={totalProductAmount}
            totalProductDiscountAmount={totalProductDiscount}
            totalDiscountAmount={totalDiscountAmount}
            couponDiscount={couponDiscount}
            pointsUsed={pointsUsed}
            finalTotal={paymentAmount}
          />
        </BorderedSection>
        <BorderedSection>
          <PaymentMethodSelector
            selectedPaymentMethod={selectedPaymentMethod}
            onPaymentMethodSelect={setSelectedPaymentMethod}
          />
        </BorderedSection>
        <BorderedSection>
          <OrderTermsAgreement agreed={agreedToTerms} onAgreementChange={setAgreedToTerms} />
        </BorderedSection>
      </SectionStack>
      <PaymentActionBar onPaymentClick={handlePayment} />
    </>
  )
}
