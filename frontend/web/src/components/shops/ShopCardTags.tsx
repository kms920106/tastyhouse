import HashTag from '@/components/ui/HashTag'
import { cn } from '@/lib/utils'
import * as React from 'react'

interface Props extends React.HTMLAttributes<HTMLDivElement> {
  tags: string[]
  variant?: 'primary' | 'secondary'
}

export function ShopCardTags({ tags, variant = 'primary', className, ...props }: Props) {
  return (
    <div className={cn('flex gap-1.5 mt-[15px] overflow-hidden', className)} {...props}>
      {tags.map((tag, index) => (
        <HashTag key={index} tag={tag} variant={variant} />
      ))}
    </div>
  )
}
