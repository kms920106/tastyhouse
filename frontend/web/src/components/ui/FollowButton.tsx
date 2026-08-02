'use client'

import AppButton from '@/components/ui/AppButton'
import { cn } from '@/lib/utils'

interface Props {
  following: boolean
  onClick: () => void
  className?: string
}

export default function FollowButton({ following, onClick, className }: Props) {
  return (
    <AppButton
      onClick={onClick}
      className={cn(
        'h-[31px] px-[23px] py-2.5 text-xs leading-[12px] rounded-[2.5px]',
        following ? 'bg-white text-main border border-main box-border' : 'bg-main text-white',
        className,
      )}
    >
      {following ? '팔로잉' : '팔로우'}
    </AppButton>
  )
}
