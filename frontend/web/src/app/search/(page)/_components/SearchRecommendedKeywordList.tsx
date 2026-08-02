import FetchErrorState from '@/components/ui/FetchErrorState'
import HashTag from '@/components/ui/HashTag'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import type { RecommendedKeyword } from '@/domains/search/search.model'
import { searchRepository } from '@/domains/search/search.repository'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'

export default async function SearchRecommendedKeywordList() {
  const { error, status, data } = await searchRepository.getRecommendedKeywords()

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('추천 검색어')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  const keywords: RecommendedKeyword[] = data

  return (
    <div className="flex flex-wrap gap-2">
      {keywords.map(({ keyword }, i) => (
        <Link key={i} href={`${PAGE_PATHS.SEARCH}?q=${encodeURIComponent(keyword)}`}>
          <HashTag tag={keyword} variant="secondary" size="md" />
        </Link>
      ))}
    </div>
  )
}
