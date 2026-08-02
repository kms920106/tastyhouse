'use client'

import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import type { OrderMethodType, OrderMethod } from '@/domains/order'
import { PAGE_PATHS } from '@/lib/paths'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import OrderMethodGrid from './OrderMethodGrid'

interface Props {
  shopId: number
  orderMethods: OrderMethod[]
}

export default function OrderMethodContentClient({ shopId, orderMethods }: Props) {
  const router = useRouter()

  const [selectedMethod, setSelectedMethod] = useState<OrderMethodType | null>(null)

  const handleNext = () => {
    if (!selectedMethod) return
    if (selectedMethod === 'RESERVATION') {
      router.push(PAGE_PATHS.ORDER_RESERVATION(shopId))
      return
    }
    router.push(PAGE_PATHS.ORDER_MENUS(shopId, selectedMethod))
  }

  return (
    <>
      <div className="flex-1 flex flex-col justify-center px-[15px]">
        <div className="flex flex-col gap-[21px] text-center">
          <h2 className="text-[23px] leading-[23px]">원하시는 주문방법을 선택해 주세요.</h2>
          <p className="text-sm leading-[21px] text-[#999999]">
            가게 사정에 따라 가능한 주문방법이 달라질 수 있으며,
            <br />
            자세한 사항은 가게 정보를 확인해 주세요.
          </p>
        </div>
        <OrderMethodGrid
          orderMethods={orderMethods}
          selectedMethod={selectedMethod}
          onSelect={setSelectedMethod}
        />
      </div>
      <div className="px-[15px] py-2.5">
        <AppPrimaryButton onClick={handleNext} disabled={!selectedMethod}>
          다음
        </AppPrimaryButton>
      </div>
    </>
  )
}
