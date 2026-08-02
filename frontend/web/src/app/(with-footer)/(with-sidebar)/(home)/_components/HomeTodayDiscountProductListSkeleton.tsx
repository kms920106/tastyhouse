import { DiscountProductItemSkeleton } from '@/components/products/DiscountProductItemSkeleton'
import Divider from '@/components/ui/Divider'
import { Fragment } from 'react'

const COUNT = 4

export function HomeTodayDiscountProductListSkeleton() {
  return (
    <div className="py-[15px] border-t border-b border-line">
      {Array.from({ length: COUNT }).map((_, i) => (
        <Fragment key={i}>
          <DiscountProductItemSkeleton />
          {i < COUNT - 1 && <Divider />}
        </Fragment>
      ))}
    </div>
  )
}
