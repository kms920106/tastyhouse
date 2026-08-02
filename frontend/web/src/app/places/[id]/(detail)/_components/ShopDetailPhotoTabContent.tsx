import ShopDetailPhotoList from './ShopDetailPhotoList'

interface Props {
  shopId: number
}

export default function ShopDetailPhotoTabContent({ shopId }: Props) {
  return (
    <div className="px-[15px] pb-5">
      <ShopDetailPhotoList shopId={shopId} />
    </div>
  )
}
