'use client'

import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import ConfirmModal from '@/components/ui/ConfirmModal'
import SectionStack from '@/components/ui/SectionStack'
import StickyFooter from '@/components/ui/StickyFooter'
import type { ProductOptionGroup } from '@/domains/product'
import { useCartAction } from '@/hooks/useCartAction'
import { useProductOptionSelection } from '@/hooks/useProductOptionSelection'
import { useSearchParams } from 'next/navigation'
import ShopOrderMenuDetailOptionList from './ShopOrderMenuDetailOptionList'

interface Props {
  productId: number
  shopId: number
  optionGroups: ProductOptionGroup[]
}

export default function ShopOrderMenuDetailOptionForm({ productId, shopId, optionGroups }: Props) {
  const searchParams = useSearchParams()
  const isOptionsTab = (searchParams.get('tab') ?? 'options') === 'options'

  const { options, handleRadioSelect, handleCheckboxToggle, getOptionsData } =
    useProductOptionSelection(optionGroups)

  const { showShopChangeModal, handleAddToCart, handleConfirmShopChange, onCancelShopChange } =
    useCartAction({ productId, shopId, optionGroups, options, getOptionsData })

  return (
    <>
      <SectionStack>
        <ShopOrderMenuDetailOptionList
          optionGroups={optionGroups}
          options={options}
          onRadioSelect={handleRadioSelect}
          onCheckboxToggle={handleCheckboxToggle}
        />
      </SectionStack>
      {isOptionsTab && (
        <StickyFooter>
          <div className="px-[15px] py-2.5 bg-[#f9f9f9]">
            <AppPrimaryButton onClick={handleAddToCart}>장바구니 담기</AppPrimaryButton>
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
