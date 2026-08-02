import SectionStack from '@/components/ui/SectionStack'
import LatestReviewList from './LatestReviewList'

export default function ReviewAllTabContent() {
  return (
    <SectionStack>
      <LatestReviewList reviewType="ALL" />
    </SectionStack>
  )
}
