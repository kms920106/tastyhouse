import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import StickyFooter from '@/components/ui/StickyFooter'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import ShopDetailMenuList from './ShopDetailMenuList'
import ShopOrderNoticeContent from './ShopOrderNoticeContent'
import ShopPopularMenuGroup from './ShopPopularMenuGroup'

interface Props {
  shopId: number
}

export default function ShopDetailMenuTabContent({ shopId }: Props) {
  return (
    <div className="px-[15px]">
      {/* 주문안내 → 인기 메뉴 → 전체 메뉴 순서. 둘 다 미설정·빈 목록이면 스스로 null 을 반환한다 */}
      <ShopOrderNoticeContent shopId={shopId} />
      <ShopPopularMenuGroup shopId={shopId} />
      <ShopDetailMenuList shopId={shopId} />
      <StickyFooter>
        <div className="px-[15px] py-2.5 bg-[#f9f9f9]">
          <Link href={PAGE_PATHS.ORDER_METHOD(shopId)}>
            <AppPrimaryButton>주문하기</AppPrimaryButton>
          </Link>
        </div>
      </StickyFooter>
    </div>
  )
}
