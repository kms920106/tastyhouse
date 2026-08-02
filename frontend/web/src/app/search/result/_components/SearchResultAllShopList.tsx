'use client'

import BookmarkListItem from '@/components/shops/BookmarkListItem'
import { BookmarkListItemSkeleton } from '@/components/shops/BookmarkListItemSkeleton'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useSearchShopsPreview } from '@/domains/search/search.hook'
import SearchResultEmptyState from './SearchResultEmptyState'

interface Props {
  query: string
  isLoggedIn: boolean
}

export default function SearchResultAllShopList({ query, isLoggedIn }: Props) {
  const { data, isLoading, isError } = useSearchShopsPreview(query, isLoggedIn)

  if (isLoading)
    return (
      <div className="flex flex-col gap-2.5">
        {Array.from({ length: 3 }).map((_, i) => (
          <BookmarkListItemSkeleton key={i} />
        ))}
      </div>
    )
  if (isError) return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('플레이스')} />
  if (!data?.data || data.data.length === 0)
    return <SearchResultEmptyState query={query} label="플레이스" />

  return (
    <div className="flex flex-col gap-2.5">
      {data.data.map((bookmark) => (
        <BookmarkListItem
          key={bookmark.shopId}
          shopId={bookmark.shopId}
          shopImage={bookmark.imageUrl}
          region={bookmark.stationName}
          shopName={bookmark.shopName}
          rating={bookmark.rating}
          isBookmarked={bookmark.bookmarked}
        />
      ))}
    </div>
  )
}
