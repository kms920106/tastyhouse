'use client'

import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useMyBookmarks } from '@/domains/member/member.hook'
import BookmarkList from './BookmarkList'
import { BookmarkListSkeleton } from './BookmarkListSkeleton'

export default function MyPageBookmarkTabContent() {
  const { data, isLoading, error } = useMyBookmarks()

  if (isLoading) {
    return <BookmarkListSkeleton />
  }

  if (error || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('즐겨찾기')} />
  }

  return <BookmarkList bookmarks={data.bookmarks} hasMoreBookmarks={data.hasMoreBookmarks} />
}
