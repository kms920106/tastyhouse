'use client'

import {
  Drawer,
  DrawerClose,
  DrawerContent,
  DrawerDescription,
  DrawerTitle,
  DrawerTrigger,
} from '@/components/ui/shadcn/drawer'
import Icon from '@/components/ui/Icon'
import { IoCheckmark } from 'react-icons/io5'

export type TodayDiscountSortType = 'RECOMMENDED' | 'DISCOUNT_RATE' | 'PRICE_LOW' | 'PRICE_HIGH'

const SORT_OPTIONS: { label: string; value: TodayDiscountSortType }[] = [
  { label: '추천순', value: 'RECOMMENDED' },
  { label: '할인율 높은순', value: 'DISCOUNT_RATE' },
  { label: '낮은 가격순', value: 'PRICE_LOW' },
  { label: '높은 가격순', value: 'PRICE_HIGH' },
]

interface Props {
  value: TodayDiscountSortType
  onChange: (value: TodayDiscountSortType) => void
}

export default function TodayDiscountSortDrawer({ value, onChange }: Props) {
  const currentLabel = SORT_OPTIONS.find((opt) => opt.value === value)?.label ?? '추천순'

  return (
    <Drawer autoFocus>
      <DrawerTrigger
        aria-label="정렬 선택"
        className="flex items-center gap-2.5 text-sm leading-[14px] text-[#333333] cursor-pointer"
      >
        <span>{currentLabel}</span>
        <Icon name="filter-nav" />
      </DrawerTrigger>
      <DrawerContent className="bg-transparent p-[15px] border-none">
        <DrawerTitle className="sr-only">정렬 선택</DrawerTitle>
        <DrawerDescription className="sr-only">
          추천순, 할인율 높은순, 낮은 가격순, 높은 가격순
        </DrawerDescription>
        <div className="text-center bg-white rounded-[14px]">
          {SORT_OPTIONS.map(({ label, value: optionValue }, index) => {
            const isSelected = value === optionValue
            return (
              <div key={optionValue}>
                {index > 0 && <div className="h-px bg-[#f6f6f6]" />}
                <DrawerClose asChild>
                  <button
                    type="button"
                    onClick={() => onChange(optionValue)}
                    className={`flex items-center justify-between w-full px-4 py-[20.5px] text-sm leading-[14px] cursor-pointer ${
                      isSelected ? 'font-bold' : 'text-[#333333]'
                    }`}
                  >
                    <span>{label}</span>
                    {isSelected && <IoCheckmark size={16} aria-hidden="true" />}
                  </button>
                </DrawerClose>
              </div>
            )
          })}
        </div>
      </DrawerContent>
    </Drawer>
  )
}
