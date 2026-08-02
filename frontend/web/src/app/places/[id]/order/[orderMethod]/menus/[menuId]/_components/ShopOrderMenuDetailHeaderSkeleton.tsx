import Header, { HeaderLeft, HeaderRight } from '@/components/layouts/Header'
import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function ShopOrderMenuDetailHeaderSkeleton() {
  return (
    <Header variant="white" height={55}>
      <HeaderLeft>
        <div className="flex items-center justify-center w-[55px] h-[55px]">
          <Skeleton className="h-[16px] w-[9px] rounded-sm" />
        </div>
      </HeaderLeft>
      <HeaderRight>
        <div className="flex items-center justify-center w-[55px] h-[55px]">
          <Skeleton className="h-[20px] w-[20px] rounded-full" />
        </div>
        <div className="flex items-center justify-center w-[55px] h-[55px]">
          <Skeleton className="h-[22px] w-[22px] rounded-full" />
        </div>
      </HeaderRight>
    </Header>
  )
}
