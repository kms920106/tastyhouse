'use client'

import ShopDeliveryTipDialog from '@/components/shops/ShopDeliveryTipDialog'
import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import StickyFooter from '@/components/ui/StickyFooter'
import { useCartInfo } from '@/hooks/useCartInfo'
import { removeFromCart, updateCartItemQuantity } from '@/lib/cart'
import {
  calculateMinOrderShortfall,
  calculateTotalProductAmount,
  calculateTotalProductDiscount,
  calculateTotalProductPaymentAmount,
} from '@/lib/paymentCalculation'
import CartItemList from './CartItemList'
import CartSelectionControl from './CartSelectionControl'
import PaymentSummary from './PaymentSummary'
import ShopOrderCartContentSkeleton from './ShopOrderCartContentSkeleton'
import ShopOrderCartLinkButton from './ShopOrderCartLinkButton'

import type { OrderMethodType, OrderProduct } from '@/domains/order'
import { useEffect, useMemo, useState } from 'react'

interface Props {
  shopId: number
  shopName: string
  orderMethod: OrderMethodType
  minOrderAmount: number
  /** 배달팁 하한. 상한과 함께 0이면 노출하지 않는다 */
  minDeliveryTip: number
  /** 배달팁 상한 (고객 주소 확정 전) */
  maxDeliveryTip: number
}

export default function ShopOrderCartContentClient({
  shopId,
  shopName,
  orderMethod,
  minOrderAmount,
  minDeliveryTip,
  maxDeliveryTip,
}: Props) {
  const { items: initialItems, isLoading } = useCartInfo()

  const [cartItems, setCartItems] = useState<OrderProduct[]>([])
  const [selectedKeys, setSelectedKeys] = useState<Set<string>>(new Set())
  const [isDeliveryTipDialogOpen, setIsDeliveryTipDialogOpen] = useState(false)

  useEffect(() => {
    setCartItems(initialItems)
    setSelectedKeys(new Set(initialItems.map((item) => item.optionKey)))
  }, [initialItems])

  const selectedItems = useMemo(
    () => cartItems.filter((item) => selectedKeys.has(item.optionKey)),
    [cartItems, selectedKeys],
  )

  const allSelected = cartItems.length > 0 && selectedKeys.size === cartItems.length
  const selectedCount = selectedKeys.size

  const totalProductAmount = calculateTotalProductAmount(selectedItems)
  const totalDiscountAmount = calculateTotalProductDiscount(selectedItems)
  const totalProductPaymentAmount = calculateTotalProductPaymentAmount(selectedItems)

  // 최소주문금액 판정은 선택분(selectedItems)이 아니라 장바구니 전체(cartItems)를 기준으로 한다.
  // 결제 화면은 useCartInfo()로 장바구니 전체를 읽어 그대로 주문에 담으므로(선택 상태를 넘겨받지 않는다),
  // 여기서 선택분으로 판정하면 실제로 접수될 주문과 기준이 어긋나 버튼만 막히거나 반대로 서버에서 거절된다.
  const minOrderShortfall = calculateMinOrderShortfall(
    calculateTotalProductPaymentAmount(cartItems),
    minOrderAmount,
    orderMethod,
  )

  const handleToggleSelectAll = () => {
    if (allSelected) {
      setSelectedKeys(new Set())
    } else {
      setSelectedKeys(new Set(cartItems.map((item) => item.optionKey)))
    }
  }

  const handleToggleSelect = (optionKey: string) => {
    setSelectedKeys((prev) => {
      const next = new Set(prev)
      if (next.has(optionKey)) {
        next.delete(optionKey)
      } else {
        next.add(optionKey)
      }
      return next
    })
  }

  const handleQuantityChange = (optionKey: string, quantity: number) => {
    updateCartItemQuantity(optionKey, quantity)
    setCartItems((items) =>
      items.map((item) => (item.optionKey === optionKey ? { ...item, quantity } : item)),
    )
  }

  const handleRemove = (optionKey: string) => {
    removeFromCart(optionKey)
    setCartItems((items) => items.filter((item) => item.optionKey !== optionKey))
    setSelectedKeys((prev) => {
      const next = new Set(prev)
      next.delete(optionKey)
      return next
    })
  }

  const handleDeleteSelected = () => {
    selectedKeys.forEach((key) => removeFromCart(key))
    setCartItems((items) => items.filter((item) => !selectedKeys.has(item.optionKey)))
    setSelectedKeys(new Set())
  }

  if (isLoading) {
    return <ShopOrderCartContentSkeleton />
  }

  return (
    <>
      <SectionStack>
        <BorderedSection>
          <CartSelectionControl
            selectedCount={selectedCount}
            totalCount={cartItems.length}
            allSelected={allSelected}
            onToggleSelectAll={handleToggleSelectAll}
            onDeleteSelected={handleDeleteSelected}
          />
        </BorderedSection>
        <BorderedSection>
          <CartItemList
            cartItems={cartItems}
            shopName={shopName}
            selectedKeys={selectedKeys}
            onToggleSelect={handleToggleSelect}
            onQuantityChange={handleQuantityChange}
            onRemove={handleRemove}
          />
        </BorderedSection>
      </SectionStack>
      <PaymentSummary
        totalProductAmount={totalProductAmount}
        totalDiscountAmount={totalDiscountAmount}
        totalProductPaymentAmount={totalProductPaymentAmount}
        minDeliveryTip={minDeliveryTip}
        maxDeliveryTip={maxDeliveryTip}
        onDeliveryTipClick={() => setIsDeliveryTipDialogOpen(true)}
      />
      <ShopDeliveryTipDialog
        open={isDeliveryTipDialogOpen}
        onOpenChange={setIsDeliveryTipDialogOpen}
        shopId={shopId}
        orderAmount={totalProductPaymentAmount}
        orderMethod={orderMethod}
      />
      <StickyFooter>
        <div className="px-[15px] py-2.5 bg-[#f9f9f9]">
          <ShopOrderCartLinkButton
            shopId={shopId}
            orderMethod={orderMethod}
            minOrderShortfall={minOrderShortfall}
          />
        </div>
      </StickyFooter>
    </>
  )
}
