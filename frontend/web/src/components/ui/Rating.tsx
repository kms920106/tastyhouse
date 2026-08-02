import { formatDecimal } from '@/lib/number'
import { cn } from '@/lib/utils'
import { ComponentPropsWithoutRef, ElementType } from 'react'

type RatingProps<T extends ElementType = 'span'> = {
  as?: T
  value: number
  decimals?: number
  className?: string
} & Omit<ComponentPropsWithoutRef<T>, 'as' | 'value' | 'className'>

export default function Rating<T extends ElementType = 'span'>({
  as,
  value,
  decimals = 1,
  className,
  ...props
}: RatingProps<T>) {
  const Component = as || 'span'

  return (
    <Component className={cn('text-[19px] leading-[19px] text-main', className)} {...props}>
      {formatDecimal(value, decimals)}
    </Component>
  )
}
