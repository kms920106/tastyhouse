import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { searchRepository } from '@/domains/search/search.repository'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import SearchPopularKeywordListItem from './SearchPopularKeywordListItem'

export default async function SearchPopularKeywordList() {
  const { error, status, data } = await searchRepository.getPopularKeywords()

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('인기 검색어')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  const keywords = data

  return (
    <ul className="flex flex-col gap-5 px-[15px]">
      {keywords.map(({ rank, keyword, newKeyword }) => (
        <li key={rank}>
          <Link href={`${PAGE_PATHS.SEARCH}?q=${encodeURIComponent(keyword)}`}>
            <SearchPopularKeywordListItem rank={rank} keyword={keyword} isNew={newKeyword} />
          </Link>
        </li>
      ))}
    </ul>
  )
}
