'use client'

import { getProductsBatch } from '@/actions/product'
import { toast } from '@/components/ui/AppToaster'
import type { OrderMethodType, OrderProduct, OrderProductOption } from '@/domains/order'
import type {
  ProductBatchItemRequest,
  ProductBatchItemResponse,
  ProductPriceResponse,
} from '@/domains/product'
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
  /** 금액 불일치로 주문이 거절됐을 때 메뉴를 재조회한다 */
  refreshCartInfo: () => Promise<void>
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
        cupCount: option.cupCount,
        depositAmount: option.depositAmount,
        personalCupDiscountAmount: option.personalCupDiscountAmount,
      }
    })
    .filter((option): option is OrderProductOption => option !== null)
}

/**
 * 손님이 담을 때 고른 가격 행을 배치 응답에서 되찾습니다.
 *
 * `priceId` 가 없으면(가격이 1개인 메뉴 또는 이 필드가 생기기 전에 담아 둔 항목) 기본 가격 행,
 * 즉 서버가 `sort` 오름차순으로 내려준 첫 행을 씁니다 — 주문 접수 시 서버도 `priceId` 미지정을
 * `sort=0` 행으로 해석하므로(`OrderProductValidationService#resolvePrice`) 표시와 계산이 일치합니다.
 *
 * 가격 행이 없는 메뉴(가격 행 도입 이전 데이터)는 `null` 이며, 호출부가 기존
 * `originalPrice`(= `PRODUCT.original_price`) 경로로 폴백합니다.
 *
 * 담아 둔 `priceId` 가 응답에 없으면(점주가 그 가격 행을 지웠다) 기본 행으로 폴백하지 않고 `null` 을
 * 돌려 기존 경로를 쓰게 합니다 — 다른 가격명의 금액을 슬쩍 보여주면 손님이 고른 것과 다른 금액이
 * 표시되고, 접수 시 서버는 그 `priceId` 를 `PRODUCT_PRICE_NOT_FOUND` 로 거절한다.
 */
function resolveSelectedPrice(
  batchProduct: ProductBatchItemResponse,
  priceId?: number,
): ProductPriceResponse | null {
  const prices = batchProduct.prices ?? []
  if (prices.length === 0) return null
  if (priceId == null) return prices[0]
  return prices.find((price) => price.priceId === priceId) ?? null
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
      priceId: cartProduct.priceId,
      // 판매 종료 항목은 가격 행도 내려오지 않으므로 가격명을 표시하지 않는다.
      priceName: null,
    }
  }

  const options = resolveOptionDetails(batchProduct, cartProduct.options)
  const selectedPrice = resolveSelectedPrice(batchProduct, cartProduct.priceId)
  const { salePrice, originalPrice, discountPrice } = calculateItemPrice(
    options,
    /*
      단가는 손님이 고른 가격 행의 가격이다 — 서버가 주문유형으로 이미 해석해 내려준 값을 그대로 쓴다
      (`ProductPrice#resolvePrice`). 가격 행이 없는 메뉴만 기존 `originalPrice` 로 폴백한다.

      할인가는 가격 행이 아니라 상품 단위 값이다 — 주문 스냅샷도 `discountPrice` 를 상품에서 가져오므로
      (`OrderProductValidationService#validateLine`) 여기서도 같은 축으로 계산해야 금액이 어긋나지 않는다.
    */
    selectedPrice?.price ?? batchProduct.originalPrice ?? 0,
    batchProduct.discountPrice,
  )

  return {
    /*
      `priceId` 는 장바구니가 보관한 값을 그대로 나른다 — 주문 요청에 실려야 서버가 어느 가격으로
      계산할지 알 수 있다.

      가격명은 배치 응답의 가격 행에서 되찾는다. 담을 때 저장하지 않는 이유는 점주가 가격명을 바꾸면
      장바구니의 낡은 이름이 그대로 남기 때문이다 — 주문 시점 박제는 접수 때 서버가 수행한다.
    */
    priceId: cartProduct.priceId,
    priceName: selectedPrice?.priceName ?? null,
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

/**
 * 장바구니 항목의 표시 정보를 배치 조회로 채운다.
 *
 * `orderMethod` 는 가격 행의 채널 가격(배달가/픽업가)을 서버가 해석하는 기준이다. 넘기지 않으면
 * 서버가 `DELIVERY` 로 보므로, 포장 주문 화면은 반드시 자기 주문유형을 넘겨야 표시 금액과 접수 금액이
 * 일치한다(불일치 시 `ORDER_PRODUCT_AMOUNT_MISMATCH`).
 */
export function useCartInfo(orderMethod?: OrderMethodType): CartInfo {
  const [state, setState] = useState<CartState>(initialCartState)

  const loadCartInfo = useCallback(async () => {
    const cart = getCartData()
    if (!cart || cart.products.length === 0) {
      setState({ ...initialCartState, isLoading: false })
      return
    }

    const { data, error } = await getProductsBatch(toBatchItems(cart.products), orderMethod)
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
  }, [orderMethod])

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
    /**
     * 장바구니 상품 정보를 다시 읽는다.
     *
     * 주문 접수가 금액 불일치(`ORDER_PRODUCT_AMOUNT_MISMATCH`)로 거절됐을 때 쓴다 — 점주가 그
     * 사이 가격을 바꾼 상황이므로, 화면의 낡은 금액을 그대로 두고 재시도하면 계속 거절된다.
     */
    refreshCartInfo: loadCartInfo,
  }
}
