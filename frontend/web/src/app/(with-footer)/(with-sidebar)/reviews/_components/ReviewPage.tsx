import type { ReviewTab } from './ReviewTabs'
import ReviewHeader from './ReviewHeader'
import ReviewTabs from '../_components/ReviewTabs'

interface Props {
  tab: ReviewTab
}

export default function ReviewPage({ tab }: Props) {
  return (
    <>
      <ReviewHeader />
      <ReviewTabs tab={tab} />
      <div className="h-[70px]" />
    </>
  )
}
