import Divider from '@/components/ui/Divider'
import { cn } from '@/lib/utils'
import React from 'react'

interface Props {
  categoryName: string
  children: React.ReactNode
  className?: string
}

export default function ProductCategoryGroup({ categoryName, children, className = '' }: Props) {
  const childArray = React.Children.toArray(children)

  return (
    <div className={cn('pt-[30px] pb-[15px]', className)}>
      <h3 className="mb-5 text-base leading-[16px] font-bold">{categoryName}</h3>
      <div>
        {childArray.map((child, i) => (
          <React.Fragment key={i}>
            {child}
            {i < childArray.length - 1 && <Divider />}
          </React.Fragment>
        ))}
      </div>
    </div>
  )
}
