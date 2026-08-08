'use client'

import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import { ORDER_METHOD_COPY, type OrderMethodType, type OrderMethod } from '@/domains/order'
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

  // 목록이 갱신되며 선택 중이던 방식이 불가로 바뀔 수 있어, 버튼 disabled 외에 '다음'에서도 한 번 더 막는다
  const isSelectedMethodOrderable =
    selectedMethod !== null &&
    (orderMethods.find((method) => method.code === selectedMethod)?.orderable ?? false)

  // 조회는 성공했고 '지금 주문할 수 없음'은 정상 결과이므로 FetchErrorState가 아니라 안내 문구를 보여준다.
  // 배정 0건(기다려도 안 됨)과 전부 불가(일시적)는 안내 문구가 달라야 하므로 분리해 판정한다.
  const hasNoAssignedMethod = orderMethods.length === 0
  const hasOrderableMethod = orderMethods.some((method) => method.orderable)

  const handleNext = () => {
    if (!selectedMethod || !isSelectedMethodOrderable) return
    if (selectedMethod === 'RESERVATION') {
      router.push(PAGE_PATHS.ORDER_RESERVATION(shopId))
      return
    }
    router.push(PAGE_PATHS.ORDER_MENUS(shopId, selectedMethod))
  }

  const { title, description } = hasNoAssignedMethod
    ? {
        title: ORDER_METHOD_COPY.NONE_ASSIGNED_TITLE,
        description: ORDER_METHOD_COPY.NONE_ASSIGNED_DESCRIPTION,
      }
    : hasOrderableMethod
      ? {
          title: ORDER_METHOD_COPY.SELECT_TITLE,
          description: (
            <>
              {ORDER_METHOD_COPY.SELECT_DESCRIPTION_LINE1}
              <br />
              {ORDER_METHOD_COPY.SELECT_DESCRIPTION_LINE2}
            </>
          ),
        }
      : {
          title: ORDER_METHOD_COPY.ALL_UNAVAILABLE_TITLE,
          description: ORDER_METHOD_COPY.ALL_UNAVAILABLE_DESCRIPTION,
        }

  return (
    <>
      <div className="flex-1 flex flex-col justify-center px-[15px]">
        <div className="flex flex-col gap-[21px] text-center">
          <h2 className="text-[23px] leading-[23px]">{title}</h2>
          <p className="text-sm leading-[21px] text-[#999999]">{description}</p>
        </div>
        <OrderMethodGrid
          orderMethods={orderMethods}
          selectedMethod={selectedMethod}
          onSelect={setSelectedMethod}
        />
      </div>
      <div className="px-[15px] py-2.5">
        <AppPrimaryButton onClick={handleNext} disabled={!isSelectedMethodOrderable}>
          {ORDER_METHOD_COPY.NEXT_BUTTON}
        </AppPrimaryButton>
      </div>
    </>
  )
}
