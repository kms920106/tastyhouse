import { ImageGallerySkeleton } from '@/components/ui/ImageGallerySkeleton'
import { Suspense } from 'react'
import ShopDetailImageGalleryServer from './ShopDetailImageGalleryServer'

interface Props {
  shopId: number
}

export default function ShopDetailImageGalleryContent({ shopId }: Props) {
  return (
    <Suspense fallback={<ImageGallerySkeleton />}>
      <ShopDetailImageGalleryServer shopId={shopId} />
    </Suspense>
  )
}
