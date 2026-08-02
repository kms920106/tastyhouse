'use client'

import type { OrderMethodType } from '@/domains/order'
import { getCartItemCount } from '@/lib/cart'
import { useEffect, useState } from 'react'
import ShopOrderMenuDetailCartButton from './ShopOrderMenuDetailCartButton'

interface Props {
  shopId: number
  orderMethod: OrderMethodType
}

export default function ShopOrderMenuDetailCartButtonClient({ shopId, orderMethod }: Props) {
  const [cartItemCount, setCartItemCount] = useState(0)

  useEffect(() => {
    setCartItemCount(getCartItemCount())
  }, [shopId])

  return (
    <ShopOrderMenuDetailCartButton shopId={shopId} count={cartItemCount} orderMethod={orderMethod} />
  )
}
