import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import ShopDetailNoticeContent from './ShopDetailNoticeContent'
import ShopDetailReviewStatistic from './ShopDetailReviewStatistic'
import ShopDetailReviewListContent from './ShopDetailReviewListContent'

interface Props {
  shopId: number
}

export default function ShopDetailReviewTabContent({ shopId }: Props) {
  return (
    <SectionStack>
      {/* PDF 원문 위치 — 리뷰 지면에서 가장 먼저 보이는 영역 */}
      <ShopDetailNoticeContent shopId={shopId} />
      <BorderedSection>
        <ShopDetailReviewStatistic shopId={shopId} />
      </BorderedSection>
      <BorderedSection>
        <ShopDetailReviewListContent shopId={shopId} />
      </BorderedSection>
    </SectionStack>
  )
}
