'use client'

import { getCartItemCount } from '@/lib/cart'
import { useEffect, useState } from 'react'

interface Props {
  shopId: number
}

export default function CartItemCount({ shopId }: Props) {
  const [count, setCount] = useState(0)

  useEffect(() => {
    const updateCount = () => {
      setCount(getCartItemCount())
    }

    // 초기 카운트 설정
    updateCount()

    // storage 변경 이벤트 리스너 등록 (다른 탭이나 컴포넌트에서 장바구니 변경 시)
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === 'cart') {
        updateCount()
      }
    }

    window.addEventListener('storage', handleStorageChange)

    // 커스텀 이벤트 리스너 (같은 탭 내에서 장바구니 변경 시)
    window.addEventListener('cartUpdated', updateCount)

    return () => {
      window.removeEventListener('storage', handleStorageChange)
      window.removeEventListener('cartUpdated', updateCount)
    }
  }, [shopId])

  return (
    <>
      장바구니 <span>(<span className="font-bold">{count}</span>)</span>
    </>
  )
}
