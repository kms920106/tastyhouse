import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import type { OrderMethodType } from '@/domains/order'
import { formatNumber } from '@/lib/number'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'

interface Props {
  shopId: number
  orderMethod: OrderMethodType
  /** 가게 최소주문금액까지 부족한 금액. 0이면 주문 가능 */
  minOrderShortfall: number
  /** 선택된 수령 예약 시각(슬롯 startAt). 미선택(즉시 주문)이면 null */
  scheduledAt: string | null
}

/**
 * 장바구니 주문하기 CTA.
 *
 * 가게 최소주문금액이 부족하면 링크 없이 비활성 버튼을 렌더하고 부족 금액을 안내합니다.
 */
export default function ShopOrderCartLinkButton({
  shopId,
  orderMethod,
  minOrderShortfall,
  scheduledAt,
}: Props) {
  if (minOrderShortfall > 0) {
    return (
      <div className="flex flex-col gap-2">
        <p className="text-xs leading-[12px] text-center text-[#aaaaaa]">
          {formatNumber(minOrderShortfall)}원 더 담으면 주문할 수 있어요
        </p>
        <AppPrimaryButton disabled>주문하기</AppPrimaryButton>
      </div>
    )
  }

  return (
    <Link href={PAGE_PATHS.ORDER_CHECKOUT(shopId, orderMethod, scheduledAt ?? undefined)}>
      <AppPrimaryButton>주문하기</AppPrimaryButton>
    </Link>
  )
}
