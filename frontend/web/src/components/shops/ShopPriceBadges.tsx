import { SHOP_PRICE_BADGE_COPY } from '@/domains/product'
import type { ShopPriceBadges as ShopPriceBadgesData } from '@/domains/shop'
import { cn } from '@/lib/utils'
import * as React from 'react'

interface Props extends React.HTMLAttributes<HTMLDivElement> {
  badges: ShopPriceBadgesData
}

/**
 * 가게 가격 뱃지.
 *
 * "매장과 같은 가격"·"매장가격 픽업" 두 종을 조건에 따라 노출한다.
 *
 * **노출 조건을 화면이 계산하지 않는다.** 서버가 전체 메뉴 대비 매장가·픽업가 설정 비율과
 * 픽업가 설정 시점(익일 노출 규정)까지 보고 판정한 boolean 을 그대로 읽는다 — 화면이 가격을
 * 비교해 판정하면 서버 규정과 어긋나 사실과 다른 뱃지를 붙이게 된다.
 *
 * 둘 다 false 면 아무것도 렌더하지 않는다(빈 여백을 남기지 않기 위해 `null` 반환).
 */
export default function ShopPriceBadges({ badges, className, ...props }: Props) {
  const labels = [
    badges.sameAsStorePrice ? SHOP_PRICE_BADGE_COPY.SAME_AS_STORE_PRICE : null,
    badges.storePricePickup ? SHOP_PRICE_BADGE_COPY.STORE_PRICE_PICKUP : null,
  ].filter((label) => label !== null)

  if (labels.length === 0) return null

  return (
    <div className={cn('flex flex-wrap items-center gap-[5px]', className)} {...props}>
      {labels.map((label) => (
        <span
          key={label}
          className="shrink-0 px-[6px] py-[3px] bg-main text-[10px] leading-[10px] text-white rounded-[3px]"
        >
          {label}
        </span>
      ))}
    </div>
  )
}
