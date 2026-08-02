import { Suspense } from 'react'
import SearchRecommendedKeywordList from './SearchRecommendedKeywordList'
import SearchRecommendedKeywordListSkeleton from './SearchRecommendedKeywordListSkeleton'

export default function SearchRecommendedKeywordContent() {
  return (
    <div className="px-[15px] py-[30px]">
      <h2 className="text-base leading-[16px] font-bold mb-6">추천 검색어</h2>
      <Suspense fallback={<SearchRecommendedKeywordListSkeleton />}>
        <SearchRecommendedKeywordList />
      </Suspense>
    </div>
  )
}
