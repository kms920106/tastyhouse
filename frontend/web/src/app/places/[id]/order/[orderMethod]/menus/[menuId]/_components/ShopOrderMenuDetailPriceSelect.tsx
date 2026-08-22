'use client'

import BorderedSection from '@/components/ui/BorderedSection'
import { PRODUCT_PRICE_COPY, type ProductPrice } from '@/domains/product'
import { formatNumber } from '@/lib/number'
import { cn } from '@/lib/utils'
import { RiRadioButtonFill } from 'react-icons/ri'

interface Props {
  prices: ProductPrice[]
  /** 아직 고르지 않았으면 null */
  selectedPriceId: number | null
  onSelect: (priceId: number) => void
}

/**
 * 가격 선택 (가격명이 여러 개인 메뉴).
 *
 * 옵션 선택 화면 **최상단**에 둔다 — 어느 가격을 골랐는지가 옵션 추가금의 기준이 되므로,
 * 옵션보다 먼저 결정해야 손님이 최종 금액을 이해할 수 있다.
 *
 * 표시하는 `price` 는 **서버가 주문유형에 따라 이미 해석한 값**이다. 화면이 배달가·매장가·픽업가
 * 중 무엇을 쓸지 고르지 않는다 — 주문 접수 시 서버가 클라이언트 금액과 자기 계산을 대조하므로
 * 화면이 다르게 해석하면 `ORDER_PRODUCT_AMOUNT_MISMATCH` 로 **주문이 거절된다.**
 *
 * 가격이 1개인 메뉴는 고를 것이 없으므로 호출부가 이 컴포넌트를 렌더하지 않는다.
 */
export default function ShopOrderMenuDetailPriceSelect({
  prices,
  selectedPriceId,
  onSelect,
}: Props) {
  return (
    <BorderedSection>
      <div className="px-4 py-5">
        <h3 className="text-base leading-[16px] font-bold">
          {PRODUCT_PRICE_COPY.SELECT_SECTION_TITLE}
          {/* 필수 표시는 옵션그룹의 required 와 같은 기호를 쓴다 — 손님에게 같은 의미다 */}
          <span className="text-main ml-1">*</span>
        </h3>
        <div className="flex flex-col gap-[15px] mt-5">
          {prices.map((row) => {
            const isSelected = row.priceId === selectedPriceId
            return (
              <button
                key={row.priceId}
                type="button"
                onClick={() => onSelect(row.priceId)}
                className="flex items-center gap-2.5 w-full text-left cursor-pointer"
              >
                <RiRadioButtonFill
                  size={28}
                  className={cn('flex-shrink-0', isSelected ? 'text-main' : 'text-[#dddddd]')}
                />
                <span className="flex-1 text-sm leading-[14px]">{row.priceName}</span>
                <span className="text-sm leading-[14px]">{formatNumber(row.price)}원</span>
              </button>
            )
          })}
        </div>
      </div>
    </BorderedSection>
  )
}
