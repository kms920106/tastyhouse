import { Suspense } from 'react'

import SearchPopularKeywordList from './SearchPopularKeywordList'
import SearchPopularKeywordListSkeleton from './SearchPopularKeywordListSkeleton'

export default function SearchPopularKeywordContent() {
  return (
    <div className="px-[15px] py-[30px]">
      <h2 className="text-base leading-[16px] font-bold mb-6">인기 검색어</h2>
      <Suspense fallback={<SearchPopularKeywordListSkeleton />}>
        <SearchPopularKeywordList />
      </Suspense>
    </div>
  )
}
