import SectionStack from '@/components/ui/SectionStack'
import LatestReviewList from './LatestReviewList'

export default function ReviewFollowingTabContent() {
  return (
    <SectionStack>
      <LatestReviewList reviewType="FOLLOWING" />
    </SectionStack>
  )
}
