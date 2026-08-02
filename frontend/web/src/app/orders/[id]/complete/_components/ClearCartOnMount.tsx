'use client'

import { clearCart } from '@/lib/cart'
import { useEffect } from 'react'

/**
 * 주문 완료 페이지 진입 시 장바구니를 1회 초기화한다.
 * 결제가 서버에서 확정된 후 도달하는 유일한 클라이언트 시점이므로
 * 결제 취소/실패 시에는 장바구니가 보존된다.
 * 렌더링 출력이 없는 side-effect 전용 컴포넌트.
 */
export default function ClearCartOnMount() {
  useEffect(() => {
    clearCart()
  }, [])

  return null
}
