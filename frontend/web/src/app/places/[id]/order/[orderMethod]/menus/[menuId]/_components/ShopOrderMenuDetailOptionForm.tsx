'use client'

import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import ConfirmModal from '@/components/ui/ConfirmModal'
import SectionStack from '@/components/ui/SectionStack'
import StickyFooter from '@/components/ui/StickyFooter'
import type { ProductOptionGroup, ProductPrice } from '@/domains/product'
import { useCartAction } from '@/hooks/useCartAction'
import { useProductOptionSelection } from '@/hooks/useProductOptionSelection'
import { useSearchParams } from 'next/navigation'
import { useState } from 'react'
import ShopOrderMenuDetailOptionList from './ShopOrderMenuDetailOptionList'
import ShopOrderMenuDetailPriceSelect from './ShopOrderMenuDetailPriceSelect'

interface Props {
  productId: number
  shopId: number
  optionGroups: ProductOptionGroup[]
  /**
   * 가격 행 목록.
   *
   * 2개 이상이면 손님이 주문 전 하나를 골라야 한다. 1개(또는 미지정)면 고를 것이 없어
   * 선택 UI 를 렌더하지 않고 `priceId` 도 보내지 않는다 — 서버가 `sort=0` 행으로 해석한다.
   */
  prices?: ProductPrice[]
}

export default function ShopOrderMenuDetailOptionForm({
  productId,
  shopId,
  optionGroups,
  prices,
}: Props) {
  const searchParams = useSearchParams()
  const isOptionsTab = (searchParams.get('tab') ?? 'options') === 'options'

  const {
    options,
    handleRadioSelect,
    handleCheckboxToggle,
    getOptionsData,
    normalOptionGroups,
    cupDepositOptionGroups,
  } = useProductOptionSelection(optionGroups)

  /**
   * 가격 선택.
   *
   * 기본값을 첫 행으로 미리 채우지 않는다 — 손님이 고르지 않았는데 골랐다고 취급하면, 의도와
   * 다른 가격으로 담긴 것을 알아채지 못한다. 미선택이면 담기 버튼이 막힌다.
   */
  const priceSelectionRequired = prices != null && prices.length > 1
  const [selectedPriceId, setSelectedPriceId] = useState<number | null>(null)

  const { showShopChangeModal, handleAddToCart, handleConfirmShopChange, onCancelShopChange } =
    useCartAction({
      productId,
      shopId,
      optionGroups,
      options,
      getOptionsData,
      priceId: selectedPriceId ?? undefined,
      priceSelectionRequired,
    })

  return (
    <>
      <SectionStack>
        {/* 가격은 옵션보다 먼저 고른다 — 옵션 추가금의 기준이 되는 값이다 */}
        {priceSelectionRequired && (
          <ShopOrderMenuDetailPriceSelect
            prices={prices}
            selectedPriceId={selectedPriceId}
            onSelect={setSelectedPriceId}
          />
        )}
        <ShopOrderMenuDetailOptionList
          normalOptionGroups={normalOptionGroups}
          cupDepositOptionGroups={cupDepositOptionGroups}
          options={options}
          onRadioSelect={handleRadioSelect}
          onCheckboxToggle={handleCheckboxToggle}
        />
      </SectionStack>
      {isOptionsTab && (
        <StickyFooter>
          <div className="px-[15px] py-2.5 bg-[#f9f9f9]">
            {/* 가격 미선택이면 담을 수 없다 — 어느 가격으로 결제할지 정해지지 않은 상태다 */}
            <AppPrimaryButton
              onClick={handleAddToCart}
              disabled={priceSelectionRequired && selectedPriceId === null}
            >
              장바구니 담기
            </AppPrimaryButton>
          </div>
        </StickyFooter>
      )}
      <ConfirmModal
        open={showShopChangeModal}
        description="가게를 변경하실 경우 장바구니에 담은 메뉴가 삭제됩니다."
        onConfirm={handleConfirmShopChange}
        onCancel={onCancelShopChange}
      />
    </>
  )
}
