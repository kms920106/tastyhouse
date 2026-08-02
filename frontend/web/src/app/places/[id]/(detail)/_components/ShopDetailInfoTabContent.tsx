import ShopDetailInfoFetcher from './ShopDetailInfoFetcher'

interface Props {
  shopId: number
}

export default function ShopDetailInfoTabContent({ shopId }: Props) {
  return (
    <div className="px-[15px] py-5">
      <ShopDetailInfoFetcher shopId={shopId} />
    </div>
  )
}
