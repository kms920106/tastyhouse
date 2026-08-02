import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import SearchPopularKeywordContent from './SearchPopularKeywordContent'
import SearchRecommendedKeywordContent from './SearchRecommendedKeywordContent'
import SearchHeader from './SearchHeader'

export default function SearchPage() {
  return (
    <>
      <SearchHeader />
      <SectionStack className="flex-1">
        <BorderedSection>
          <SearchPopularKeywordContent />
        </BorderedSection>
        <BorderedSection>
          <SearchRecommendedKeywordContent />
        </BorderedSection>
      </SectionStack>
    </>
  )
}
