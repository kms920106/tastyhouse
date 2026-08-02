'use client'

import { cn } from '@/lib/utils'

interface Props {
  onClick: () => void
  className?: string
}

export function MoreButton({ onClick, className = '' }: Props) {
  return (
    <button
      onClick={onClick}
      className={cn('absolute bottom-1 right-0 text-sm leading-[14px] text-[#cccccc] bg-white pl-1', className)}
    >
      <span className="text-black">... </span>
      <span className="cursor-pointer">더보기</span>
    </button>
  )
}
