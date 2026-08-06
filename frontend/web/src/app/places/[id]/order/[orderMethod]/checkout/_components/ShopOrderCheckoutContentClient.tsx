'use client'

import { createOrder } from '@/actions/order'
import { completeOnSitePayment, createPayment } from '@/actions/payment'
import OrderRequestField from '@/components/orders/OrderRequestField'
import { toast } from '@/components/ui/AppToaster'
import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import type { MemberCoupon, MemberPersonalInfo } from '@/domains/member'
import { useMyDeliveryAddresses } from '@/domains/member/member.hook'
import { getOrderErrorMessage, type OrderMethodType } from '@/domains/order'
import type { PaymentMethod } from '@/domains/payment'
import { Shop } from '@/domains/shop'
import { useShopDeliveryTip } from '@/domains/shop/shop.hook'
import { useCartInfo } from '@/hooks/useCartInfo'
import { useTossPayments } from '@/hooks/useTossPayments'
import { formatNumber } from '@/lib/number'
import { PAGE_PATHS } from '@/lib/paths'
import { calculateMinOrderShortfall, calculatePaymentSummary } from '@/lib/paymentCalculation'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import CustomerInfoSection from './CustomerInfoSection'
import DeliveryAddressSection from './DeliveryAddressSection'
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
  const [selectedDeliveryAddressId, setSelectedDeliveryAddressId] = useState<number | null>(null)

  const isDeliveryOrder = orderMethod === 'DELIVERY'

  const { deliveryAddresses, isLoading: isDeliveryAddressesLoading } = useMyDeliveryAddresses({
    enabled: isDeliveryOrder,
  })

  // 기본 배송지가 있으면 초기 선택. 없으면 첫 번째 주소를 선택한다.
  useEffect(() => {
    if (!isDeliveryOrder || selectedDeliveryAddressId !== null || deliveryAddresses.length === 0) {
      return
    }
    const defaultAddress = deliveryAddresses.find((address) => address.isDefault)
    setSelectedDeliveryAddressId((defaultAddress ?? deliveryAddresses[0]).id)
  }, [isDeliveryOrder, selectedDeliveryAddressId, deliveryAddresses])

  const { tossPayment } = useTossPayments()

  // 배달팁 확정 조회. 주소가 바뀌거나 상품 할인 후 금액이 바뀌면 queryKey가 달라져 자동 재조회된다.
  // 배달 외 주문 방법은 조회하지 않고 배달팁 0으로 처리한다.
  const productPaymentAmount = totalProductAmount - totalProductDiscount
  const { deliveryTip: deliveryTipQuote, refetch: refetchDeliveryTip } = useShopDeliveryTip(
    shopId,
    {
      enabled: isDeliveryOrder && selectedDeliveryAddressId !== null,
      deliveryAddressId: selectedDeliveryAddressId ?? undefined,
      orderAmount: productPaymentAmount,
      orderMethod,
    },
  )

  const deliveryTipAmount = isDeliveryOrder ? (deliveryTipQuote?.deliveryTip ?? 0) : 0

  const { totalDiscountAmount, couponDiscount, pointsUsed, deliveryTip, paymentAmount } =
    calculatePaymentSummary(
      totalProductAmount,
      totalProductDiscount,
      deliveryTipAmount,
      selectedCoupon,
      pointInput,
    )

  const handlePayment = async () => {
    // 장바구니에서 이미 차단하지만, 링크 직접 진입·탭 방치 후 재시도를 대비해 결제 직전에도 확인한다.
    const minOrderShortfall = calculateMinOrderShortfall(
      totalProductAmount - totalProductDiscount,
      shop.minOrderAmount,
      orderMethod,
    )
    if (minOrderShortfall > 0) {
      toast(`최소주문금액까지 ${formatNumber(minOrderShortfall)}원 부족합니다.`)
      return
    }

    if (!agreedToTerms) {
      toast('약관에 동의해 주세요.')
      return
    }

    if (!selectedPaymentMethod) {
      toast('결제 수단을 선택해 주세요.')
      return
    }

    if (isDeliveryOrder && selectedDeliveryAddressId === null) {
      toast('배달 주소를 입력해 주세요.')
      return
    }

    // 서버는 주문 접수 시점에 배달팁을 다시 계산한다. 주문서 진입 후 시간별 배달팁 구간을 넘겼으면
    // 표시값과 어긋나 주문이 거절되므로, 결제 직전에 재견적을 받아 최신 값으로 주문한다.
    let confirmedDeliveryTipAmount = deliveryTipAmount
    if (isDeliveryOrder) {
      const { data: requotedDeliveryTip } = await refetchDeliveryTip()
      const requotedAmount = requotedDeliveryTip?.deliveryTip ?? null

      if (requotedAmount === null) {
        toast('배달팁을 확인할 수 없습니다. 배달 주소를 다시 확인해 주세요.')
        return
      }

      if (requotedAmount !== confirmedDeliveryTipAmount) {
        toast(getOrderErrorMessage('ORDER_DELIVERY_TIP_AMOUNT_MISMATCH') ?? '')
        return
      }

      confirmedDeliveryTipAmount = requotedAmount
    }

    const confirmedPaymentSummary = calculatePaymentSummary(
      totalProductAmount,
      totalProductDiscount,
      confirmedDeliveryTipAmount,
      selectedCoupon,
      pointInput,
    )

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
      usePoint: confirmedPaymentSummary.pointsUsed,
      totalProductAmount,
      totalDiscountAmount: confirmedPaymentSummary.totalDiscountAmount,
      productDiscountAmount: totalProductDiscount,
      couponDiscountAmount: confirmedPaymentSummary.couponDiscount,
      finalAmount: confirmedPaymentSummary.paymentAmount,
      request: trimmedRequest,
      // 좌표는 보내지 않는다. 서버가 이 id로 저장된 주소에서만 좌표를 읽는다.
      deliveryAddressId: isDeliveryOrder ? selectedDeliveryAddressId : null,
      deliveryTipAmount: confirmedDeliveryTipAmount,
    })

    if (orderResult.error) {
      // 배달팁 불일치는 토스트만으로 끝내지 않고 재견적을 받아 표시 금액을 갱신한다.
      if (orderResult.errorCode === 'ORDER_DELIVERY_TIP_AMOUNT_MISMATCH') {
        await refetchDeliveryTip()
      }

      toast(orderResult.message ?? getOrderErrorMessage(orderResult.errorCode) ?? orderResult.error)
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
          value: confirmedPaymentSummary.paymentAmount,
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
        {/* 포장 등 배달 외 주문 방법에는 배달 주소가 필요하지 않으므로 섹션 자체를 렌더하지 않는다 */}
        {isDeliveryOrder && (
          <BorderedSection>
            <DeliveryAddressSection
              deliveryAddresses={deliveryAddresses}
              isLoading={isDeliveryAddressesLoading}
              selectedDeliveryAddressId={selectedDeliveryAddressId}
              onDeliveryAddressSelect={setSelectedDeliveryAddressId}
            />
          </BorderedSection>
        )}
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
            deliveryTip={deliveryTip}
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
