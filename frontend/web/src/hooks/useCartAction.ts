'use client'

import { toast } from '@/components/ui/AppToaster'
import type { ProductOptionGroup } from '@/domains/product'
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
}

export function useCartAction({
  productId,
  shopId,
  optionGroups,
  options,
  getOptionsData,
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
      const cartItem = { productId, options: getOptionsData() }
      if (replace) {
        replaceCartAndAdd(shopId, cartItem)
      } else {
        addToCart(shopId, cartItem)
      }
      window.dispatchEvent(new Event('cartUpdated'))
      toast('메뉴를 장바구니에 담았습니다.')
      router.back()
    },
    [productId, shopId, getOptionsData, router],
  )

  const handleAddToCart = useCallback(() => {
    if (!validateRequiredOptions()) return
    const currentCartShopId = getCartShopId()
    if (currentCartShopId === null || currentCartShopId === shopId) {
      executeAddToCart()
      return
    }
    setShowShopChangeModal(true)
  }, [shopId, validateRequiredOptions, executeAddToCart])

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
