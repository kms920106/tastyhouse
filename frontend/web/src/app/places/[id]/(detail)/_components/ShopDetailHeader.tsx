import Header, { HeaderCenter, HeaderLeft, HeaderRight } from '@/components/layouts/Header'
import { BackButton } from '@/components/layouts/header-parts'
import { Suspense } from 'react'
import { ShopDetailHeaderSkeleton } from './ShopDetailHeaderSkeleton'
import ShopDetailHeaderServer from './ShopDetailHeaderServer'
import ShopDetailShareButtonServer from './ShopDetailShareButtonServer'

interface Props {
  shopId: number
}

export default function ShopDetailHeader({ shopId }: Props) {
  return (
    <Header variant="white" height={55}>
      <HeaderLeft>
        <BackButton />
      </HeaderLeft>
      <HeaderCenter>
        <Suspense fallback={<ShopDetailHeaderSkeleton />}>
          <ShopDetailHeaderServer shopId={shopId} />
        </Suspense>
      </HeaderCenter>
      <HeaderRight>
        <ShopDetailShareButtonServer shopId={shopId} />
      </HeaderRight>
    </Header>
  )
}
