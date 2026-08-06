'use client'

import {
  Modal,
  ModalContentWrapper,
  ModalDescription,
  ModalFooter,
  ModalHeader,
  ModalTitle,
} from '@/components/ui/Modal'
import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import type { ShopDeliveryTip } from '@/domains/shop'
import { useShopDeliveryTip } from '@/domains/shop/shop.hook'
import { formatNumber } from '@/lib/number'

interface Props {
  open: boolean
  onOpenChange: (open: boolean) => void
  shopId: number
  /** 확정 계산용 배달 주소 id. 없으면 범위 모드로 조회한다 */
  deliveryAddressId?: number
  /** 상품 할인 후 금액. 구간 확정용 */
  orderAmount?: number
  /** 주문 방법. 기본 DELIVERY */
  orderMethod?: string
}

/**
 * 배달팁 상세 안내 팝업.
 *
 * 가게 상세에서는 파라미터 없이(범위 모드), 장바구니·결제에서는 주소·주문금액을 실어(확정 모드)
 * 같은 컴포넌트를 재사용합니다. 열릴 때만 조회해 상세 초기 렌더 비용을 늘리지 않습니다.
 */
export default function ShopDeliveryTipDialog({
  open,
  onOpenChange,
  shopId,
  deliveryAddressId,
  orderAmount,
  orderMethod,
}: Props) {
  const { deliveryTip, isLoading, isError } = useShopDeliveryTip(shopId, {
    enabled: open,
    deliveryAddressId,
    orderAmount,
    orderMethod,
  })

  return (
    <Modal open={open} onOpenChange={onOpenChange}>
      <ModalHeader>
        <ModalTitle className="pt-10 pb-[30px] text-base leading-[16px] text-center font-bold">
          배달팁 안내
        </ModalTitle>
      </ModalHeader>
      <ModalDescription className="sr-only">배달팁 안내</ModalDescription>
      <ModalContentWrapper className="px-5 pb-[30px] max-h-[60vh] overflow-y-auto">
        {isLoading && (
          <p className="text-sm leading-[14px] text-[#666666] text-center">불러오는 중입니다.</p>
        )}
        {!isLoading && (isError || !deliveryTip) && (
          <p className="text-sm leading-[14px] text-[#666666] text-center">
            {COMMON_ERROR_MESSAGES.API_FETCH_ERROR}
          </p>
        )}
        {!isLoading && !isError && deliveryTip && <DeliveryTipDetail deliveryTip={deliveryTip} />}
      </ModalContentWrapper>
      <ModalFooter>
        <AppPrimaryButton onClick={() => onOpenChange(false)}>확인</AppPrimaryButton>
      </ModalFooter>
    </Modal>
  )
}

function DeliveryTipDetail({ deliveryTip }: { deliveryTip: ShopDeliveryTip }) {
  const { breakdown, tiers, extraTipType, distance, regions, schedules, holidayTipAmount } =
    deliveryTip

  const hasAnySetting =
    tiers.length > 0 ||
    extraTipType !== 'NONE' ||
    schedules.length > 0 ||
    holidayTipAmount > 0 ||
    breakdown.length > 0

  if (!hasAnySetting) {
    return <p className="text-sm leading-[14px] text-[#666666] text-center">배달팁이 없습니다.</p>
  }

  return (
    <div className="flex flex-col gap-[30px]">
      {breakdown.length > 0 && (
        <DeliveryTipSection title="내 배달팁 내역">
          {breakdown.map(({ label, amount }, index) => (
            <DeliveryTipRow key={`${label}-${index}`} label={label} amount={amount} />
          ))}
          <div className="pt-2.5 flex justify-between">
            <span className="text-sm leading-[14px]">합계</span>
            <span className="text-sm leading-[14px] text-main">
              {formatNumber(deliveryTip.deliveryTip ?? 0)}원
            </span>
          </div>
        </DeliveryTipSection>
      )}
      {tiers.length > 0 && (
        <DeliveryTipSection title="주문금액별 배달팁">
          {tiers.map(({ minOrderAmount, tipAmount }) => (
            <DeliveryTipRow
              key={minOrderAmount}
              label={`${formatNumber(minOrderAmount)}원 이상 주문 시`}
              amount={tipAmount}
            />
          ))}
        </DeliveryTipSection>
      )}
      {extraTipType === 'DISTANCE' && distance && (
        <DeliveryTipSection title="거리별 추가 배달팁">
          <p className="text-xs leading-[16px] text-[#666666]">
            기본 배달거리 {formatNumber(distance.baseDistanceMeters)}m까지는 추가 배달팁이 없고,
            이후
            {distance.surchargeUnit === 'PER_100M' ? ' 100m' : ' 500m'}마다{' '}
            {formatNumber(distance.surchargeAmount)}원이 더해집니다.
          </p>
        </DeliveryTipSection>
      )}
      {extraTipType === 'REGION' && regions.length > 0 && (
        <DeliveryTipSection title="지역별 추가 배달팁">
          {regions.map(({ regionName, tipAmount }) => (
            <DeliveryTipRow key={regionName} label={regionName} amount={tipAmount} />
          ))}
        </DeliveryTipSection>
      )}
      {schedules.length > 0 && (
        <DeliveryTipSection title="시간별 추가 배달팁">
          {schedules.map(({ dayType, dayTypeDescription, startTime, endTime, tipAmount }) => (
            <DeliveryTipRow
              key={`${dayType}-${startTime}-${endTime}`}
              label={`${dayTypeDescription} ${startTime}~${endTime}`}
              amount={tipAmount}
            />
          ))}
        </DeliveryTipSection>
      )}
      {holidayTipAmount > 0 && (
        <DeliveryTipSection title="공휴일 추가 배달팁">
          <DeliveryTipRow label="법정 공휴일" amount={holidayTipAmount} />
          <p className="text-xs leading-[16px] text-[#aaaaaa]">
            일요일에는 적용되지 않고 시간별 배달팁이 적용됩니다.
          </p>
        </DeliveryTipSection>
      )}
    </div>
  )
}

function DeliveryTipSection({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section className="flex flex-col gap-2.5">
      <h3 className="text-sm leading-[14px] font-bold">{title}</h3>
      {children}
    </section>
  )
}

function DeliveryTipRow({ label, amount }: { label: string; amount: number }) {
  return (
    <div className="flex justify-between gap-3">
      <span className="text-xs leading-[16px] text-[#666666]">{label}</span>
      <span className="shrink-0 text-xs leading-[16px]">{formatNumber(amount)}원</span>
    </div>
  )
}
