'use client'

import { createOrder } from '@/actions/order'
import { completeOnSitePayment, createPayment, getPaymentByOrderId } from '@/actions/payment'
import OrderRequestField from '@/components/orders/OrderRequestField'
import ScheduledOrderSheet from '@/components/shops/ScheduledOrderSheet'
import { toast } from '@/components/ui/AppToaster'
import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import type { MemberCoupon, MemberPersonalInfo } from '@/domains/member'
import { useMyDeliveryAddresses } from '@/domains/member/member.hook'
import { getOrderErrorMessage, type OrderMethodType } from '@/domains/order'
import type { PaymentMethod } from '@/domains/payment'
import { Shop } from '@/domains/shop'
import type { ScheduledOrderSlot } from '@/domains/shop'
import { useScheduledOrderSlots, useShopDeliveryTip } from '@/domains/shop/shop.hook'
import { useCartInfo } from '@/hooks/useCartInfo'
import { useTossPayments } from '@/hooks/useTossPayments'
import { formatNumber } from '@/lib/number'
import { PAGE_PATHS } from '@/lib/paths'
import {
  calculateCupDepositAmount,
  calculateMinOrderShortfall,
  calculatePaymentSummary,
  calculatePersonalCupDiscountAmount,
} from '@/lib/paymentCalculation'
import { useRouter } from 'next/navigation'
import { useEffect, useRef, useState } from 'react'
import CustomerInfoSection from './CustomerInfoSection'
import DeliveryAddressSection from './DeliveryAddressSection'
import DiscountApplicationSection from './DiscountApplicationSection'
import OrderInfoSection from './OrderInfoSection'
import ScheduledOrderSection from './ScheduledOrderSection'
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
  /** 가게의 예약주문 운영 여부. false면 수령시간 섹션 자체를 렌더하지 않는다 */
  scheduledOrderEnabled: boolean
  /** 장바구니에서 URL로 넘어온 수령 예약 시각. 슬롯 재조회로 유효성을 확인한다 */
  initialScheduledAt: string | null
}

const MAX_REQUEST_LENGTH = 200

/** 수령시간을 예약할 수 있는 주문 방법. 테이블·매장예약은 수령 시각 개념이 없다. */
const SCHEDULABLE_ORDER_METHODS: OrderMethodType[] = ['DELIVERY', 'TAKEOUT']

export default function ShopOrderCheckoutContentClient({
  shop,
  member,
  availableCoupons,
  usablePoints,
  orderMethod,
  scheduledOrderEnabled,
  initialScheduledAt,
}: Props) {
  const router = useRouter()

  const { id: shopId, name: shopName } = shop
  const { fullName, phoneNumber, email } = member

  const {
    items,
    firstProductName,
    totalItemCount,
    totalProductAmount,
    totalProductDiscount,
    refreshCartInfo,
  } = useCartInfo(orderMethod) // 주문유형 기준으로 가격 행(배달가/픽업가)이 해석돼 내려온다

  const [selectedCoupon, setSelectedCoupon] = useState<MemberCoupon | null>(null)
  const [pointInput, setPointInput] = useState('')
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState<PaymentMethod | null>(null)
  const [agreedToTerms, setAgreedToTerms] = useState(false)
  const [request, setRequest] = useState('')
  const [selectedDeliveryAddressId, setSelectedDeliveryAddressId] = useState<number | null>(null)

  const [scheduledSlot, setScheduledSlot] = useState<ScheduledOrderSlot | null>(null)
  const [isScheduledOrderSheetOpen, setIsScheduledOrderSheetOpen] = useState(false)

  const isDeliveryOrder = orderMethod === 'DELIVERY'
  const canScheduleOrder = scheduledOrderEnabled && SCHEDULABLE_ORDER_METHODS.includes(orderMethod)

  // 슬롯은 시간이 지나면 사라지므로 캐시하지 않는다. 결제 직전 재검증도 이 쿼리를 refetch한다.
  const {
    availability: scheduledOrderAvailability,
    isLoading: isScheduledOrderSlotsLoading,
    refetch: refetchScheduledOrderSlots,
  } = useScheduledOrderSlots(shopId, {
    orderMethod,
    enabled: canScheduleOrder,
  })

  // URL로 넘어온 시각을 그대로 믿지 않는다. 슬롯 목록에 없으면(경계 경과·URL 조작) 선택을 해제한다.
  // 조회 결과가 도착한 뒤 한 번만 판정하면 되므로 처리 여부를 ref로 기억한다.
  const hasResolvedInitialScheduledAt = useRef(false)
  useEffect(() => {
    if (!canScheduleOrder || isScheduledOrderSlotsLoading || !scheduledOrderAvailability) return
    if (hasResolvedInitialScheduledAt.current) return
    hasResolvedInitialScheduledAt.current = true

    if (initialScheduledAt === null) return

    const matched = scheduledOrderAvailability.slots.find(
      (slot) => slot.startAt === initialScheduledAt,
    )
    if (matched) {
      setScheduledSlot(matched)
    } else {
      toast('선택한 수령시간을 사용할 수 없어 예약 없이 진행합니다. 수령시간을 다시 선택해 주세요.')
    }
  }, [
    canScheduleOrder,
    isScheduledOrderSlotsLoading,
    scheduledOrderAvailability,
    initialScheduledAt,
  ])

  const { deliveryAddresses, isLoading: isDeliveryAddressesLoading } = useMyDeliveryAddresses({
    enabled: isDeliveryOrder,
  })

  // 기본 배송지가 있으면 초기 선택. 없으면 첫 번째 주소를 선택한다.
  useEffect(() => {
    if (!isDeliveryOrder || selectedDeliveryAddressId !== null || deliveryAddresses.length === 0) {
      return
    }
    const defaultAddress = deliveryAddresses.find((address) => address.defaultAddress)
    setSelectedDeliveryAddressId((defaultAddress ?? deliveryAddresses[0]).id)
  }, [isDeliveryOrder, selectedDeliveryAddressId, deliveryAddresses])

  const { tossPayment } = useTossPayments()

  // 일회용컵 보증금은 Σ(옵션.depositAmount) × 수량. totalProductAmount에 합산하지 않는다 —
  // 합산하면 최소주문금액·쿠폰·포인트 기준액까지 오염된다.
  const cupDepositAmount = calculateCupDepositAmount(items)
  // 개인컵 할인은 보증금이 아니라 상품 할인 축이므로 productDiscountAmount에 가산한다.
  // 서버의 최소주문금액 판정 기준액(totalProductAmount - productDiscountAmount)과 맞추기 위해
  // 배달팁 견적·최소주문금액 판정에도 이 값을 사용한다.
  const personalCupDiscountAmount = calculatePersonalCupDiscountAmount(items)
  const totalProductDiscountWithPersonalCup = totalProductDiscount + personalCupDiscountAmount

  // 배달팁 확정 조회. 주소가 바뀌거나 상품 할인 후 금액이 바뀌면 queryKey가 달라져 자동 재조회된다.
  // 배달 외 주문 방법은 조회하지 않고 배달팁 0으로 처리한다. 보증금은 포함하지 않는다.
  const productPaymentAmount = totalProductAmount - totalProductDiscountWithPersonalCup
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
      totalProductDiscountWithPersonalCup,
      deliveryTipAmount,
      selectedCoupon,
      pointInput,
      cupDepositAmount,
    )

  const handlePayment = async () => {
    // 장바구니에서 이미 차단하지만, 링크 직접 진입·탭 방치 후 재시도를 대비해 결제 직전에도 확인한다.
    // 최소주문금액 안내에는 보증금을 포함하지 않는다 — 서버 판정 기준과 어긋나면 혼란이 생긴다.
    const minOrderShortfall = calculateMinOrderShortfall(
      totalProductAmount - totalProductDiscountWithPersonalCup,
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

      // 재견적 금액은 서버가 인정하는 최신 값이므로 그대로 채택해 진행한다.
      // 표시값과 달라졌다면 안내만 하고 결제를 막지 않는다 — 여기서 막으면 시간대 경계를
      // 넘길 때마다 결제가 무한정 반복 거부될 수 있다.
      if (requotedAmount !== confirmedDeliveryTipAmount) {
        toast(`배달팁이 ${formatNumber(requotedAmount)}원으로 변경되어 최신 금액으로 결제합니다.`)
      }

      confirmedDeliveryTipAmount = requotedAmount
    }

    // 예약 시각도 서버가 접수 시점에 슬롯을 다시 계산한다. 다만 배달팁과 달리 최신 값으로 갈아끼울
    // 수 없다 — 슬롯이 사라졌다는 것은 그 시각에 받을 수 없다는 뜻이라 주문 자체가 성립하지 않는다.
    // 그래서 여기서는 결제를 중단하고 시트를 열어 재선택을 유도한다.
    let confirmedScheduledAt: string | null = null
    if (canScheduleOrder && scheduledSlot !== null) {
      const { data: requotedSlots } = await refetchScheduledOrderSlots()
      const stillAvailable = requotedSlots?.slots.some(
        (slot) => slot.startAt === scheduledSlot.startAt,
      )

      if (!stillAvailable) {
        setScheduledSlot(null)
        setIsScheduledOrderSheetOpen(true)
        toast('선택한 수령시간이 마감되었어요. 다시 선택해주세요')
        return
      }

      confirmedScheduledAt = scheduledSlot.startAt
    }

    const confirmedPaymentSummary = calculatePaymentSummary(
      totalProductAmount,
      totalProductDiscountWithPersonalCup,
      confirmedDeliveryTipAmount,
      selectedCoupon,
      pointInput,
      cupDepositAmount,
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
      /*
        가격 행 id 는 예외적으로 함께 보낸다 — 금액이 아니라 "어느 가격 행을 골랐는지"라서
        서버가 DB에서 되찾을 수 없다. 미지정이면 서버가 `sort=0` 행을 쓰므로 가격이 1개인
        메뉴는 보내지 않아도 기존과 동일하게 동작한다.
      */
      priceId: item.priceId,
    }))

    const orderResult = await createOrder({
      shopId,
      orderMethod,
      orderProducts,
      memberCouponId: selectedCoupon?.id ?? null,
      usePoint: confirmedPaymentSummary.pointsUsed,
      totalProductAmount,
      totalDiscountAmount: confirmedPaymentSummary.totalDiscountAmount,
      productDiscountAmount: totalProductDiscountWithPersonalCup,
      couponDiscountAmount: confirmedPaymentSummary.couponDiscount,
      finalAmount: confirmedPaymentSummary.paymentAmount,
      cupDepositAmount: confirmedPaymentSummary.cupDepositAmount,
      request: trimmedRequest,
      // 좌표는 보내지 않는다. 서버가 이 id로 저장된 주소에서만 좌표를 읽는다.
      deliveryAddressId: isDeliveryOrder ? selectedDeliveryAddressId : null,
      deliveryTipAmount: confirmedDeliveryTipAmount,
      scheduledAt: confirmedScheduledAt,
    })

    if (orderResult.error) {
      // 배달팁 불일치는 토스트만으로 끝내지 않고 재견적을 받아 표시 금액을 갱신한다.
      if (orderResult.errorCode === 'ORDER_DELIVERY_TIP_AMOUNT_MISMATCH') {
        await refetchDeliveryTip()
      }

      /*
        금액 불일치는 점주가 그 사이 가격을 바꾼 상황이다. 낡은 금액으로 재시도하면 계속 거절되므로
        메뉴를 재조회해 화면 금액을 갱신한다 — 안내 문구도 "새로고침 후 다시 시도"를 말한다
        (`ORDER_ERROR_MESSAGES.ORDER_PRODUCT_AMOUNT_MISMATCH`).
      */
      if (orderResult.errorCode === 'ORDER_PRODUCT_AMOUNT_MISMATCH') {
        await refreshCartInfo()
      }

      // 예약 관련 거절은 선택을 비우고 슬롯을 다시 받아 재선택할 수 있게 한다.
      if (
        orderResult.errorCode === 'SHOP_SCHEDULED_ORDER_DISABLED' ||
        orderResult.errorCode === 'ORDER_SCHEDULE_METHOD_NOT_SUPPORTED' ||
        orderResult.errorCode === 'ORDER_SCHEDULED_AT_UNAVAILABLE'
      ) {
        setScheduledSlot(null)
        await refetchScheduledOrderSlots()
      }

      // 가게·주문방식 자체가 주문 불가면 이 화면에서 재시도해도 계속 실패한다.
      // 다른 주문방식을 고를 수 있게 주문방식 선택 화면으로 되돌린다.
      if (
        orderResult.errorCode === 'SHOP_NOT_ORDERABLE' ||
        orderResult.errorCode === 'SHOP_ORDER_METHOD_SUSPENDED' ||
        orderResult.errorCode === 'SHOP_ORDER_METHOD_NOT_SUPPORTED'
      ) {
        toast(
          orderResult.message ?? getOrderErrorMessage(orderResult.errorCode) ?? orderResult.error,
        )
        router.push(PAGE_PATHS.ORDER_METHOD(shopId))
        return
      }

      toast(orderResult.message ?? getOrderErrorMessage(orderResult.errorCode) ?? orderResult.error)
      return
    }

    // 서버는 생성된 주문 ID를 스칼라로 내려준다(ApiResponse<Long>). 객체로 감싸 오지 않는다.
    const orderId = orderResult.data
    if (orderId == null) {
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

    // 결제 생성도 주문과 마찬가지로 생성된 결제 ID를 스칼라로 내려준다.
    const paymentId = paymentResult.data
    if (paymentId == null) {
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

      // 결제 생성 응답은 결제 ID뿐이므로, PG 결제창에 넘길 pgOrderId는 주문별 결제 조회로 받는다.
      const paymentDetailResult = await getPaymentByOrderId(orderId)
      const pgOrderId = paymentDetailResult.data?.pgOrderId

      if (!pgOrderId) {
        toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
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
        orderId: pgOrderId,
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
        {/* 배달주소 아래, 요청사항 위에 수령시간을 둔다 — 언제 받을지가 무엇을 요청할지보다 앞선다 */}
        {canScheduleOrder && (
          <BorderedSection>
            <ScheduledOrderSection
              selectedSlot={scheduledSlot}
              onChangeClick={() => setIsScheduledOrderSheetOpen(true)}
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
            totalProductDiscountAmount={totalProductDiscountWithPersonalCup}
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
            totalProductDiscountAmount={totalProductDiscountWithPersonalCup}
            totalDiscountAmount={totalDiscountAmount}
            couponDiscount={couponDiscount}
            pointsUsed={pointsUsed}
            deliveryTip={deliveryTip}
            cupDepositAmount={cupDepositAmount}
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
      {canScheduleOrder && (
        <ScheduledOrderSheet
          open={isScheduledOrderSheetOpen}
          onOpenChange={setIsScheduledOrderSheetOpen}
          shopId={shopId}
          orderMethod={orderMethod}
          selectedStartAt={scheduledSlot?.startAt ?? null}
          onSelect={setScheduledSlot}
        />
      )}
      <PaymentActionBar onPaymentClick={handlePayment} />
    </>
  )
}
