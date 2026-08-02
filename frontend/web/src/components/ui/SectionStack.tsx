import { cn } from '@/lib/utils'
import { ReactNode } from 'react'

interface Props {
  children: ReactNode
  className?: string
}

export default function SectionStack({ children, className = '' }: Props) {
  return (
    <div
      className={cn(
        'flex flex-col gap-2.5 bg-[#f9f9f9]',
        '[&>*:first-child]:border-t-0',
        '[&>*:last-child]:border-b-0',
        className,
      )}
    >
      {children}
    </div>
  )
}
