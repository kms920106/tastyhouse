'use client'

import { cn } from '@/lib/utils'
import Image from 'next/image'

interface Props {
  name: string
  imageUrl: string
  isSelected: boolean
}

export default function FoodTypeItemContent({ name, imageUrl, isSelected }: Props) {
  return (
    <>
      <div className="relative flex items-center justify-center">
        <div className="relative w-14 h-9">
          <Image src={imageUrl} alt={name} fill className="object-contain" />
        </div>
      </div>
      <span
        className={cn(
          'text-xs leading-[12px] whitespace-nowrap',
          isSelected ? 'text-main' : 'text-[#cccccc] opacity-50',
        )}
      >
        {name}
      </span>
    </>
  )
}
