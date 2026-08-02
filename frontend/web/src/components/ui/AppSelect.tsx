import { cn } from '@/lib/utils'
import Icon from '@/components/ui/Icon'
import * as React from 'react'

type Props = React.SelectHTMLAttributes<HTMLSelectElement>

export default function AppSelect({ className, children, ...props }: Props) {
  return (
    <div className="relative w-full">
      <select
        className={cn(
          'w-full h-[50px] pl-4 pr-10 text-sm leading-[14px] border border-line focus:border-[#666666] box-border appearance-none bg-white focus:outline-none',
          (props.value === '' || props.value === undefined) && 'text-[#aaaaaa]',
          className,
        )}
        {...props}
      >
        {children}
      </select>
      <div className="pointer-events-none absolute top-1/2 right-4 -translate-y-1/2">
        <Icon name="nav-bottom-gray" className="select-none" />
      </div>
    </div>
  )
}
