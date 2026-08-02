'use client'

import { cn } from '@/lib/utils'
import Image from 'next/image'

interface Props {
  name: string
  imageUrl: string
  isSelected: boolean
}

export default function FacilityItemContent({ name, imageUrl, isSelected }: Props) {
  return (
    <>
      <div className="relative flex items-center justify-center w-full h-12 mb-[15px]">
        <Image
          src={imageUrl}
          alt={name}
          width={32}
          height={25}
          className="object-contain max-w-full max-h-full"
          style={{ width: 'auto', height: 'auto' }}
        />
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
