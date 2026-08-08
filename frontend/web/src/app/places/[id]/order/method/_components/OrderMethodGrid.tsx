'use client'

import type { OrderMethodType, OrderMethod } from '@/domains/order'
import Icon from '@/components/ui/Icon'
import { getOrderMethodIconName } from '@/components/ui/icon-helpers'
import { cn } from '@/lib/utils'

const METHOD_CONFIG: Record<OrderMethodType, { title: string }> = {
  TABLE: { title: '바로 주문하기' },
  RESERVATION: { title: '예약하기' },
  DELIVERY: { title: '배달하기' },
  TAKEOUT: { title: '포장하기' },
}

const METHOD_ORDER: OrderMethodType[] = ['TABLE', 'RESERVATION', 'DELIVERY', 'TAKEOUT']

interface Props {
  orderMethods: OrderMethod[]
  selectedMethod: OrderMethodType | null
  onSelect: (method: OrderMethodType) => void
}

export default function OrderMethodGrid({ orderMethods, selectedMethod, onSelect }: Props) {
  const methods = [...orderMethods]
    .sort((a, b) => METHOD_ORDER.indexOf(a.code) - METHOD_ORDER.indexOf(b.code))
    // METHOD_CONFIG를 먼저 펼친다 — 나중에 펼치면 향후 추가되는 정적 키가 서버의 orderable을 덮을 수 있다
    .map((method) => ({
      ...METHOD_CONFIG[method.code],
      id: method.code,
      orderable: method.orderable,
      // 사유 문구는 서버가 완성해 내려주므로 그대로 표시한다
      unavailableReasonName: method.unavailableReasonName,
    }))

  return (
    <div className="grid grid-cols-2 gap-[15px] mt-[60px]">
      {methods.map((method) => {
        // 선택 중이던 방식이 불가로 바뀌면 선택 표시를 거둔다 — 비활성 버튼이 선택된 것처럼 보이지 않게 한다
        const isSelected = selectedMethod === method.id && method.orderable

        return (
          <div key={method.id} className="flex flex-col gap-[5px]">
            <button
              onClick={() => onSelect(method.id)}
              disabled={!method.orderable}
              className={cn(
                'flex flex-col items-center justify-center px-5 py-[17px] border box-border cursor-pointer',
                isSelected ? 'bg-[#f8f5f4] border-main' : 'border-line',
                // 모바일 웹은 hover 툴팁 접근 경로가 없어, 불가 상태는 opacity와 하단 캡션으로만 알린다
                !method.orderable && 'opacity-40 cursor-not-allowed',
              )}
              style={{ aspectRatio: '165/100' }}
            >
              <div className="relative flex items-center justify-center w-full h-12 mb-2.5">
                <Icon
                  name={getOrderMethodIconName(method.id, isSelected)}
                  alt={method.title}
                  width={32}
                  height={32}
                  className="object-contain max-w-full max-h-full"
                  style={{ width: 'auto', height: 'auto' }}
                />
              </div>
              <span
                className={cn(
                  'text-[13px] leading-[13px]',
                  isSelected ? 'text-main' : 'text-[#cccccc]',
                )}
              >
                {method.title}
              </span>
            </button>
            {!method.orderable && method.unavailableReasonName && (
              <span className="text-[11px] leading-[14px] text-[#999999] text-center">
                {method.unavailableReasonName}
              </span>
            )}
          </div>
        )
      })}
    </div>
  )
}
