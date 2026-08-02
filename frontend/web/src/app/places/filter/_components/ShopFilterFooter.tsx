import StickyFooter from '@/components/ui/StickyFooter'
import ShopFilterApplyButton from './ShopFilterApplyButton'

export default function ShopFilterFooter() {
  return (
    <>
      <StickyFooter>
        <div className="px-[15px] py-2.5">
          <ShopFilterApplyButton />
        </div>
      </StickyFooter>
    </>
  )
}
