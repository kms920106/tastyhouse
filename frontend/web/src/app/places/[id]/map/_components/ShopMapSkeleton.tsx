import Header, { HeaderCenter, HeaderLeft } from '@/components/layouts/Header'
import { BackButton } from '@/components/layouts/header-parts'
import { Skeleton } from '@/components/ui/shadcn/skeleton'
import { MapLoadingIndicator } from './MapLoadingIndicator'

export function ShopMapSkeleton() {
  return (
    <div className="flex flex-col h-screen">
      <Header variant="white" height={55}>
        <HeaderLeft>
          <BackButton />
        </HeaderLeft>
        <HeaderCenter>
          <Skeleton className="h-[17px] w-[120px]" />
        </HeaderCenter>
      </Header>
      <div className="relative flex-1 min-h-0">
        <Skeleton className="w-full h-full rounded-none" />
        <MapLoadingIndicator />
      </div>
    </div>
  )
}
