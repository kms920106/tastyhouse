import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '@/components/layouts/Header'
import { BackButton } from '@/components/layouts/header-parts'
import { Skeleton } from '@/components/ui/shadcn/skeleton'

export default function EventDetailSectionSkeleton() {
  return (
    <>
      <Header variant="white" height={55}>
        <HeaderLeft>
          <BackButton />
        </HeaderLeft>
        <HeaderCenter>
          <HeaderTitle>이벤트</HeaderTitle>
        </HeaderCenter>
      </Header>
      <Skeleton className="w-full aspect-[3/4]" />
    </>
  )
}
