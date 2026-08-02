import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import ShopDetailHeader from './ShopDetailHeader'
import ShopDetailImageGalleryContent from './ShopDetailImageGalleryContent'
import ShopDetailSummaryContent from './ShopDetailSummaryContent'
import ShopDetailTabs, { type ShopDetailTab } from './ShopDetailTabs'

interface Props {
  shopId: number
  tab: ShopDetailTab
}

export default function ShopDetailPage({ shopId, tab }: Props) {
  return (
    <>
      <ShopDetailHeader shopId={shopId} />
      <SectionStack>
        <BorderedSection>
          <ShopDetailImageGalleryContent shopId={shopId} />
          <ShopDetailSummaryContent shopId={shopId} />
        </BorderedSection>
        <BorderedSection>
          <ShopDetailTabs shopId={shopId} tab={tab} />
        </BorderedSection>
      </SectionStack>
    </>
  )
}
