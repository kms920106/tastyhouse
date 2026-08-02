'use client'

import type { OrderMethodType, OrderMethod } from '@/domains/order'
import Icon from '@/components/ui/Icon'
import { getOrderMethodIconName } from '@/components/ui/icon-helpers'
import { cn } from '@/lib/utils'

const METHOD_CONFIG: Record<OrderMethodType, { title: string }> = {
  TABLE: { title: '바로 주문하기' },
  RESERVATION: { title: '예약하기' },
  DELIVERY:    { title: '배달하기' },
  TAKEOUT:     { title: '포장하기' },
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
    .map((method) => ({ id: method.code, ...METHOD_CONFIG[method.code] }))

  return (
    <div className="grid grid-cols-2 gap-[15px] mt-[60px]">
      {methods.map((method) => (
        <button
          key={method.id}
          onClick={() => onSelect(method.id)}
          className={cn(
            'flex flex-col items-center justify-center px-5 py-[17px] border box-border cursor-pointer',
            selectedMethod === method.id ? 'bg-[#f8f5f4] border-main' : 'border-line',
          )}
          style={{ aspectRatio: '165/100' }}
        >
          <div className="relative flex items-center justify-center w-full h-12 mb-2.5">
            <Icon
              name={getOrderMethodIconName(method.id, selectedMethod === method.id)}
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
              selectedMethod === method.id ? 'text-main' : 'text-[#cccccc]',
            )}
          >
            {method.title}
          </span>
        </button>
      ))}
    </div>
  )
}
