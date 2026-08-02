import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import ShopDetailReviewStatistic from './ShopDetailReviewStatistic'
import ShopDetailReviewListContent from './ShopDetailReviewListContent'

interface Props {
  shopId: number
}

export default function ShopDetailReviewTabContent({ shopId }: Props) {
  return (
    <SectionStack>
      <BorderedSection>
        <ShopDetailReviewStatistic shopId={shopId} />
      </BorderedSection>
      <BorderedSection>
        <ShopDetailReviewListContent shopId={shopId} />
      </BorderedSection>
    </SectionStack>
  )
}
