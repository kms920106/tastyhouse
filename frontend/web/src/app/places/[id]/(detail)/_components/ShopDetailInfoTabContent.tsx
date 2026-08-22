import ShopDetailInfoFetcher from './ShopDetailInfoFetcher'
import ShopOriginContent from './ShopOriginContent'

interface Props {
  shopId: number
}

export default function ShopDetailInfoTabContent({ shopId }: Props) {
  return (
    <div className="px-[15px] py-5">
      <ShopDetailInfoFetcher shopId={shopId} />
      {/* 미설정이면 컴포넌트가 null 을 돌려 영역이 통째로 사라진다 */}
      <ShopOriginContent shopId={shopId} />
    </div>
  )
}
