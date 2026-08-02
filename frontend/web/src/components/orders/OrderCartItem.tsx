'use client'

import ImageContainer from '@/components/ui/ImageContainer'
import type { OrderProductOption } from '@/domains/order'
import { formatNumber } from '@/lib/number'
import { HiOutlineXMark } from 'react-icons/hi2'
import CircleCheckbox from '../ui/CircleCheckbox'
import QuantityStepper from '../ui/QuantityStepper'

interface Props {
  optionKey: string
  name: string
  imageUrl: string
  salePrice: number
  originalPrice: number
  quantity: number
  selected: boolean
  options?: OrderProductOption[]
  onToggleSelect: (optionKey: string) => void
  onQuantityChange: (optionKey: string, quantity: number) => void
  onRemove: (optionKey: string) => void
}

export default function OrderCartItem({
  optionKey,
  name,
  imageUrl,
  salePrice,
  originalPrice,
  quantity,
  selected,
  options,
  onToggleSelect,
  onQuantityChange,
  onRemove,
}: Props) {
  return (
    <div className="flex py-5">
      <CircleCheckbox checked={selected} onChange={() => onToggleSelect(optionKey)} />
      <ImageContainer src={imageUrl} alt={name} size={65} className="ml-2.5" />
      <div className="flex-1 ml-4">
        <h3 className="text-sm leading-[14px] truncate">{name}</h3>
        {options && options.length > 0 && (
          <div className="mt-1 space-y-1">
            {options.map((opt, index) => (
              <p key={index} className="text-xs text-[#999999]">
                {opt.optionName}
                {opt.additionalPrice > 0 && ` (${formatNumber(opt.additionalPrice)}원)`}
              </p>
            ))}
          </div>
        )}
        <div className="flex items-baseline mt-[15px]">
          <span className="text-base leading-[16px]">{formatNumber(salePrice)}원</span>
          {originalPrice > salePrice && (
            <span className="ml-[7px] text-xs text-[#aaaaaa] line-through">
              {formatNumber(originalPrice)}원
            </span>
          )}
        </div>
      </div>
      <div className="flex flex-col items-end justify-between">
        <button onClick={() => onRemove(optionKey)} className="w-5 h-5 -mt-1">
          <HiOutlineXMark size={20} color="#cccccc" />
        </button>
        <QuantityStepper value={quantity} onChange={(next) => onQuantityChange(optionKey, next)} />
      </div>
    </div>
  )
}
