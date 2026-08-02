import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import StickyFooter from '@/components/ui/StickyFooter'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import ShopDetailMenuList from './ShopDetailMenuList'

interface Props {
  shopId: number
}

export default function ShopDetailMenuTabContent({ shopId }: Props) {
  return (
    <div className="px-[15px]">
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
