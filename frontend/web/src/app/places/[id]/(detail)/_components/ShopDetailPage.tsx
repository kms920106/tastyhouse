import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import ShopDetailHeader from './ShopDetailHeader'
import ShopDetailImageGalleryContent from './ShopDetailImageGalleryContent'
import ShopDetailNoticeContent from './ShopDetailNoticeContent'
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
        {/* 공지가 없으면 컴포넌트가 통째로 null 을 반환하므로 BorderedSection 은 그 안에서 감싼다 */}
        <ShopDetailNoticeContent shopId={shopId} />
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
