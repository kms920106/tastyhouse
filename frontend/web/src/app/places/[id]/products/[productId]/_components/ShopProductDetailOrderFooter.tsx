import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import StickyFooter from '@/components/ui/StickyFooter'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'

interface Props {
  shopId: number
}

export default function ShopProductDetailOrderFooter({ shopId }: Props) {
  return (
    <StickyFooter>
      <div className="px-[15px] py-2.5 bg-[#f9f9f9]">
        <Link href={PAGE_PATHS.ORDER_METHOD(shopId)}>
          <AppPrimaryButton>주문하기</AppPrimaryButton>
        </Link>
      </div>
    </StickyFooter>
  )
}
