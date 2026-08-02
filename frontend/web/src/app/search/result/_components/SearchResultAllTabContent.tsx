import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import SearchResultAllMenuContent from './SearchResultAllMenuContent'
import SearchResultAllShopContent from './SearchResultAllShopContent'
import SearchResultAllReviewContent from './SearchResultAllReviewContent'

interface Props {
  query: string
  isLoggedIn: boolean
}

export default function SearchResultAllTabContent({ query, isLoggedIn }: Props) {
  return (
    <SectionStack>
      <BorderedSection>
        <SearchResultAllMenuContent query={query} />
      </BorderedSection>
      <BorderedSection>
        <SearchResultAllReviewContent query={query} />
      </BorderedSection>
      <BorderedSection>
        <SearchResultAllShopContent query={query} isLoggedIn={isLoggedIn} />
      </BorderedSection>
    </SectionStack>
  )
}
