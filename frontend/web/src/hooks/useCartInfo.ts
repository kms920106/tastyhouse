'use client'

import { getProductsBatch } from '@/actions/product'
import { toast } from '@/components/ui/AppToaster'
import type { OrderProduct, OrderProductOption } from '@/domains/order'
import type { ProductBatchItemRequest, ProductBatchItemResponse } from '@/domains/product'
import type { CartProduct, CartSelectedOption } from '@/lib/cart'
import { getCartData, getCartProductTypeCount } from '@/lib/cart'
import {
  calculateTotalProductAmount,
  calculateTotalProductDiscount,
  calculateTotalProductPaymentAmount,
} from '@/lib/paymentCalculation'
import { useCallback, useEffect, useState } from 'react'

export interface CartInfo {
  items: OrderProduct[]
  firstProductName: string
  totalItemCount: number
  totalProductAmount: number
  totalProductDiscount: number
  totalProductPaymentAmount: number
  isLoading: boolean
}

/**
 * 장바구니 상품들을 배치 조회 요청 항목((productId, optionId) 조합)으로 변환합니다.
 * 옵션이 없는 상품은 optionId=null 한 건으로 보냅니다.
 */
function toBatchItems(products: CartProduct[]): ProductBatchItemRequest[] {
  return products.flatMap<ProductBatchItemRequest>((product) =>
    product.options.length === 0
      ? [{ productId: product.productId, optionId: null }]
      : product.options.map((option) => ({
          productId: product.productId,
          optionId: option.optionId,
        })),
  )
}

/**
 * 배치 응답의 옵션 정보를 장바구니가 보관한 옵션(groupId)과 합쳐 주문 옵션으로 변환합니다.
 * 삭제됐거나 상품 소속이 아닌 옵션은 응답에서 조용히 빠지므로(가이드 4번),
 * 매칭되지 않는 옵션은 제외합니다.
 */
function resolveOptionDetails(
  batchProduct: ProductBatchItemResponse,
  selectedOptions: CartSelectedOption[],
): OrderProductOption[] {
  return selectedOptions
    .map((selected) => {
      const option = batchProduct.options.find((o) => o.id === selected.optionId)
      if (!option) return null
      return {
        groupId: selected.groupId,
        groupName: '',
        optionId: option.id,
        optionName: option.name,
        additionalPrice: option.price,
      }
    })
    .filter((option): option is OrderProductOption => option !== null)
}

/**
 * 배치 응답 상품과 선택 옵션으로 단일 장바구니 항목의 표시 가격을 계산합니다.
 * 표시가 = discountPrice ?? originalPrice, 할인액 = originalPrice - 표시가, 옵션 추가금 합산.
 */
function calculateItemPrice(
  options: OrderProductOption[],
  originalPrice: number,
  discountPrice: number | null,
): { salePrice: number; originalPrice: number; discountPrice: number } {
  const basePrice = discountPrice ?? originalPrice
  const discount = originalPrice - basePrice
  const optionAdditionalPrice = options.reduce((sum, option) => sum + option.additionalPrice, 0)

  return {
    salePrice: basePrice + optionAdditionalPrice,
    originalPrice: originalPrice + optionAdditionalPrice,
    discountPrice: discount,
  }
}

/**
 * 배치 응답 상품과 장바구니 항목을 합쳐 주문 상품으로 변환합니다.
 * available=false(판매 종료/삭제/미존재) 상품은 가격 0, 옵션 빈 배열로 남겨
 * 호출부가 "판매 종료" 안내 + 결제 제외 처리를 할 수 있게 합니다.
 */
function toOrderProduct(
  cartProduct: CartProduct,
  batchProduct: ProductBatchItemResponse,
): OrderProduct {
  if (!batchProduct.available) {
    return {
      productId: cartProduct.productId,
      optionKey: cartProduct.optionKey,
      name: batchProduct.name ?? '',
      imageUrl: '',
      quantity: cartProduct.quantity,
      salePrice: 0,
      originalPrice: 0,
      discountPrice: 0,
      options: [],
      available: false,
    }
  }

  const options = resolveOptionDetails(batchProduct, cartProduct.options)
  const { salePrice, originalPrice, discountPrice } = calculateItemPrice(
    options,
    batchProduct.originalPrice ?? 0,
    batchProduct.discountPrice,
  )

  return {
    productId: cartProduct.productId,
    optionKey: cartProduct.optionKey,
    name: batchProduct.name ?? '',
    imageUrl: batchProduct.imageUrl ?? '',
    quantity: cartProduct.quantity,
    salePrice,
    originalPrice,
    discountPrice,
    options,
    available: true,
  }
}

interface CartState {
  items: OrderProduct[]
  firstProductName: string
  totalItemCount: number
  isLoading: boolean
}

const initialCartState: CartState = {
  items: [],
  firstProductName: '',
  totalItemCount: 0,
  isLoading: true,
}

export function useCartInfo(): CartInfo {
  const [state, setState] = useState<CartState>(initialCartState)

  const loadCartInfo = useCallback(async () => {
    const cart = getCartData()
    if (!cart || cart.products.length === 0) {
      setState({ ...initialCartState, isLoading: false })
      return
    }

    const { data, error } = await getProductsBatch(toBatchItems(cart.products))
    if (error || !data) {
      toast('상품 정보를 불러오지 못했습니다.')
      setState({ ...initialCartState, isLoading: false })
      return
    }

    // 응답은 productId 최초 등장 순서로 그룹핑되어 내려오므로 productId로 매핑한다.
    const batchProductMap = new Map(data.products.map((product) => [product.id, product]))

    const orderProducts: OrderProduct[] = cart.products
      .map((cartProduct) => {
        const batchProduct = batchProductMap.get(cartProduct.productId)
        if (!batchProduct) return null
        return toOrderProduct(cartProduct, batchProduct)
      })
      .filter((item): item is OrderProduct => item !== null)

    setState({
      items: orderProducts,
      firstProductName: orderProducts.find((item) => item.available)?.name ?? '',
      totalItemCount: getCartProductTypeCount(),
      isLoading: false,
    })
  }, [])

  useEffect(() => {
    loadCartInfo()
  }, [loadCartInfo])

  const totalProductAmount = calculateTotalProductAmount(state.items)
  const totalProductDiscount = calculateTotalProductDiscount(state.items)
  const totalProductPaymentAmount = calculateTotalProductPaymentAmount(state.items)

  return {
    ...state,
    totalProductAmount,
    totalProductDiscount,
    totalProductPaymentAmount,
  }
}
