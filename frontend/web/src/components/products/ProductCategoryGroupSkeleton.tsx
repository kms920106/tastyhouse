import Divider from '@/components/ui/Divider'
import { Skeleton } from '@/components/ui/shadcn/skeleton'
import { ProductItemSkeleton } from './ProductItemSkeleton'

export function ProductCategoryGroupSkeleton() {
  return (
    <div className="pt-[30px] pb-[15px] border-b border-line box-border">
      <Skeleton className="w-20 h-[16px] mb-5" />
      <div>
        <ProductItemSkeleton />
        <Divider />
        <ProductItemSkeleton />
        <Divider />
        <ProductItemSkeleton />
      </div>
    </div>
  )
}
