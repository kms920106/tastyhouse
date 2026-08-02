import { DiscountProductItemSkeleton } from '@/components/products/DiscountProductItemSkeleton'
import Divider from '@/components/ui/Divider'
import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function HomeChoiceSwiperSkeleton() {
  return (
    <div className="pb-12">
      <div className="flex gap-5">
        {[1, 2].map((i) => (
          <div
            key={i}
            className="swiper-slide-skeleton flex-shrink-0"
            style={{ width: 'calc((100% - 3px) / 1.15)' }}
          >
            <div className="relative w-full aspect-[2/3] overflow-hidden bg-gray-200 animate-pulse">
              <div className="absolute bottom-0 left-0 right-0 p-8 text-white">
                <Skeleton className="w-3/4 h-[17px] mb-3" />
                <Skeleton className="w-full h-3 opacity-90 leading-relaxed mb-1" />
                <Skeleton className="w-full h-3 opacity-90 leading-relaxed" />
              </div>
            </div>
            <div className="mb-10">
              <DiscountProductItemSkeleton />
              <Divider />
              <DiscountProductItemSkeleton />
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
