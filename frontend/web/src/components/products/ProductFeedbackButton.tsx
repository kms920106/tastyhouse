'use client'

import ProductFeedbackDialog from '@/components/products/ProductFeedbackDialog'
import { toast } from '@/components/ui/AppToaster'
import { useMyProfile } from '@/domains/member/member.hook'
import {
  PRODUCT_FEEDBACK_COPY,
  PRODUCT_FEEDBACK_MESSAGE,
} from '@/domains/product/product.constants'
import { PAGE_PATHS } from '@/lib/paths'
import { useRouter } from 'next/navigation'
import { useState } from 'react'

interface Props {
  productId: number
}

/**
 * 메뉴 상세 하단의 '의견 보내기' 진입 버튼.
 *
 * **비로그인이면 다이얼로그를 열지 않고 로그인 화면으로 보낸다** — 익명 제보를 허용하면 경쟁
 * 가게의 반복 허위 제보를 막을 수 없어 서버가 로그인을 요구하므로, 폼을 다 채운 뒤 401 로
 * 되돌리는 대신 진입 시점에 안내한다.
 */
export default function ProductFeedbackButton({ productId }: Props) {
  const router = useRouter()
  const { isLoggedIn, isLoading } = useMyProfile()
  const [open, setOpen] = useState(false)

  const handleClick = () => {
    // 로그인 판정이 끝나기 전에는 어느 쪽으로도 보내지 않는다 — 로그인 상태인데 로그인
    // 화면으로 튕기면 되돌아올 길이 없다.
    if (isLoading) return

    if (!isLoggedIn) {
      toast(PRODUCT_FEEDBACK_MESSAGE.LOGIN_REQUIRED)
      router.push(PAGE_PATHS.AUTH_LOGIN)
      return
    }

    setOpen(true)
  }

  return (
    <>
      <div className="px-[15px] py-5">
        <button
          type="button"
          onClick={handleClick}
          className="h-[50px] w-full border border-line text-sm leading-[14px] text-[#666666]"
        >
          {PRODUCT_FEEDBACK_COPY.TRIGGER}
        </button>
      </div>

      <ProductFeedbackDialog productId={productId} open={open} onOpenChange={setOpen} />
    </>
  )
}
