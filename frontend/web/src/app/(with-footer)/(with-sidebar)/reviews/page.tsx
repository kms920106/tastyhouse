import type { ReviewTab } from './_components/ReviewTabs'
import ReviewPage from './_components/ReviewPage'

interface Props {
  searchParams: Promise<{
    tab?: string
  }>
}

const REVIEW_TAB_VALUES: ReviewTab[] = ['all', 'following']

function parseReviewTab(value: string | undefined): ReviewTab {
  return REVIEW_TAB_VALUES.includes(value as ReviewTab) ? (value as ReviewTab) : 'all'
}

export default async function Page({ searchParams }: Props) {
  const { tab } = await searchParams
  const initialTab = parseReviewTab(tab)

  return <ReviewPage tab={initialTab} />
}
