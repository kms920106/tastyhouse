'use client'

import { toast } from '@/components/ui/AppToaster'
import { PRODUCT_PRICE_COPY, type ProductOptionGroup } from '@/domains/product'
import type { CartSelectedOption } from '@/lib/cart'
import { addToCart, getCartShopId, replaceCartAndAdd } from '@/lib/cart'
import { useRouter } from 'next/navigation'
import { useCallback, useState } from 'react'

interface UseCartActionParams {
  productId: number
  shopId: number
  optionGroups: ProductOptionGroup[]
  options: Record<number, number | number[]>
  getOptionsData: () => CartSelectedOption[]
  /**
   * 손님이 고른 가격 행 id. 가격이 1개인 메뉴는 `undefined` 다.
   *
   * 가격이 2개 이상인데 고르지 않았으면 담기를 막는다 — 서버는 미지정을 `sort=0` 행으로
   * 해석하므로, 고르지 않은 채 담기면 손님이 의도하지 않은 가격으로 결제된다.
   */
  priceId?: number
  /** 가격 선택이 필요한 메뉴인지(가격 행 2개 이상) */
  priceSelectionRequired?: boolean
}

export function useCartAction({
  productId,
  shopId,
  optionGroups,
  options,
  getOptionsData,
  priceId,
  priceSelectionRequired = false,
}: UseCartActionParams) {
  const router = useRouter()
  const [showShopChangeModal, setShowShopChangeModal] = useState(false)

  const validateRequiredOptions = useCallback((): boolean => {
    const missingRequired = optionGroups.filter((group) => {
      if (!group.required) return false
      const selected = options[group.id]
      if (group.multipleSelect) return (selected as number[]).length < group.minSelect
      return selected === -1
    })
    if (missingRequired.length > 0) {
      toast(`필수 옵션을 선택해 주세요: ${missingRequired.map((g) => g.name).join(', ')}`)
      return false
    }
    return true
  }, [optionGroups, options])

  const executeAddToCart = useCallback(
    (replace = false) => {
      const cartItem = { productId, options: getOptionsData(), priceId }
      if (replace) {
        replaceCartAndAdd(shopId, cartItem)
      } else {
        addToCart(shopId, cartItem)
      }
      window.dispatchEvent(new Event('cartUpdated'))
      toast('메뉴를 장바구니에 담았습니다.')
      router.back()
    },
    [productId, shopId, getOptionsData, priceId, router],
  )

  const handleAddToCart = useCallback(() => {
    // 가격 선택은 옵션보다 먼저 본다 — 어느 가격인지 모르면 옵션 추가금도 의미가 없다.
    if (priceSelectionRequired && priceId == null) {
      toast(PRODUCT_PRICE_COPY.SELECT_REQUIRED)
      return
    }
    if (!validateRequiredOptions()) return
    const currentCartShopId = getCartShopId()
    if (currentCartShopId === null || currentCartShopId === shopId) {
      executeAddToCart()
      return
    }
    setShowShopChangeModal(true)
  }, [shopId, priceSelectionRequired, priceId, validateRequiredOptions, executeAddToCart])

  const handleConfirmShopChange = useCallback(() => {
    setShowShopChangeModal(false)
    executeAddToCart(true)
  }, [executeAddToCart])

  return {
    showShopChangeModal,
    handleAddToCart,
    handleConfirmShopChange,
    onCancelShopChange: () => setShowShopChangeModal(false),
  }
}
