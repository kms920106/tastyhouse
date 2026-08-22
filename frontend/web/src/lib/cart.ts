'use client'

export interface CartSelectedOption {
  groupId: number
  optionId: number
}

export interface CartProduct {
  productId: number
  quantity: number
  options: CartSelectedOption[]
  optionKey: string
  /**
   * 손님이 고른 가격 행 id.
   *
   * 가격이 2개 이상인 메뉴("보통"/"곱빼기")는 이 값이 있어야 어느 가격으로 담았는지 알 수 있다.
   * 가격이 1개인 메뉴는 고를 것이 없어 `undefined` 이며, 서버가 `sort=0` 행으로 해석한다.
   *
   * 옵셔널인 이유는 **이 필드가 생기기 전에 저장된 장바구니**가 localStorage 에 남아 있기
   * 때문이다 — 필수로 만들면 기존 장바구니가 깨진다(`getCartData` 의 placeId 하위호환과 같은 이유).
   */
  priceId?: number
}

export interface CartData {
  shopId: number
  products: CartProduct[]
}

const CART_STORAGE_KEY = 'cart'

/**
 * 선택한 옵션들로 고유 키 생성
 * 같은 상품이라도 옵션이 다르면 다른 항목으로 취급
 *
 * `priceId` 도 키에 포함한다 — 같은 메뉴를 옵션까지 똑같이 골랐어도 **가격명이 다르면 다른
 * 항목**이다. 포함하지 않으면 "보통"과 "곱빼기"가 한 줄로 합쳐져 수량만 늘어나고, 결제 시
 * 어느 가격으로 계산해야 하는지 알 수 없어진다.
 *
 * 가격이 1개인 메뉴는 `priceId` 가 없어 접미어를 붙이지 않는다 — 기존 장바구니 키와 같은
 * 문자열이 유지되므로 이 필드가 생기기 전에 담아 둔 항목이 그대로 인식된다.
 */
export function generateOptionKey(
  productId: number,
  options: CartSelectedOption[],
  priceId?: number,
): string {
  const sortedOptions = [...options].sort((a, b) => a.groupId - b.groupId)
  const optionIds = sortedOptions.map((opt) => `${opt.groupId}:${opt.optionId}`).join('|')
  const base = `${productId}_${optionIds}`
  return priceId == null ? base : `${base}_p${priceId}`
}

/**
 * localStorage에서 장바구니 데이터 조회
 * 하위호환: 기존 placeId 키로 저장된 데이터도 shopId로 읽음
 */
export function getCartData(): CartData | null {
  if (typeof window === 'undefined') return null

  try {
    const data = localStorage.getItem(CART_STORAGE_KEY)
    if (!data) return null
    const parsed = JSON.parse(data)
    // 구 버전(placeId) 하위호환 처리
    if (parsed && parsed.shopId == null && parsed.placeId != null) {
      parsed.shopId = parsed.placeId
    }
    return parsed
  } catch {
    return null
  }
}

/**
 * 장바구니 상품 목록 조회
 */
export function getCartProducts(): CartProduct[] {
  return getCartData()?.products ?? []
}

/**
 * 장바구니의 shopId 조회
 */
export function getCartShopId(): number | null {
  return getCartData()?.shopId ?? null
}

/**
 * 장바구니에 상품 추가
 * 같은 상품+옵션 조합이면 수량만 증가
 * 다른 가게의 상품이 이미 있으면 false 반환 (교체 필요)
 */
export function addToCart(
  shopId: number,
  item: Omit<CartProduct, 'optionKey' | 'quantity'>,
  quantity: number = 1,
): CartData {
  const cart = getCartData()
  const optionKey = generateOptionKey(item.productId, item.options, item.priceId)

  const products = cart?.products ?? []
  const existingIndex = products.findIndex((p) => p.optionKey === optionKey)

  if (existingIndex >= 0) {
    products[existingIndex].quantity += quantity
  } else {
    products.push({
      ...item,
      optionKey,
      quantity,
    })
  }

  const newCart: CartData = { shopId, products }
  localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(newCart))
  return newCart
}

/**
 * 장바구니를 초기화하고 새 가게의 상품 추가
 */
export function replaceCartAndAdd(
  shopId: number,
  item: Omit<CartProduct, 'optionKey' | 'quantity'>,
  quantity: number = 1,
): CartData {
  const optionKey = generateOptionKey(item.productId, item.options, item.priceId)
  const newCart: CartData = {
    shopId,
    products: [{ ...item, optionKey, quantity }],
  }
  localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(newCart))
  return newCart
}

/**
 * 장바구니 아이템 수량 변경
 */
export function updateCartItemQuantity(optionKey: string, quantity: number): CartData | null {
  const cart = getCartData()
  if (!cart) return null

  if (quantity <= 0) {
    cart.products = cart.products.filter((p) => p.optionKey !== optionKey)
  } else {
    const index = cart.products.findIndex((p) => p.optionKey === optionKey)
    if (index >= 0) {
      cart.products[index].quantity = quantity
    }
  }

  if (cart.products.length === 0) {
    localStorage.removeItem(CART_STORAGE_KEY)
    return null
  }

  localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(cart))
  return cart
}

/**
 * 장바구니에서 아이템 제거
 */
export function removeFromCart(optionKey: string): CartData | null {
  const cart = getCartData()
  if (!cart) return null

  cart.products = cart.products.filter((p) => p.optionKey !== optionKey)

  if (cart.products.length === 0) {
    localStorage.removeItem(CART_STORAGE_KEY)
    return null
  }

  localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(cart))
  return cart
}

/**
 * 전체 장바구니 삭제
 */
export function clearCart(): void {
  localStorage.removeItem(CART_STORAGE_KEY)
}

/**
 * 장바구니 아이템 수 조회
 */
export function getCartItemCount(): number {
  return getCartProducts().reduce((sum, item) => sum + item.quantity, 0)
}

/**
 * 장바구니 상품 종류 수 조회
 */
export function getCartProductTypeCount(): number {
  return getCartProducts().length
}
